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
import mffs.tileentity.ProjectorCalculationThread.IThreadCallBack;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import calclavia.lib.CalculationHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityForceFieldProjector extends TileEntityModuleAcceptor implements IProjector, IThreadCallBack
{
	private static final int MODULE_SLOT_ID = 2;

	/**
	 * A set containing all positions of all force field blocks.
	 */
	protected final Set<Vector3> forceFields = new HashSet<Vector3>();

	protected final Set<Vector3> calculatedField = Collections.synchronizedSet(new HashSet<Vector3>());

	public boolean isCalculating = false;
	public boolean isCalculated = false;

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
			this.requestFortron(this.getFortronCost(), true);

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

			if (this.ticks % (2 * 20) == 0)
			{
				this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "mffs.field", 0.6f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
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
		super.onInventoryChanged();
		this.destroyField();
	}

	private void calculateForceField(IThreadCallBack callBack)
	{
		if (!this.worldObj.isRemote && !this.isCalculating)
		{
			if (this.getMode() != null)
			{
				this.calculatedField.clear();

				// Start multi-threading calculation
				(new ProjectorCalculationThread(this, callBack)).start();
			}
		}
	}

	private void calculateForceField()
	{
		this.calculateForceField(null);
	}

	/**
	 * Projects a force field based on the calculations made.
	 */
	@Override
	public void projectField()
	{
		if (!this.worldObj.isRemote && this.isCalculated && !this.isCalculating)
		{
			int constructionCount = 0;
			int constructionSpeed = Math.min(this.getProjectionSpeed(), Settings.MAX_FORCE_FIELDS_PER_TICK);
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

			Iterator<Vector3> it = this.calculatedField.iterator();

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
								this.worldObj.setBlock(vector.intX(), vector.intY(), vector.intZ(), ModularForceFieldSystem.blockForceField.blockID, 0, 2);

								// Sets the controlling projector of the force field block to this
								// one.
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
				else
				{
					Block block = Block.blocksList[vector.getBlockID(this.worldObj)];

					if (block == ModularForceFieldSystem.blockForceField)
					{
						this.worldObj.setBlock(vector.intX(), vector.intY(), vector.intZ(), 0, 0, 3);
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
		if (this.getStackInSlot(MODULE_SLOT_ID) != null)
		{
			if (this.getStackInSlot(MODULE_SLOT_ID).getItem() instanceof IProjectorMode)
			{
				return (IProjectorMode) this.getStackInSlot(MODULE_SLOT_ID).getItem();
			}
		}

		return null;
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

	@Override
	public long getTicks()
	{
		return this.ticks;
	}

	@Override
	public Vector3 getTranslation()
	{
		String cacheID = "getTranslation";

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Vector3)
				{
					return (Vector3) this.cache.get(cacheID);
				}
			}
		}

		ForgeDirection direction = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);

		int zTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)));
		int zTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH)));

		int xTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST)));
		int xTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST)));

		int yTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.UP));
		int yTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.DOWN));

		Vector3 translation = new Vector3(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg);

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, translation);
		}

		return translation;
	}

	@Override
	public Vector3 getPositiveScale()
	{
		String cacheID = "getPositiveScale";

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Vector3)
				{
					return (Vector3) this.cache.get(cacheID);
				}
			}
		}

		ForgeDirection direction = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);

		int zScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH)));
		int xScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST)));
		int yScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.UP));

		int omnidirectionalScale = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getModuleSlots());

		zScalePos += omnidirectionalScale;
		xScalePos += omnidirectionalScale;
		yScalePos += omnidirectionalScale;

		Vector3 positiveScale = new Vector3(xScalePos, yScalePos, zScalePos);

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, positiveScale);
		}

		return positiveScale;
	}

	@Override
	public Vector3 getNegativeScale()
	{
		String cacheID = "getNegativeScale";

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Vector3)
				{
					return (Vector3) this.cache.get(cacheID);
				}
			}
		}

		ForgeDirection direction = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);

		int zScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)));
		int xScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST)));
		int yScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.DOWN));

		int omnidirectionalScale = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getModuleSlots());

		zScaleNeg += omnidirectionalScale;
		xScaleNeg += omnidirectionalScale;
		yScaleNeg += omnidirectionalScale;

		Vector3 negativeScale = new Vector3(xScaleNeg, yScaleNeg, zScaleNeg);

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, negativeScale);
		}

		return negativeScale;
	}

	@Override
	public int getRotationYaw()
	{
		String cacheID = "getRotationYaw";

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Integer)
				{
					return (Integer) this.cache.get(cacheID);
				}
			}
		}

		ForgeDirection direction = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		int horizontalRotation = this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST))) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST))) + this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH))) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)));

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, horizontalRotation);
		}

		return horizontalRotation;
	}

	@Override
	public int getRotationPitch()
	{
		String cacheID = "getRotationPitch";

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Integer)
				{
					return (Integer) this.cache.get(cacheID);
				}
			}
		}

		int verticleRotation = this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.UP)) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.DOWN));

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, verticleRotation);
		}

		return verticleRotation;
	}

	@Override
	public Set<Vector3> getInteriorPoints()
	{
		Set<Vector3> newField = this.getMode().getInteriorPoints(this);

		Vector3 translation = this.getTranslation();
		int rotationYaw = this.getRotationYaw();
		int rotationPitch = this.getRotationPitch();

		for (Vector3 position : newField)
		{
			if (rotationYaw != 0)
			{
				CalculationHelper.rotateXZByAngle(position, rotationYaw);
			}

			if (rotationPitch != 0)
			{
				CalculationHelper.rotateYByAngle(position, rotationPitch);
			}

			position.add(new Vector3(this));
			position.add(translation);
		}

		return newField;
	}

}