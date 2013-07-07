package mffs.tileentity;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.Blacklist;
import mffs.api.ISpecialForceManipulation;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.card.ItemCard;
import mffs.event.BlockPreMoveDelayedEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public class TileEntityForceManipulator extends TileEntityFieldInteraction
{
	public static final int ANIMATION_TIME = 20;
	public Vector3 anchor = null;

	/**
	 * The display mode. 0 = none, 1 = minimal, 2 = maximal.
	 */
	public int displayMode = 1;

	public boolean isCalculatingManipulation = false;
	public Set<Vector3> manipulationVectors = null;
	public boolean doAnchor = true;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.anchor == null)
		{
			this.anchor = new Vector3();
		}

		if (this.getMode() != null && Settings.ENABLE_MANIPULATOR)
		{
			if (!this.worldObj.isRemote)
			{
				if (this.manipulationVectors != null && !this.isCalculatingManipulation)
				{
					/**
					 * This section is called when blocks set events are set and animation packets
					 * are to be sent.
					 */
					ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);

					NBTTagCompound nbt = new NBTTagCompound();
					NBTTagList nbtList = new NBTTagList();

					int i = 0;

					for (Vector3 position : this.manipulationVectors)
					{
						if (this.moveBlock(position, dir) && this.isBlockVisible(position) && i < Settings.MAX_FORCE_FIELDS_PER_TICK)
						{
							nbtList.appendTag(position.writeToNBT(new NBTTagCompound()));
							i++;
						}
					}

					nbt.setByte("type", (byte) 2);
					nbt.setTag("list", nbtList);

					PacketManager.sendPacketToClients(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this, TilePacketType.FXS.ordinal(), nbt), worldObj, new Vector3(this), 60);

					if (this.doAnchor)
					{
						this.anchor = this.anchor.modifyPositionFromSide(dir);
					}

					this.updatePushedObjects(0.02f);
					this.manipulationVectors = null;
					this.onInventoryChanged();
				}
			}

			if (this.isActive() && this.ticks % 20 == 0 && this.requestFortron(this.getFortronCost(), false) > 0)
			{
				if (!this.worldObj.isRemote)
				{
					this.requestFortron(this.getFortronCost(), true);
					// Start multi-threading calculations
					(new ManipulatorCalculationThread(this)).start();
				}

				if (this.getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0)
				{
					this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "mffs:fieldmove", 0.6f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
				}

				this.setActive(false);
			}

			if (!this.worldObj.isRemote)
			{
				if (!this.isCalculated)
				{
					this.calculateForceField();
				}

				// Manipulation area preview
				if (this.ticks % 120 == 0 && !this.isCalculating && Settings.HIGH_GRAPHICS && this.getDelayedEvents().size() <= 0 && this.displayMode > 0)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					NBTTagList nbtList = new NBTTagList();

					int i = 0;

					for (Vector3 position : this.getInteriorPoints())
					{
						if (this.isBlockVisible(position) && (this.displayMode == 2 || position.getBlockID(this.worldObj) > 0) && i < Settings.MAX_FORCE_FIELDS_PER_TICK)
						{
							nbtList.appendTag(position.writeToNBT(new NBTTagCompound()));
							i++;
						}
					}

					nbt.setByte("type", (byte) 1);
					nbt.setTag("list", nbtList);

					PacketManager.sendPacketToClients(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this, TilePacketType.FXS.ordinal(), nbt), worldObj, new Vector3(this), 60);
				}
			}
		}
	}

	public boolean isBlockVisible(Vector3 position)
	{
		int i = 0;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			Vector3 checkPos = position.clone().modifyPositionFromSide(direction);
			int blockID = checkPos.getBlockID(this.worldObj);

			if (blockID > 0)
			{
				if (Block.blocksList[blockID] != null)
				{
					if (Block.blocksList[blockID].isOpaqueCube())
					{
						i++;
					}
				}
			}
		}

		return !(i >= ForgeDirection.VALID_DIRECTIONS.length);
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.FXS.ordinal() && this.worldObj.isRemote)
		{
			/**
			 * Holographic FXs
			 */
			NBTTagCompound nbt = PacketManager.readNBTTagCompound(dataStream);
			byte type = nbt.getByte("type");

			NBTTagList nbtList = (NBTTagList) nbt.getTag("list");

			for (int i = 0; i < nbtList.tagCount(); i++)
			{
				Vector3 vector = Vector3.readFromNBT((NBTTagCompound) nbtList.tagAt(i)).add(0.5);

				if (type == 1)
				{
					ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 1, 1, 30, vector.clone().modifyPositionFromSide(this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord)));
				}
				else if (type == 2)
				{
					// Red
					ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 0, 0, 30, vector.clone().modifyPositionFromSide(this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord)));
					this.updatePushedObjects(0.02f);
				}
			}
		}
		else if (packetID == TilePacketType.TOGGLE_MODE.ordinal() && !this.worldObj.isRemote)
		{
			this.anchor = null;
			this.onInventoryChanged();
		}
		else if (packetID == TilePacketType.TOGGLE_MODE_2.ordinal() && !this.worldObj.isRemote)
		{
			this.displayMode = (this.displayMode + 1) % 3;
		}
		else if (packetID == TilePacketType.TOGGLE_MODE_3.ordinal() && !this.worldObj.isRemote)
		{
			this.doAnchor = !this.doAnchor;
		}
	}

	@Override
	public int getFortronCost()
	{
		return (int) ((super.getFortronCost() + this.anchor.getMagnitude()) * 1000);
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.isCalculated = false;
	}

	/**
	 * Scan target area...
	 */
	protected boolean canMove()
	{
		Set<Vector3> mobilizationPoints = this.getInteriorPoints();
		ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);

		loop:
		for (Vector3 position : mobilizationPoints)
		{
			if (position.getBlockID(this.worldObj) > 0)
			{
				if (Blacklist.forceManipulationBlacklist.contains(Block.blocksList[position.getBlockID(this.worldObj)]))
				{
					return false;
				}

				TileEntity tileEntity = position.getTileEntity(this.worldObj);

				if (tileEntity instanceof ISpecialForceManipulation)
				{
					if (!((ISpecialForceManipulation) tileEntity).preMove(position.intX(), position.intY(), position.intZ()))
					{
						return false;
					}
				}

				Vector3 targetPosition = position.clone().modifyPositionFromSide(dir);

				if (targetPosition.getTileEntity(this.worldObj) == this)
				{
					return false;
				}

				for (Vector3 checkPos : mobilizationPoints)
				{
					if (checkPos.equals(targetPosition))
					{
						continue loop;
					}
				}

				int blockID = targetPosition.getBlockID(this.worldObj);

				if (!(blockID == 0 || (blockID > 0 && (Block.blocksList[blockID].isBlockReplaceable(this.worldObj, targetPosition.intX(), targetPosition.intY(), targetPosition.intZ()) || Block.blocksList[blockID] instanceof BlockFluid))))
				{
					return false;
				}
			}
		}

		return true;
	}

	protected boolean moveBlock(Vector3 position, ForgeDirection direction)
	{
		if (!this.worldObj.isRemote)
		{
			Vector3 newPosition = position.clone().modifyPositionFromSide(direction);

			TileEntity tileEntity = position.getTileEntity(this.worldObj);
			int blockID = position.getBlockID(this.worldObj);

			if (blockID > 0 && tileEntity != this)
			{
				this.getDelayedEvents().add(new BlockPreMoveDelayedEvent(this, ANIMATION_TIME, this.worldObj, position, newPosition));
				return true;
			}
		}

		return false;
	}

	public void updatePushedObjects(float amount)
	{
		ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		AxisAlignedBB axisalignedbb = this.getSearchAxisAlignedBB();

		if (axisalignedbb != null)
		{
			@SuppressWarnings("unchecked")
			List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, axisalignedbb);

			for (Entity entity : entities)
			{
				entity.addVelocity(amount * dir.offsetX, amount * dir.offsetY, amount * dir.offsetZ);
			}
		}
	}

	public AxisAlignedBB getSearchAxisAlignedBB()
	{
		Vector3 positiveScale = new Vector3(this).add(this.getTranslation()).add(this.getPositiveScale());
		Vector3 negativeScale = new Vector3(this).add(this.getTranslation()).subtract(this.getNegativeScale());

		Vector3 minScale = new Vector3(Math.min(positiveScale.x, negativeScale.x), Math.min(positiveScale.y, negativeScale.y), Math.min(positiveScale.z, negativeScale.z));
		Vector3 maxScale = new Vector3(Math.max(positiveScale.x, negativeScale.x), Math.max(positiveScale.y, negativeScale.y), Math.max(positiveScale.z, negativeScale.z));

		return AxisAlignedBB.getAABBPool().getAABB(minScale.intX(), minScale.intY(), minScale.intZ(), maxScale.intX(), maxScale.intY(), maxScale.intZ());
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

	/**
	 * NBT Methods
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.anchor = Vector3.readFromNBT(nbt.getCompoundTag("anchor"));
		this.displayMode = nbt.getInteger("displayMode");
		this.doAnchor = nbt.getBoolean("doAnchor");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.anchor != null)
		{
			nbt.setCompoundTag("anchor", this.anchor.writeToNBT(new NBTTagCompound()));
		}

		nbt.setInteger("displayMode", this.displayMode);
		nbt.setBoolean("doAnchor", this.doAnchor);
	}

	@Override
	public Vector3 getTranslation()
	{
		return super.getTranslation().clone().add(this.anchor);
	}

	@Override
	public int getSizeInventory()
	{
		return 3 + 18;
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[] { "isActivate", "setActivate", "resetAnchor" };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception
	{
		switch (method)
		{
			case 2:
			{
				this.anchor = null;
				return null;
			}
		}

		return super.callMethod(computer, method, arguments);
	}
}
