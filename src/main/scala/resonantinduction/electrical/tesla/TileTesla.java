/**
 * 
 */
package resonantinduction.electrical.tesla;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.multiblock.IMultiBlockStructure;
import resonant.lib.multiblock.MultiBlockHandler;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.IPacketSender;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.damage.ElectricalDamage;
import resonant.lib.prefab.tile.TileElectrical;
import resonant.lib.render.EnumColor;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * The Tesla TileEntity.
 * 
 * - Redstone (Prevent Output Toggle) - Right click (Prevent Input Toggle)
 * 
 * @author Calclavia
 * 
 */
public class TileTesla extends TileElectrical implements IMultiBlockStructure<TileTesla>, ITesla, IPacketSender, IPacketReceiver
{
	public final static int DEFAULT_COLOR = 12;
	public final long TRANSFER_CAP = 10000;
	private int dyeID = DEFAULT_COLOR;

	private boolean canReceive = true;
	private boolean attackEntities = true;

	/** Client side to do sparks */
	private boolean doTransfer = true;

	/** Prevents transfer loops */
	private final Set<TileTesla> outputBlacklist = new HashSet<TileTesla>();
	private final Set<TileTesla> connectedTeslas = new HashSet<TileTesla>();

	/**
	 * Quantum Tesla
	 */
	public Vector3 linked;
	public int linkDim;

	/**
	 * Client
	 */
	private int zapCounter = 0;
	private boolean isLinkedClient;
	private boolean isTransfering;

	private TileTesla topCache;

	public TileTesla()
	{
		this.energy = new EnergyStorageHandler(TRANSFER_CAP);
		this.saveIOMap = true;
	}

	@Override
	public void initiate()
	{
		super.initiate();
		TeslaGrid.instance().register(this);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		boolean doPacketUpdate = this.energy.getEnergy() > 0;

		/**
		 * Only transfer if it is the controlling Tesla tower.
		 */
		if (this.getMultiBlock().isPrimary())
		{
			this.produce();

			if (this.ticks % (4 + this.worldObj.rand.nextInt(2)) == 0 && ((this.worldObj.isRemote && isTransfering) || (!this.energy.isEmpty() && !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))))
			{
				final TileTesla topTesla = this.getTopTelsa();
				final Vector3 topTeslaVector = new Vector3(topTesla);

				if (this.linked != null || this.isLinkedClient)
				{
					/**
					 * Quantum transportation.
					 */
					if (!this.worldObj.isRemote)
					{
						World dimWorld = MinecraftServer.getServer().worldServerForDimension(this.linkDim);

						if (dimWorld != null)
						{
							TileEntity transferTile = this.linked.getTileEntity(dimWorld);

							if (transferTile instanceof TileTesla && !transferTile.isInvalid())
							{
								this.transfer(((TileTesla) transferTile), Math.min(this.energy.getEnergy(), TRANSFER_CAP));

								if (this.zapCounter % 5 == 0 && Settings.SOUND_FXS)
								{
									this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.PREFIX + "electricshock", (float) this.energy.getEnergy() / (float) TRANSFER_CAP, 1.3f - 0.5f * (this.dyeID / 16f));
								}
							}
						}
					}
					else
					{
						Electrical.proxy.renderElectricShock(this.worldObj, topTeslaVector.clone().translate(0.5), topTeslaVector.clone().translate(new Vector3(0.5, Double.POSITIVE_INFINITY, 0.5)), false);
					}
				}
				else
				{
					/**
					 * Normal transportation
					 */
					PriorityQueue<ITesla> teslaToTransfer = new PriorityQueue<ITesla>(1024, new Comparator()
					{
						public int compare(ITesla o1, ITesla o2)
						{
							double distance1 = new Vector3(topTesla).distance(new Vector3((TileEntity) o1));
							double distance2 = new Vector3(topTesla).distance(new Vector3((TileEntity) o2));

							if (distance1 < distance2)
							{
								return 1;
							}
							else if (distance1 > distance2)
							{
								return -1;
							}

							return 0;
						}

						@Override
						public int compare(Object obj, Object obj1)
						{
							return compare((ITesla) obj, (ITesla) obj1);
						}
					});

					for (ITesla otherTesla : TeslaGrid.instance().get())
					{
						if (new Vector3((TileEntity) otherTesla).distance(new Vector3(this)) < this.getRange() && otherTesla != this)
						{
							if (otherTesla instanceof TileTesla)
							{
								if (((TileTesla) otherTesla).getHeight() <= 1)
								{
									continue;
								}

								otherTesla = ((TileTesla) otherTesla).getMultiBlock().get();
							}

							/**
							 * Make sure Tesla is not part of this tower.
							 */
							if (!connectedTeslas.contains(otherTesla) && otherTesla != this && otherTesla.canTeslaTransfer(this) && canTeslaTransfer((TileEntity) otherTesla))
							{
								teslaToTransfer.add(otherTesla);
							}
						}
					}

					if (teslaToTransfer.size() > 0)
					{
						long transferEnergy = this.energy.getEnergy() / teslaToTransfer.size();

						boolean sentPacket = false;

						for (int count = 0; count < 10; count++)
						{
							if (!teslaToTransfer.isEmpty())
							{
								ITesla tesla = teslaToTransfer.poll();

								if (this.zapCounter % 5 == 0 && Settings.SOUND_FXS)
								{
									this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.PREFIX + "electricshock", (float) this.energy.getEnergy() / (float) TRANSFER_CAP, 1.3f - 0.5f * (this.dyeID / 16f));
								}

								Vector3 targetVector = new Vector3((TileEntity) tesla);
								int heightRange = 1;

								if (tesla instanceof TileTesla)
								{
									getMultiBlock().get().outputBlacklist.add(this);
									targetVector = new Vector3(((TileTesla) tesla).getTopTelsa());
									heightRange = ((TileTesla) tesla).getHeight();
								}

								double distance = topTeslaVector.distance(targetVector);

								Electrical.proxy.renderElectricShock(this.worldObj, new Vector3(topTesla).translate(new Vector3(0.5)), targetVector.translate(new Vector3(0.5, Math.random() * heightRange / 3 - heightRange / 3, 0.5)), EnumColor.DYES[this.dyeID].toColor());

								this.transfer(tesla, Math.min(transferEnergy, TRANSFER_CAP));

								if (!sentPacket && transferEnergy > 0)
								{
									this.sendPacket(3);
								}

								if (this.attackEntities && this.zapCounter % 5 == 0)
								{
									MovingObjectPosition mop = topTeslaVector.clone().translate(0.5).rayTraceEntities(this.worldObj, targetVector.clone().translate(0.5));

									if (mop != null && mop.entityHit != null)
									{
										if (mop.entityHit instanceof EntityLivingBase)
										{
										    ElectricalDamage.electrocuteEntity(mop.entityHit, this, UniversalElectricity.DEFAULT_VOLTAGE * 4, 1);
											Electrical.proxy.renderElectricShock(this.worldObj, new Vector3(topTesla).clone().translate(0.5), new Vector3(mop.entityHit));
										}
									}
								}
							}
						}
					}
				}

				this.zapCounter++;
				this.outputBlacklist.clear();

				this.doTransfer = false;
			}

			if (!this.worldObj.isRemote && this.energy.didEnergyStateChange())
			{
				this.sendPacket(2);
			}
		}

		this.topCache = null;
	}

	private void transfer(ITesla tesla, long transferEnergy)
	{
		if (transferEnergy > 0)
		{
			tesla.teslaTransfer(transferEnergy, true);
			this.teslaTransfer(-transferEnergy, true);
		}
	}

	@Override
	public boolean canTeslaTransfer(TileEntity tileEntity)
	{

		if (tileEntity instanceof TileTesla)
		{
			TileTesla otherTesla = (TileTesla) tileEntity;
			// Make sure Tesla is the same color
			if (!(otherTesla.dyeID == dyeID || (otherTesla.dyeID == DEFAULT_COLOR || dyeID == DEFAULT_COLOR)))
			{
				return false;
			}
		}

		return canReceive && tileEntity != getMultiBlock().get() && !this.outputBlacklist.contains(tileEntity);

	}

	@Override
	public boolean canConnect(ForgeDirection direction, Object obj)
	{
		return super.canConnect(direction, obj) && this.getMultiBlock().isPrimary() && !(obj instanceof TileTesla);
	}

	public void sendPacket(int type)
	{
		PacketDispatcher.sendPacketToAllInDimension(ResonantInduction.PACKET_TILE.getPacket(this, this.getPacketData(type).toArray()), this.worldObj.provider.dimensionId);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_TILE.getPacket(this, this.getPacketData(1).toArray());
	}

	/**
	 * 1 - Description Packet
	 * 2 - Energy Update
	 * 3 - Tesla Beam
	 */
	@Override
	public ArrayList getPacketData(int type)
	{
		ArrayList data = new ArrayList();
		data.add((byte) type);

		switch (type)
		{
			case 1:
			{
				data.add(this.dyeID);
				data.add(this.canReceive);
				data.add(this.attackEntities);
				data.add(this.linked != null);
				NBTTagCompound nbt = new NBTTagCompound();
				getMultiBlock().save(nbt);
				data.add(nbt);
				break;
			}
			case 2:
			{
				data.add(this.energy.getEnergy() > 0);
				break;
			}
		}

		return data;
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			switch (data.readByte())
			{
				case 1:
					this.dyeID = data.readInt();
					this.canReceive = data.readBoolean();
					this.attackEntities = data.readBoolean();
					this.isLinkedClient = data.readBoolean();
					getMultiBlock().load(PacketHandler.readNBTTagCompound(data));
					break;
				case 2:
					this.isTransfering = data.readBoolean();
					break;
				case 3:
					this.doTransfer = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public long teslaTransfer(long transferEnergy, boolean doTransfer)
	{
		if (getMultiBlock().isPrimary())
		{
			if (doTransfer)
			{
				this.energy.receiveEnergy(transferEnergy, true);

				if (this.energy.didEnergyStateChange())
				{
					this.sendPacket(2);
				}
			}

			return transferEnergy;
		}
		else
		{
			if (this.energy.getEnergy() > 0)
			{
				transferEnergy += this.energy.getEnergy();
				this.energy.setEnergy(0);
			}

			return getMultiBlock().get().teslaTransfer(transferEnergy, doTransfer);
		}
	}

	public int getRange()
	{
		return Math.min(4 * (this.getHeight() - 1), 50);
	}

	public void updatePositionStatus()
	{
		TileTesla mainTile = getLowestTesla();
		mainTile.getMultiBlock().deconstruct();
		mainTile.getMultiBlock().construct();

		boolean isTop = new Vector3(this).translate(new Vector3(0, 1, 0)).getTileEntity(this.worldObj) instanceof TileTesla;
		boolean isBottom = new Vector3(this).translate(new Vector3(0, -1, 0)).getTileEntity(this.worldObj) instanceof TileTesla;

		if (isTop && isBottom)
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
		}
		else if (isBottom)
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 2, 3);
		}
		else
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
		}
	}

	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public TileTesla getTopTelsa()
	{
		if (this.topCache != null)
		{
			return this.topCache;
		}

		this.connectedTeslas.clear();
		Vector3 checkPosition = new Vector3(this);
		TileTesla returnTile = this;

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileTesla)
			{
				this.connectedTeslas.add((TileTesla) t);
				returnTile = (TileTesla) t;
			}
			else
			{
				break;
			}

			checkPosition.y++;
		}

		this.topCache = returnTile;
		return returnTile;
	}

	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public int getHeight()
	{
		this.connectedTeslas.clear();
		int y = 0;

		while (true)
		{
			TileEntity t = new Vector3(this).translate(new Vector3(0, y, 0)).getTileEntity(this.worldObj);

			if (t instanceof TileTesla)
			{
				this.connectedTeslas.add((TileTesla) t);
				y++;
			}
			else
			{
				break;
			}

		}

		return y;
	}

	@Override
	public void invalidate()
	{
		TeslaGrid.instance().unregister(this);
		super.invalidate();
	}

	public void setDye(int id)
	{
		this.dyeID = id;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	public boolean toggleReceive()
	{
		return this.canReceive = !this.canReceive;
	}

	public boolean toggleEntityAttack()
	{
		boolean returnBool = this.attackEntities = !this.attackEntities;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		return returnBool;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.dyeID = nbt.getInteger("dyeID");
		this.canReceive = nbt.getBoolean("canReceive");
		this.attackEntities = nbt.getBoolean("attackEntities");

		if (nbt.hasKey("link_x") && nbt.hasKey("link_y") && nbt.hasKey("link_z"))
		{
			this.linked = new Vector3(nbt.getInteger("link_x"), nbt.getInteger("link_y"), nbt.getInteger("link_z"));
			this.linkDim = nbt.getInteger("linkDim");
		}
		getMultiBlock().load(nbt);
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("dyeID", this.dyeID);
		nbt.setBoolean("canReceive", this.canReceive);
		nbt.setBoolean("attackEntities", this.attackEntities);

		if (this.linked != null)
		{
			nbt.setInteger("link_x", (int) this.linked.x);
			nbt.setInteger("link_y", (int) this.linked.y);
			nbt.setInteger("link_z", (int) this.linked.z);
			nbt.setInteger("linkDim", this.linkDim);
		}
		getMultiBlock().save(nbt);
	}

	public void setLink(Vector3 vector3, int dimID, boolean setOpponent)
	{
		if (!worldObj.isRemote)
		{
			World otherWorld = MinecraftServer.getServer().worldServerForDimension(linkDim);

			if (setOpponent && linked != null && otherWorld != null)
			{
				TileEntity tileEntity = linked.getTileEntity(otherWorld);

				if (tileEntity instanceof TileTesla)
				{
					((TileTesla) tileEntity).setLink(null, this.linkDim, false);
				}
			}

			linked = vector3;
			linkDim = dimID;

			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

			World newOtherWorld = MinecraftServer.getServer().worldServerForDimension(this.linkDim);

			if (setOpponent && newOtherWorld != null && this.linked != null)
			{
				TileEntity tileEntity = this.linked.getTileEntity(newOtherWorld);

				if (tileEntity instanceof TileTesla)
				{
					((TileTesla) tileEntity).setLink(new Vector3(this), this.worldObj.provider.dimensionId, false);
				}
			}
		}
	}

	public boolean tryLink(VectorWorld vector)
	{
		if (vector != null)
		{
			if (vector.getTileEntity() instanceof TileTesla)
			{
				setLink(vector, vector.world.provider.dimensionId, true);
			}

			return true;
		}

		return false;
	}

	/**
	 * Multiblock Methods.
	 */
	private MultiBlockHandler<TileTesla> multiBlock;

	@Override
	public void onMultiBlockChanged()
	{

	}

	@Override
	public Vector3[] getMultiBlockVectors()
	{
		List<Vector3> vectors = new ArrayList<Vector3>();

		Vector3 checkPosition = new Vector3(this);

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileTesla)
			{
				vectors.add(checkPosition.clone().subtract(getPosition()));
			}
			else
			{
				break;
			}

			checkPosition.y++;
		}

		return vectors.toArray(new Vector3[0]);
	}

	public TileTesla getLowestTesla()
	{
		TileTesla lowest = this;
		Vector3 checkPosition = new Vector3(this);

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileTesla)
			{
				lowest = (TileTesla) t;
			}
			else
			{
				break;
			}

			checkPosition.y--;
		}

		return lowest;
	}

	@Override
	public World getWorld()
	{
		return worldObj;
	}

	@Override
	public Vector3 getPosition()
	{
		return new Vector3(this);
	}

	@Override
	public MultiBlockHandler<TileTesla> getMultiBlock()
	{
		if (multiBlock == null)
			multiBlock = new MultiBlockHandler<TileTesla>(this);

		return multiBlock;
	}

	@Override
	public void setIO(ForgeDirection dir, int type)
	{
		if (getMultiBlock().isPrimary())
		{
			super.setIO(dir, type);
		}
		else
		{
			getMultiBlock().get().setIO(dir, type);
		}
	}

	@Override
	public int getIO(ForgeDirection dir)
	{
		if (getMultiBlock().isPrimary())
		{
			return super.getIO(dir);
		}

		return getMultiBlock().get().getIO(dir);
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		EnumSet<ForgeDirection> dirs = super.getOutputDirections();
		dirs.remove(ForgeDirection.UP);
		return dirs;
	}
}
