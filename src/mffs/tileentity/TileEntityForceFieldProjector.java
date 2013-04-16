package mffs.tileentity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.IProjector;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.base.TileEntityModuleAcceptor;
import mffs.card.ItemCard;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityForceFieldProjector extends TileEntityModuleAcceptor implements IProjector
{
	private static final int MODULE_SLOT_ID = 2;

	/**
	 * A set containing all positions of all force field blocks.
	 */
	protected final Set<Vector3> forceFields = new HashSet<Vector3>();

	protected final Set<Vector3> calculatedField = Collections.synchronizedSet(new HashSet<Vector3>());
	protected final Set<Vector3> calculatedFieldInterior = Collections.synchronizedSet(new HashSet<Vector3>());

	private boolean isCalculated = false;

	public int animation = 0;

	public TileEntityForceFieldProjector()
	{
		this.fortronTank.setCapacity(20 * LiquidContainerRegistry.BUCKET_VOLUME);
		this.startModuleIndex = 1;
	}

	@Override
	public void initiate()
	{
		super.initiate();
		this.calculateForceField();
		this.destroyField();

	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.isActive() && this.getMode() != null && this.requestFortron(this.getFortronCost(), false) >= this.getFortronCost())
		{
			this.requestFortron(this.getFortronCost(), true);

			if (!this.worldObj.isRemote)
			{
				if (this.ticks % 10 == 0)
				{
					if (!this.isCalculated)
					{
						this.calculateForceField();
					}

					this.projectField();
				}
			}
			else
			{
				if (this.isActive())
				{
					this.animation += this.getFortronCost() / 3;
				}
			}

			if (this.ticks % (2 * 20) == 0)
			{
				this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "mffs.field", 1f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
			}
		}
		else if (!this.worldObj.isRemote)
		{
			this.destroyField();
		}
	}

	@Override
	public int getFortronCost()
	{
		float cost = 2;

		for (ItemStack itemStack : this.getModuleStacks())
		{
			if (itemStack != null)
			{
				cost += itemStack.stackSize * ((IModule) itemStack.getItem()).getFortronCost(this.getCalculatedField().size());
			}
		}

		return Math.round(cost);
	}

	@Override
	public void onInventoryChanged()
	{
		final boolean active = this.isActive();
		this.setActive(false);
		this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);

		if (active)
		{
			this.setActive(true);
		}
	}

	private void calculateForceField()
	{
		if (!this.worldObj.isRemote)
		{
			if (this.getMode() != null)
			{
				this.calculatedField.clear();
				this.calculatedFieldInterior.clear();

				this.getMode().calculateField(this, this.calculatedField, this.calculatedFieldInterior);

				for (Vector3 position : this.calculatedField)
				{
					position.add(new Vector3(this));
				}

				for (Vector3 position : this.calculatedFieldInterior)
				{
					position.add(new Vector3(this));
				}

				for (IModule module : this.getModules(this.getModuleSlots()))
				{
					module.onCalculate(this, this.calculatedField, this.calculatedFieldInterior);
				}

				this.isCalculated = true;
			}
		}
	}

	/**
	 * Projects a force field based on the calculations made.
	 */
	@Override
	public void projectField()
	{
		if (!this.worldObj.isRemote && this.isCalculated)
		{
			int constructionCount = 0;
			int constructionSpeed = Math.min(this.getConstructionSpeed(), Settings.MAX_FORCE_FIELDS_PER_TICK);
			this.forceFields.clear();

			HashSet<Vector3> fieldToBeProjected = new HashSet<Vector3>();
			fieldToBeProjected.addAll(this.calculatedField);

			for (IModule module : this.getModules(this.getModuleSlots()))
			{
				if (module.onProject(this, fieldToBeProjected))
				{
					return;
				}
			}

			Iterator<Vector3> it = fieldToBeProjected.iterator();

			while (it.hasNext())
			{
				Vector3 vector = it.next();

				if (constructionCount > constructionSpeed)
				{
					break;
				}

				Block block = Block.blocksList[vector.getBlockID(this.worldObj)];

				if (this.getModuleCount(ModularForceFieldSystem.itemModuleDisintegration) > 0 || block == null || block.blockMaterial.isLiquid() || block == Block.snow || block == Block.vine || block == Block.tallGrass || block == Block.deadBush || block.isBlockReplaceable(this.worldObj, vector.intX(), vector.intY(), vector.intZ()) || block == ModularForceFieldSystem.blockForceField)
				{
					if (block != ModularForceFieldSystem.blockForceField)
					{
						if (this.worldObj.getChunkFromBlockCoords(vector.intX(), vector.intZ()).isChunkLoaded)
						{
							this.worldObj.setBlock(vector.intX(), vector.intY(), vector.intZ(), ModularForceFieldSystem.blockForceField.blockID, 0, 2);

							// Sets the controlling projector of the force field block to this one.

							TileEntity tileEntity = this.worldObj.getBlockTileEntity(vector.intX(), vector.intY(), vector.intZ());

							if (tileEntity instanceof TileEntityForceField)
							{
								((TileEntityForceField) tileEntity).setZhuYao(new Vector3(this));
							}

							boolean cancel = false;

							for (IModule module : this.getModules(this.getModuleSlots()))
							{
								if (module.onProject(this, vector.clone()))
								{
									cancel = true;
								}
							}

							this.forceFields.add(vector);
							constructionCount++;

							if (cancel)
							{
								break;
							}
						}
					}
				}
			}

		}
	}

	@Override
	public void destroyField()
	{
		if (!this.worldObj.isRemote)
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

		this.calculatedField.clear();
		this.calculatedFieldInterior.clear();
		this.isCalculated = false;
	}

	@Override
	public void invalidate()
	{
		this.destroyField();
		super.invalidate();
	}

	@Override
	public int getConstructionSpeed()
	{
		return 100 + 20 * this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed, this.getModuleSlots());
	}

	@Override
	public int getSizeInventory()
	{
		return 3 + 18;
	}

	@Override
	public IProjectorMode getMode()
	{
		if (this.getModeStack() != null)
		{
			return (IProjectorMode) this.getModeStack().getItem();
		}

		return null;
	}

	@Override
	public ItemStack getModeStack()
	{
		ItemStack itemStack = this.getStackInSlot(MODULE_SLOT_ID);

		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IProjectorMode)
			{
				return itemStack;
			}
		}

		return null;
	}

	@Override
	public Set<Vector3> getInteriorPoints()
	{
		return this.calculatedFieldInterior;
	}

	@Override
	public Set<Vector3> getCalculatedField()
	{
		return this.calculatedField;
	}

	@Override
	public int getSidedModuleCount(IModule module, ForgeDirection... direction)
	{
		int count = 0;

		if (direction != null && direction.length > 0)
		{
			for (ForgeDirection checkDir : direction)
			{
				count += this.getModuleCount(module, this.getSlotsBasedOnDirection(checkDir));
			}
		}
		else
		{
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection checkDir = ForgeDirection.getOrientation(i);
				count += this.getModuleCount(module, this.getSlotsBasedOnDirection(checkDir));
			}
		}

		return count;
	}

	@Override
	public int[] getSlotsBasedOnDirection(ForgeDirection direction)
	{
		switch (direction)
		{
			default:
				return new int[] {};
			case UP:
				return new int[] { 3, 11 };
			case DOWN:
				return new int[] { 6, 14 };
			case NORTH:
				return new int[] { 8, 10 };
			case SOUTH:
				return new int[] { 7, 9 };
			case WEST:
				return new int[] { 12, 13 };
			case EAST:
				return new int[] { 4, 5 };
		}
	}

	@Override
	public int[] getModuleSlots()
	{
		return new int[] { 15, 16, 17, 18, 19, 20 };
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
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
		return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
	}

	public long getTicks()
	{
		return this.ticks;
	}
}