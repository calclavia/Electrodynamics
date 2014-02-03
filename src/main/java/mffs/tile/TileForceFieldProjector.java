package mffs.tile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.ICache;
import mffs.api.IProjector;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.card.ItemCard;
import mffs.item.mode.ItemModeCustom;
import mffs.tile.ProjectorCalculationThread.IThreadCallBack;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.chunk.Chunk;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.PacketHandler;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileForceFieldProjector extends TileFieldInteraction implements IProjector, IThreadCallBack
{
	/**
	 * A set containing all positions of all force field blocks.
	 */
	protected final Set<Vector3> forceFields = new HashSet<Vector3>();

	/** True if the field is done constructing and the projector is simply maintaining the field **/
	private boolean isCompleteConstructing = false;

	private boolean fieldRequireTicks = false;

	public boolean markFieldUpdate = true;

	public static final HashMap<Chunk, Set<TileForceFieldProjector>> chunkProjectorMap = new HashMap<Chunk, Set<TileForceFieldProjector>>();

	public TileForceFieldProjector()
	{
		this.capacityBase = 50;
		this.startModuleIndex = 1;
	}

	@Override
	public void initiate()
	{
		super.initiate();
		this.calculateForceField(this);
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		/**
		 * Stablizer Module Construction FXs
		 */
		if (this.worldObj.isRemote)
		{
			if (packetID == TilePacketType.FXS.ordinal())
			{
				int type = dataStream.readInt();
				Vector3 vector = new Vector3(dataStream.readInt(), dataStream.readInt(), dataStream.readInt()).translate(0.5);
				Vector3 root = new Vector3(this).translate(0.5);

				if (type == 1)
				{
					ModularForceFieldSystem.proxy.renderBeam(this.worldObj, root, vector, 0.6f, 0.6f, 1, 40);
					ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, 1, 1, 1, 50);
				}
				else if (type == 2)
				{
					ModularForceFieldSystem.proxy.renderBeam(this.worldObj, vector, root, 1f, 0f, 0f, 40);
					ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, 1, 0, 0, 50);
				}
			}
			else if (packetID == TilePacketType.FIELD.ordinal())
			{
				this.getCalculatedField().clear();
				NBTTagCompound nbt = PacketHandler.readNBTTagCompound(dataStream);
				NBTTagList nbtList = nbt.getTagList("blockList");

				for (int i = 0; i < nbtList.tagCount(); i++)
				{
					NBTTagCompound tagAt = (NBTTagCompound) nbtList.tagAt(i);
					this.getCalculatedField().add(new Vector3(tagAt));
				}

				this.isCalculated = true;
			}
		}
	}

	public void sendFieldToClient()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList nbtList = new NBTTagList();

		for (Vector3 vector : this.getCalculatedField())
		{
			nbtList.appendTag(vector.writeToNBT(new NBTTagCompound()));
		}

		nbt.setTag("blockList", nbtList);
		PacketDispatcher.sendPacketToAllPlayers(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FIELD.ordinal(), nbt));
	}

	@Override
	protected void calculateForceField(IThreadCallBack callBack)
	{
		if (!this.worldObj.isRemote && !this.isCalculating)
		{
			if (this.getMode() != null)
			{
				this.forceFields.clear();
			}
		}

		super.calculateForceField(callBack);
		this.isCompleteConstructing = false;
		this.fieldRequireTicks = false;

		if (this.getModuleStacks() != null)
		{
			for (ItemStack module : this.getModuleStacks())
			{
				if (((IModule) module.getItem()).requireTicks(module))
				{
					fieldRequireTicks = true;
					break;
				}
			}
		}
	}

	@Override
	public void onThreadComplete()
	{
		// TODO: Send packet on start-up?
		if (this.clientSideSimulationRequired())
		{
			this.sendFieldToClient();
		}
	}

	private boolean clientSideSimulationRequired()
	{
		return this.getModuleCount(ModularForceFieldSystem.itemModuleRepulsion) > 0;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.isActive() && this.getMode() != null && this.requestFortron(this.getFortronCost(), false) >= this.getFortronCost())
		{
			this.consumeCost();

			if (this.ticks % 10 == 0 || this.markFieldUpdate || this.fieldRequireTicks)
			{
				if (!this.isCalculated)
				{
					this.calculateForceField(this);
				}
				else
				{
					this.projectField();
				}
			}

			if (this.isActive() && this.worldObj.isRemote)
			{
				this.animation += this.getFortronCost() / 10;
			}

			if (this.ticks % (2 * 20) == 0 && this.getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0)
			{
				this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "field", 0.6f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
			}
		}
		else if (!this.worldObj.isRemote)
		{
			this.destroyField();
		}
	}

	/**
	 * Returns Fortron cost in ticks.
	 */
	@Override
	protected int doGetFortronCost()
	{
		if (this.getMode() != null)
		{
			return Math.round(super.doGetFortronCost() + this.getMode().getFortronCost(this.getAmplifier()));
		}

		return 0;
	}

	@Override
	protected float getAmplifier()
	{
		if (this.getMode() instanceof ItemModeCustom)
		{
			return ((ItemModeCustom) this.getMode()).getFieldBlocks(this, this.getModeStack()).size() / 1000;
		}

		return Math.max(Math.min((this.getCalculatedField().size() / 1000), 10), 1);
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.destroyField();
	}

	/**
	 * Projects a force field based on the calculations made.
	 */
	@Override
	public void projectField()
	{
		if (isCalculated && !isCalculating)
		{
			if (!isCompleteConstructing || markFieldUpdate || fieldRequireTicks)
			{
				this.markFieldUpdate = false;

				if (this.forceFields.size() <= 0)
				{
					if (this.getModeStack().getItem() instanceof ICache)
					{
						((ICache) this.getModeStack().getItem()).clearCache();
					}
				}

				int constructionCount = 0;
				int constructionSpeed = Math.min(this.getProjectionSpeed(), Settings.MAX_FORCE_FIELDS_PER_TICK);

				synchronized (calculatedField)
				{
					Set<Vector3> fieldToBeProjected = new HashSet<Vector3>(calculatedField);

					for (IModule module : this.getModules(this.getModuleSlots()))
					{
						if (module.onProject(this, fieldToBeProjected))
						{
							return;
						}
					}

					Iterator<Vector3> it = fieldToBeProjected.iterator();

					fieldLoop:
					while (it.hasNext())
					{
						Vector3 vector = it.next();

						Block block = Block.blocksList[vector.getBlockID(this.worldObj)];

						if (this.canReplaceBlock(vector, block))
						{
							/**
							 * Prevents the force field projector from disintegrating itself.
							 */
							if (block != ModularForceFieldSystem.blockForceField && !vector.equals(new Vector3(this)))
							{
								if (this.worldObj.getChunkFromBlockCoords(vector.intX(), vector.intZ()).isChunkLoaded)
								{
									constructionCount++;

									for (IModule module : this.getModules(this.getModuleSlots()))
									{
										int flag = module.onProject(this, vector.clone());

										if (flag == 1)
										{
											continue fieldLoop;
										}
										else if (flag == 2)
										{
											break fieldLoop;
										}
									}

									if (!this.worldObj.isRemote)
									{
										this.worldObj.setBlock(vector.intX(), vector.intY(), vector.intZ(), ModularForceFieldSystem.blockForceField.blockID, 0, 2);
									}

									this.forceFields.add(vector);

									/*
									 * Sets the controlling projector of the force field block to
									 * this one.
									 */
									TileEntity tileEntity = this.worldObj.getBlockTileEntity(vector.intX(), vector.intY(), vector.intZ());

									if (tileEntity instanceof TileForceField)
									{
										((TileForceField) tileEntity).setProjector(new Vector3(this));
									}

									this.requestFortron(1, true);

									if (constructionCount > constructionSpeed)
									{
										break;
									}
								}
							}
						}
					}
				}

				/**
				 * Change the field to tick every second when construction completes.
				 */
				this.isCompleteConstructing = constructionCount == 0;
			}
		}
	}

	private boolean canReplaceBlock(Vector3 vector, Block block)
	{
		return block == null || (this.getModuleCount(ModularForceFieldSystem.itemModuleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.intX(), vector.intY(), vector.intZ()) != -1) || block.blockMaterial.isLiquid() || block == Block.snow || block == Block.vine || block == Block.tallGrass || block == Block.deadBush || block.isBlockReplaceable(this.worldObj, vector.intX(), vector.intY(), vector.intZ());
	}

	@Override
	public void destroyField()
	{
		if (!this.worldObj.isRemote && this.isCalculated && !this.isCalculating)
		{
			synchronized (calculatedField)
			{
				HashSet<Vector3> copiedSet = new HashSet<Vector3>(calculatedField);
				Iterator<Vector3> it = copiedSet.iterator();

				for (IModule module : this.getModules(this.getModuleSlots()))
				{
					if (module.onDestroy(this, this.getCalculatedField()))
					{
						break;
					}
				}

				while (it.hasNext())
				{
					Vector3 vector = it.next();
					Block block = Block.blocksList[vector.getBlockID(this.worldObj)];

					if (block == ModularForceFieldSystem.blockForceField)
					{
						this.worldObj.setBlock(vector.intX(), vector.intY(), vector.intZ(), 0, 0, 3);
					}
				}
			}
		}

		this.forceFields.clear();
		this.calculatedField.clear();
		this.isCalculated = false;
		this.isCompleteConstructing = false;
		this.fieldRequireTicks = false;
	}

	@Override
	public void invalidate()
	{
		this.destroyField();
		super.invalidate();
	}

	@Override
	public int getProjectionSpeed()
	{
		return 28 + 28 * this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed, this.getModuleSlots());
	}

	@Override
	public int getSizeInventory()
	{
		return 3 + 18;
	}

	@Override
	public Set<Vector3> getForceFields()
	{
		return this.forceFields;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
	{
		if (slotID == 0 || slotID == 1)
		{
			return itemStack.getItem() instanceof ItemCard;
		}
		else if (slotID == MODULE_SLOT_ID)
		{
			return itemStack.getItem() instanceof IProjectorMode;
		}
		else if (slotID >= 15)
		{
			return true;
		}

		return itemStack.getItem() instanceof IModule;
	}

	@Override
	public Set<ItemStack> getCards()
	{
		Set<ItemStack> cards = new HashSet<ItemStack>();
		cards.add(super.getCard());
		cards.add(this.getStackInSlot(1));
		return cards;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getAABBPool().getAABB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 2, this.zCoord + 1);
	}

	@Override
	public long getTicks()
	{
		return this.ticks;
	}

}