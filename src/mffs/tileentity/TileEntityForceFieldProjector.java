package mffs.tileentity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.ICache;
import mffs.api.IProjector;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.block.BlockForceField;
import mffs.card.ItemCard;
import mffs.tileentity.ProjectorCalculationThread.IThreadCallBack;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityForceFieldProjector extends TileEntityFieldInteraction implements IProjector, IThreadCallBack
{
	/**
	 * A set containing all positions of all force field blocks.
	 */
	protected final Set<Vector3> forceFields = new HashSet<Vector3>();

	public TileEntityForceFieldProjector()
	{
		this.capacityBase = 50;
		this.startModuleIndex = 1;
	}

	@Override
	public void initiate()
	{
		super.initiate();
		this.calculateForceField();
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		/**
		 * Stablizer Module Construction FXs
		 */
		if (packetID == TilePacketType.FXS.ordinal() && this.worldObj.isRemote)
		{
			int type = dataStream.readInt();
			Vector3 vector = new Vector3(dataStream.readInt(), dataStream.readInt(), dataStream.readInt()).add(0.5);
			Vector3 root = new Vector3(this).add(0.5);

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
	}

	@Override
	public void onThreadComplete()
	{
		this.destroyField();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.isActive() && this.getMode() != null && this.requestFortron(this.getFortronCost(), false) >= this.getFortronCost())
		{
			this.consumeCost();

			if (!this.worldObj.isRemote)
			{
				if (this.ticks % 10 == 0)
				{
					if (!this.isCalculated)
					{
						this.calculateForceField();
					}
					else
					{
						this.projectField();
					}
				}
			}
			else
			{
				if (this.isActive())
				{
					this.animation += this.getFortronCost() / 3;
				}
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
	public int getFortronCost()
	{
		return super.getFortronCost() + 5;
	}

	@Override
	public float getAmplifier()
	{
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
		if (!this.worldObj.isRemote && this.isCalculated && !this.isCalculating)
		{
			if (this.forceFields.size() <= 0)
			{
				if (this.getModeStack().getItem() instanceof ICache)
				{
					((ICache) this.getModeStack().getItem()).clearCache();
				}
			}

			int constructionCount = 0;
			int constructionSpeed = Math.min(this.getProjectionSpeed(), Settings.MAX_FORCE_FIELDS_PER_TICK);

			HashSet<Vector3> fieldToBeProjected = new HashSet<Vector3>();
			fieldToBeProjected.addAll(this.calculatedField);

			for (IModule module : this.getModules(this.getModuleSlots()))
			{
				if (module.onProject(this, fieldToBeProjected))
				{
					return;
				}
			}

			Iterator<Vector3> it = this.calculatedField.iterator();

			fieldLoop:
			while (it.hasNext())
			{
				Vector3 vector = it.next();

				if (fieldToBeProjected.contains(vector))
				{
					if (constructionCount > constructionSpeed)
					{
						break;
					}

					Block block = Block.blocksList[vector.getBlockID(this.worldObj)];

					if (block == null || (this.getModuleCount(ModularForceFieldSystem.itemModuleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.intX(), vector.intY(), vector.intZ()) != -1) || block.blockMaterial.isLiquid() || block == Block.snow || block == Block.vine || block == Block.tallGrass || block == Block.deadBush || block.isBlockReplaceable(this.worldObj, vector.intX(), vector.intY(), vector.intZ()))
					{
						/**
						 * Prevents the force field projector from disintegrating itself.
						 */
						if (block != ModularForceFieldSystem.blockForceField && !vector.equals(new Vector3(this)))
						{
							if (this.worldObj.getChunkFromBlockCoords(vector.intX(), vector.intZ()).isChunkLoaded)
							{
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

								this.worldObj.setBlock(vector.intX(), vector.intY(), vector.intZ(), ModularForceFieldSystem.blockForceField.blockID, 0, 2);

								// Sets the controlling projector of the force field block to
								// this one.
								TileEntity tileEntity = this.worldObj.getBlockTileEntity(vector.intX(), vector.intY(), vector.intZ());

								if (tileEntity instanceof TileEntityForceField)
								{
									((TileEntityForceField) tileEntity).setProjector(new Vector3(this));
								}

								this.requestFortron(1, true);
								this.forceFields.add(vector);
								constructionCount++;
							}
						}
					}
				}
				else
				{
					Block block = Block.blocksList[vector.getBlockID(this.worldObj)];

					if (block == ModularForceFieldSystem.blockForceField)
					{
						if (((BlockForceField) block).getProjector(this.worldObj, vector.intX(), vector.intY(), vector.intZ()) == this)
						{
							this.worldObj.setBlock(vector.intX(), vector.intY(), vector.intZ(), 0);
						}
					}
				}
			}

		}
	}

	@Override
	public void destroyField()
	{
		if (!this.worldObj.isRemote && this.isCalculated && !this.isCalculating)
		{
			HashSet<Vector3> copiedSet = new HashSet<Vector3>();
			copiedSet.addAll(this.calculatedField);
			Iterator<Vector3> it = copiedSet.iterator();

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

		this.forceFields.clear();
		this.calculatedField.clear();
		this.isCalculated = false;
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