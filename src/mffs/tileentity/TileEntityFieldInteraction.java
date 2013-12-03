package mffs.tileentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.ICache;
import mffs.api.IFieldInteraction;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.base.TileEntityModuleAcceptor;
import mffs.tileentity.ProjectorCalculationThread.IThreadCallBack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityFieldInteraction extends TileEntityModuleAcceptor implements IFieldInteraction, IDelayedEventHandler
{
	protected static final int MODULE_SLOT_ID = 2;
	protected boolean isCalculating = false;
	protected boolean isCalculated = false;

	/**
	 * Are the directions on the GUI absolute values?
	 */
	public boolean isAbsolute = false;
	protected final Set<Vector3> calculatedField = Collections.synchronizedSet(new HashSet<Vector3>());
	private final List<DelayedEvent> delayedEvents = new ArrayList<DelayedEvent>();
	private final List<DelayedEvent> quedDelayedEvents = new ArrayList<DelayedEvent>();

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.delayedEvents.size() > 0)
		{
			do
			{
				this.quedDelayedEvents.clear();

				Iterator<DelayedEvent> it = this.delayedEvents.iterator();

				while (it.hasNext())
				{
					DelayedEvent evt = it.next();

					evt.update();

					if (evt.ticks <= 0)
					{
						it.remove();
					}
				}

				this.delayedEvents.addAll(this.quedDelayedEvents);
			}
			while (!this.quedDelayedEvents.isEmpty());
		}
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.TOGGLE_MODE_4.ordinal() && !this.worldObj.isRemote)
		{
			this.isAbsolute = !this.isAbsolute;
		}
	}

	protected void calculateForceField(IThreadCallBack callBack)
	{
		if (!this.worldObj.isRemote && !this.isCalculating)
		{
			if (this.getMode() != null)
			{
				if (this.getModeStack().getItem() instanceof ICache)
				{
					((ICache) this.getModeStack().getItem()).clearCache();
				}

				this.calculatedField.clear();

				// Start multi-threading calculations
				(new ProjectorCalculationThread(this, callBack)).start();
			}
		}
	}

	protected void calculateForceField()
	{
		this.calculateForceField(null);
	}

	@Override
	public ItemStack getModeStack()
	{
		if (this.getStackInSlot(MODULE_SLOT_ID) != null)
		{
			if (this.getStackInSlot(MODULE_SLOT_ID).getItem() instanceof IProjectorMode)
			{
				return this.getStackInSlot(MODULE_SLOT_ID);
			}
		}

		return null;
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
	public int[] getModuleSlots()
	{
		return new int[] { 15, 16, 17, 18, 19, 20 };
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

		ForgeDirection direction = this.getDirection();

		if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
		{
			direction = ForgeDirection.NORTH;
		}

		int zTranslationNeg, zTranslationPos, xTranslationNeg, xTranslationPos, yTranslationPos, yTranslationNeg;

		if (this.isAbsolute)
		{
			zTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.NORTH));
			zTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.SOUTH));

			xTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.WEST));
			xTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.EAST));

			yTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.UP));
			yTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.DOWN));
		}
		else
		{
			zTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)));
			zTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH)));

			xTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST)));
			xTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST)));

			yTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.UP));
			yTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.DOWN));
		}

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

		int zScalePos, xScalePos, yScalePos;

		if (this.isAbsolute)
		{
			zScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.SOUTH));
			xScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.EAST));
			yScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.UP));
		}
		else
		{
			ForgeDirection direction = this.getDirection();

			if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
			{
				direction = ForgeDirection.NORTH;
			}

			zScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH)));
			xScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST)));
			yScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.UP));
		}

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

		int zScaleNeg, xScaleNeg, yScaleNeg;

		if (this.isAbsolute)
		{
			zScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.NORTH));
			xScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.WEST));
			yScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.DOWN));
		}
		else
		{
			ForgeDirection direction = this.getDirection();

			if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
			{
				direction = ForgeDirection.NORTH;
			}

			zScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)));
			xScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST)));
			yScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.DOWN));
		}

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

		int horizontalRotation;

		if (this.isAbsolute)
		{
			horizontalRotation = this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.EAST)) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.WEST)) + this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.SOUTH)) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.NORTH));
		}
		else
		{
			ForgeDirection direction = this.getDirection();
			horizontalRotation = this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST))) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST))) + this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH))) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)));
		}

		horizontalRotation *= 2;

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
		verticleRotation *= 2;

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, verticleRotation);
		}

		return verticleRotation;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Vector3> getInteriorPoints()
	{
		final String cacheID = "getInteriorPoints";

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof Set)
				{
					return (Set<Vector3>) this.cache.get(cacheID);
				}
			}
		}

		if (this.getModeStack().getItem() instanceof ICache)
		{
			((ICache) this.getModeStack().getItem()).clearCache();
		}

		Set<Vector3> newField = this.getMode().getInteriorPoints(this);
		Set<Vector3> returnField = new HashSet<Vector3>();

		Vector3 translation = this.getTranslation();
		int rotationYaw = this.getRotationYaw();
		int rotationPitch = this.getRotationPitch();

		for (Vector3 position : newField)
		{
			Vector3 newPosition = position.clone();

			if (rotationYaw != 0 || rotationPitch != 0)
			{
				newPosition.rotate(rotationYaw, rotationPitch);
			}

			newPosition.translate(new Vector3(this));
			newPosition.translate(translation);

			returnField.add(newPosition);
		}

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, returnField);
		}

		return returnField;
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
				return new int[] { 7, 9 };
			case SOUTH:
				return new int[] { 8, 10 };
			case WEST:
				return new int[] { 4, 5 };
			case EAST:
				return new int[] { 12, 13 };
		}
	}

	@Override
	public void setCalculating(boolean bool)
	{
		this.isCalculating = bool;
	}

	@Override
	public void setCalculated(boolean bool)
	{
		this.isCalculated = bool;
	}

	@Override
	public Set<Vector3> getCalculatedField()
	{
		return this.calculatedField;
	}

	@Override
	public List<DelayedEvent> getDelayedEvents()
	{
		return this.delayedEvents;
	}

	@Override
	public List<DelayedEvent> getQuedDelayedEvents()
	{
		return this.quedDelayedEvents;
	}

	/**
	 * NBT Methods
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.isAbsolute = nbt.getBoolean("isAbsolute");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isAbsolute", this.isAbsolute);
	}
}
