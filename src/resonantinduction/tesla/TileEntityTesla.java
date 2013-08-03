/**
 * 
 */
package resonantinduction.tesla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.BlockFurnace;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import resonantinduction.ITesla;
import resonantinduction.PacketHandler;
import resonantinduction.ResonantInduction;
import resonantinduction.base.IPacketReceiver;
import resonantinduction.base.TileEntityBase;
import resonantinduction.base.Vector3;

import com.google.common.io.ByteArrayDataInput;

/**
 * The Tesla TileEntity.
 * 
 * - Redstone (Prevent Output Toggle) - Right click (Prevent Input Toggle)
 * 
 * @author Calclavia
 * 
 */
public class TileEntityTesla extends TileEntityBase implements ITesla, IPacketReceiver
{
	public static final Vector3[] dyeColors = new Vector3[] { new Vector3(), new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(0.5, 0.5, 0), new Vector3(0, 0, 1), new Vector3(0.5, 0, 05), new Vector3(0, 0.3, 1), new Vector3(0.8, 0.8, 0.8), new Vector3(0.3, 0.3, 0.3), new Vector3(0.7, 0.2, 0.2), new Vector3(0.1, 0.872, 0.884), new Vector3(0, 0.8, 0.8), new Vector3(0.46f, 0.932, 1), new Vector3(0.5, 0.2, 0.5), new Vector3(0.7, 0.5, 0.1), new Vector3(1, 1, 1) };

	private final int DEFAULT_COLOR = 12;
	private int dyeID = DEFAULT_COLOR;
	private float energy = 0;
	private boolean doTransfer = false;

	private boolean canReceive = true;
	private boolean attackEntities = true;

	/** Prevents transfer loops */
	private final Set<TileEntityTesla> outputBlacklist = new HashSet<TileEntityTesla>();
	private final Set<TileEntityTesla> connectedTeslas = new HashSet<TileEntityTesla>();

	/**
	 * Caching
	 */
	private TileEntityTesla topCache = null;
	private TileEntityTesla controlCache = null;

	/**
	 * Client
	 */
	private int zapCounter = 0;

	@Override
	public void initiate()
	{
		TeslaGrid.instance().register(this);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		boolean doPacketUpdate = this.getEnergyStored() > 0;

		/**
		 * Only transfer if it is the bottom controlling Tesla tower.
		 */
		// TODO: Fix client side issue. || this.worldObj.isRemote
		if (this.ticks % (5 + this.worldObj.rand.nextInt(2)) == 0 && this.isController() && ((this.getEnergyStored() > 0 && this.doTransfer) || this.worldObj.isRemote) && !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
		{
			List<ITesla> transferTeslaCoils = new ArrayList<ITesla>();

			for (ITesla tesla : TeslaGrid.instance().get())
			{
				if (new Vector3((TileEntity) tesla).distance(new Vector3(this)) < this.getRange())
				{
					/**
					 * Make sure Tesla is not part of this tower.
					 */
					if (!this.connectedTeslas.contains(tesla) && tesla.canReceive(this))
					{
						if (tesla instanceof TileEntityTesla)
						{
							if (((TileEntityTesla) tesla).getHeight() <= 1)
							{
								continue;
							}

							tesla = ((TileEntityTesla) tesla).getControllingTelsa();
						}

						transferTeslaCoils.add(tesla);
					}
				}
			}

			final TileEntityTesla topTesla = this.getTopTelsa();

			/**
			 * Sort by distance.
			 */
			Collections.sort(transferTeslaCoils, new Comparator()
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

			if (transferTeslaCoils.size() > 0)
			{
				float transferEnergy = this.getEnergyStored() / transferTeslaCoils.size();
				int count = 0;
				for (ITesla tesla : transferTeslaCoils)
				{
					if (this.zapCounter % 5 == 0)
					{
						this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, ResonantInduction.PREFIX + "electricshock", this.getEnergyStored() / 25, (float) (1.3f - 0.5f * ((float) this.dyeID / 16f)));
					}

					Vector3 teslaVector = new Vector3((TileEntity) tesla);

					if (tesla instanceof TileEntityTesla)
					{
						((TileEntityTesla) tesla).getControllingTelsa().outputBlacklist.add(this);
						teslaVector = new Vector3(((TileEntityTesla) tesla).getTopTelsa());
					}

					ResonantInduction.proxy.renderElectricShock(this.worldObj, new Vector3(topTesla).translate(new Vector3(0.5)), teslaVector.translate(new Vector3(0.5)), (float) dyeColors[this.dyeID].x, (float) dyeColors[this.dyeID].y, (float) dyeColors[this.dyeID].z);

					tesla.transfer(transferEnergy * (1 - (this.worldObj.rand.nextFloat() * 0.1f)));
					this.transfer(-transferEnergy);

					if (count++ > 1)
					{
						break;
					}
				}

				if (this.attackEntities && this.zapCounter % 10 == 0)
				{
					int radius = 3;
					List<EntityLivingBase> entities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getAABBPool().getAABB(this.xCoord - radius, this.yCoord - radius, this.zCoord - radius, this.xCoord + radius, this.yCoord + radius, this.zCoord + radius));

					for (EntityLivingBase entity : entities)
					{
						entity.attackEntityFrom(DamageSource.magic, 1);
						ResonantInduction.proxy.renderElectricShock(this.worldObj, new Vector3(topTesla).translate(new Vector3(0.5)), new Vector3(entity));
					}
				}
			}

			this.zapCounter++;
			this.outputBlacklist.clear();
		}

		/**
		 * Draws power from furnace below it.
		 * 
		 * @author Calclavia
		 */
		TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);

		if (tileEntity instanceof TileEntityFurnace)
		{
			TileEntityFurnace furnaceTile = (TileEntityFurnace) tileEntity;

			if (furnaceTile.getStackInSlot(0) == null)
			{
				/**
				 * Steal power from furnace.
				 */
				boolean doBlockStateUpdate = furnaceTile.furnaceBurnTime > 0;

				if (furnaceTile.furnaceBurnTime == 0)
				{
					int burnTime = TileEntityFurnace.getItemBurnTime(furnaceTile.getStackInSlot(1));

					if (burnTime > 0)
					{
						furnaceTile.decrStackSize(1, 1);
						furnaceTile.furnaceBurnTime = burnTime;
					}
				}
				else
				{
					this.transfer(ResonantInduction.POWER_PER_COAL / 20);
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				}

				if (doBlockStateUpdate != furnaceTile.furnaceBurnTime > 0)
				{
					BlockFurnace.updateFurnaceBlockState(furnaceTile.furnaceBurnTime > 0, furnaceTile.worldObj, furnaceTile.xCoord, furnaceTile.yCoord, furnaceTile.zCoord);
				}
			}
			else if (this.getEnergyStored() > ResonantInduction.POWER_PER_COAL / 20 && furnaceTile.getStackInSlot(1) == null && FurnaceRecipes.smelting().getSmeltingResult(furnaceTile.getStackInSlot(0)) != null)
			{
				/**
				 * Inject power to furnace.
				 */
				boolean doBlockStateUpdate = furnaceTile.furnaceBurnTime > 0;

				furnaceTile.furnaceBurnTime += 2;
				this.transfer(-ResonantInduction.POWER_PER_COAL / 20);
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

				if (doBlockStateUpdate != furnaceTile.furnaceBurnTime > 0)
				{
					BlockFurnace.updateFurnaceBlockState(furnaceTile.furnaceBurnTime > 0, furnaceTile.worldObj, furnaceTile.xCoord, furnaceTile.yCoord, furnaceTile.zCoord);
				}
			}
		}

		if (!this.worldObj.isRemote && this.getEnergyStored() > 0 != doPacketUpdate)
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}

		this.clearCache();
	}

	@Override
	public boolean canReceive(TileEntity tileEntity)
	{
		return this.canReceive && !this.outputBlacklist.contains(tileEntity);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketHandler.getTileEntityPacket(this, (byte) 1, this.getEnergyStored(), this.dyeID, this.canReceive);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		return null;
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			switch (input.readByte())
			{
				case 1:
					this.energy = input.readFloat();
					this.dyeID = input.readInt();
					this.canReceive = input.readBoolean();
					break;

			}

			this.doTransfer = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean isController()
	{
		return this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) == 0;
	}

	private void clearCache()
	{
		this.topCache = null;
		this.controlCache = null;
	}

	@Override
	public void transfer(float transferEnergy)
	{
		if (isController() || this.getControllingTelsa() == this)
		{
			this.energy = Math.max(this.energy + transferEnergy, 0);
			this.doTransfer = true;
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		else
		{
			this.getControllingTelsa().transfer(transferEnergy);
		}
	}

	public float getEnergyStored()
	{
		return this.energy;
	}

	public int getRange()
	{
		return Math.min(5 * (this.getHeight() - 1), 50);
	}

	public void updatePositionStatus()
	{
		boolean isTop = new Vector3(this).translate(new Vector3(0, 1, 0)).getTileEntity(this.worldObj) instanceof TileEntityTesla;
		boolean isBottom = new Vector3(this).translate(new Vector3(0, -1, 0)).getTileEntity(this.worldObj) instanceof TileEntityTesla;

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
	public TileEntityTesla getTopTelsa()
	{
		if (this.topCache != null)
		{
			return this.topCache;
		}

		this.connectedTeslas.clear();
		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				this.connectedTeslas.add((TileEntityTesla) t);
				returnTile = (TileEntityTesla) t;
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
	 * For non-controlling Tesla to use.
	 * 
	 * @return
	 */
	public TileEntityTesla getControllingTelsa()
	{
		if (this.controlCache != null)
		{
			return this.controlCache;
		}

		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				returnTile = (TileEntityTesla) t;
			}
			else
			{
				break;
			}

			checkPosition.y--;
		}

		this.controlCache = returnTile;
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

			if (t instanceof TileEntityTesla)
			{
				this.connectedTeslas.add((TileEntityTesla) t);
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
		return this.attackEntities = !this.attackEntities;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.dyeID = nbt.getInteger("dyeID");
		this.canReceive = nbt.getBoolean("canReceive");
		this.attackEntities = nbt.getBoolean("attackEntities");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("dyeID", this.dyeID);
		nbt.setBoolean("canReceive", this.canReceive);
		nbt.setBoolean("attackEntities", this.attackEntities);
	}

}
