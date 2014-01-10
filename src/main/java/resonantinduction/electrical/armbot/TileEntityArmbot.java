package resonantinduction.electrical.armbot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import resonantinduction.electrical.armbot.command.TaskDrop;
import resonantinduction.electrical.armbot.command.TaskGOTO;
import resonantinduction.electrical.armbot.command.TaskGrabItem;
import resonantinduction.electrical.armbot.command.TaskReturn;
import resonantinduction.electrical.armbot.command.TaskRotateTo;
import resonantinduction.old.api.IArmbot;
import resonantinduction.old.api.coding.IProgram;
import resonantinduction.old.api.coding.ProgramHelper;
import resonantinduction.old.core.recipe.RecipeLoader;
import resonantinduction.old.transport.ResonantInductionTransport;
import resonantinduction.old.transport.TileEntityAssembly;
import resonantinduction.old.transport.encoder.ItemDisk;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.multiblock.link.IBlockActivate;
import calclavia.lib.multiblock.link.IMultiBlock;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.utility.HelperMethods;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.MathUtility;

import com.builtbroken.common.Pair;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class TileEntityArmbot extends TileEntityAssembly implements IMultiBlock, IArmbot, IBlockActivate
{
	protected int ROTATION_SPEED = 6;

	/** The rotation of the arms. In Degrees. */
	protected int targetPitch = 0, targetYaw = 0;
	protected int actualPitch = 0, actualYaw = 0;

	protected boolean spawnEntity = false;

	protected String displayText = "";

	/** An entity that the Armbot is grabbed onto. Entity Items are held separately. */
	protected Object grabbedObject = null;
	/** Helper class that does all the logic for the armbot's program */
	protected ProgramHelper programHelper;
	/** Cached location of the armbot to feed to program tasks */
	protected Pair<World, Vector3> location;
	/** Var used by the armbot renderer */
	public EntityItem renderEntityItem;

	public TileEntityArmbot()
	{
		super(20);
		programHelper = new ProgramHelper(this).setMemoryLimit(20);
		Program program = new Program();
		program.setTaskAt(0, 0, new TaskDrop());
		program.setTaskAt(0, 1, new TaskRotateTo(180, 0));
		program.setTaskAt(0, 2, new TaskGrabItem());
		program.setTaskAt(0, 3, new TaskReturn());
		program.setTaskAt(0, 4, new TaskGOTO(0, 0));
		programHelper.setProgram(program);
	}

	/************************************ Armbot logic update methods *************************************/

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		Vector3 handPosition = this.getHandPos();
		if (this.location == null || !this.location.left().equals(this.worldObj) || this.xCoord != this.location.right().intX() || this.yCoord != this.location.right().intY() || this.zCoord != this.location.right().intZ())
		{
			this.location = new Pair<World, Vector3>(this.worldObj, new Vector3(this));
		}
		if (this.grabbedObject instanceof Entity)
		{
			if (this.spawnEntity)
			{
				this.worldObj.spawnEntityInWorld((Entity) this.grabbedObject);
				this.spawnEntity = false;
			}
			((Entity) this.grabbedObject).setPosition(handPosition.x, handPosition.y, handPosition.z);
			((Entity) this.grabbedObject).motionX = 0;
			((Entity) this.grabbedObject).motionY = 0;
			((Entity) this.grabbedObject).motionZ = 0;

			if (this.grabbedObject instanceof EntityItem)
			{
				((EntityItem) this.grabbedObject).delayBeforeCanPickup = 20;
				((EntityItem) this.grabbedObject).age = 0;
			}
		}

		if (this.isFunctioning())
		{
			float preYaw = this.targetYaw, prePitch = this.targetPitch;
			if (!this.worldObj.isRemote && this.ticks % 5 == 0)
			{
				this.programHelper.onUpdate(this.worldObj, new Vector3(this));
				if (this.targetYaw != preYaw || this.targetPitch != prePitch)
				{
					PacketHandler.sendPacketToClients(this.getDescriptionPacket(), worldObj, new Vector3(this).translate(new Vector3(.5f, 1f, .5f)), 64);
				}
			}
			this.updateRotation();
		}
	}

	public void updateRotation()
	{
		// Clamp target angles
		this.targetYaw = (int) MathUtility.clampAngleTo360(this.targetYaw);
		if (this.targetPitch < 0)
			this.targetPitch = 0;
		if (this.targetPitch > 60)
			this.targetPitch = 60;
		// Handle change in yaw rotation
		if (Math.abs(this.actualYaw - this.targetYaw) > 1)
		{
			float speedYaw;
			if (this.actualYaw > this.targetYaw)
			{
				if (Math.abs(this.actualYaw - this.targetYaw) >= 180)
				{
					speedYaw = this.ROTATION_SPEED;
				}
				else
				{
					speedYaw = -this.ROTATION_SPEED;
				}
			}
			else
			{
				if (Math.abs(this.actualYaw - this.targetYaw) >= 180)
				{
					speedYaw = -this.ROTATION_SPEED;
				}
				else
				{
					speedYaw = this.ROTATION_SPEED;
				}
			}

			this.actualYaw += speedYaw;

			if (Math.abs(this.actualYaw - this.targetYaw) < this.ROTATION_SPEED)
			{
				this.actualYaw = this.targetYaw;
			}
			this.playRotationSound();
		}
		// Handle change in pitch rotation
		if (Math.abs(this.actualPitch - this.targetPitch) > 1)
		{
			if (this.actualPitch > this.targetPitch)
			{
				this.actualPitch -= this.ROTATION_SPEED;
			}
			else
			{
				this.actualPitch += this.ROTATION_SPEED;
			}

			if (Math.abs(this.actualPitch - this.targetPitch) < this.ROTATION_SPEED)
			{
				this.actualPitch = this.targetPitch;
			}
			this.playRotationSound();
		}
		// Clamp actual angles angles
		this.actualYaw = (int) MathUtility.clampAngleTo360(this.actualYaw);
		if (this.actualPitch < 0)
			this.actualPitch = 0;
		if (this.actualPitch > 60)
			this.actualPitch = 60;
	}

	public void playRotationSound()
	{
		if (this.ticks % 5 == 0 && this.worldObj.isRemote)
		{
			this.worldObj.playSound(this.xCoord, this.yCoord, this.zCoord, "mods.assemblyline.conveyor", 2f, 2.5f, true);
		}
	}

	@Override
	public String getInvName()
	{
		return LanguageUtility.getLocal("tile.armbot.name");
	}

	public String getCommandDisplayText()
	{
		return this.displayText;
	}

	@Override
	public boolean onActivated(EntityPlayer player)
	{
		ItemStack containingStack = this.getStackInSlot(0);

		if (containingStack != null)
		{
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			{
				EntityItem dropStack = new EntityItem(this.worldObj, player.posX, player.posY, player.posZ, containingStack);
				dropStack.delayBeforeCanPickup = 0;
				this.worldObj.spawnEntityInWorld(dropStack);
			}

			this.setInventorySlotContents(0, null);
			return true;
		}
		else
		{
			if (player.getCurrentEquippedItem() != null)
			{
				if (player.getCurrentEquippedItem().getItem() instanceof ItemDisk)
				{
					this.setInventorySlotContents(0, player.getCurrentEquippedItem());
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					return true;
				}
			}
		}

		return false;
	}

	/************************************ Save and load code *************************************/

	/** NBT Data */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		this.targetYaw = nbt.getInteger("armYaw");
		this.targetPitch = nbt.getInteger("armPitch");
		this.actualYaw = nbt.getInteger("armYawActual");
		this.actualPitch = nbt.getInteger("armPitchActual");

		if (nbt.hasKey("grabbedEntity"))
		{
			NBTTagCompound tag = nbt.getCompoundTag("grabbedEntity");
			Entity entity = EntityList.createEntityFromNBT(tag, worldObj);
			if (entity != null)
			{
				this.grabbedObject = entity;
				this.spawnEntity = true;
			}
		}
		else if (nbt.hasKey("grabbedItem"))
		{
			ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("grabbedItem"));
			if (stack != null)
			{
				this.grabbedObject = stack;
			}
		}
	}

	/** Writes a tile entity to NBT. */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setInteger("armYaw", this.targetYaw);
		nbt.setInteger("armPitch", this.targetPitch);
		nbt.setInteger("armYawActual", this.actualYaw);
		nbt.setInteger("armPitchActual", this.actualPitch);

		if (this.grabbedObject instanceof Entity)
		{
			NBTTagCompound entityNBT = new NBTTagCompound();
			((Entity) this.grabbedObject).writeToNBT(entityNBT);
			((Entity) this.grabbedObject).writeToNBTOptional(entityNBT);
			nbt.setCompoundTag("grabbedEntity", entityNBT);
		}
		else if (this.grabbedObject instanceof ItemStack)
		{
			nbt.setCompoundTag("grabbedItem", ((ItemStack) this.grabbedObject).writeToNBT(new NBTTagCompound()));
		}

	}

	/************************************ Network Packet code *************************************/

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_TILE.getPacket(this, "armbot", this.functioning, this.targetYaw, this.targetPitch, this.actualYaw, this.actualPitch);
	}

	public void sendGrabItemToClient()
	{
		if (this.grabbedObject instanceof ItemStack)
		{
			PacketHandler.sendPacketToClients(ResonantInductionTransport.getTilePacket().getPacket(this, "armbotItem", true, ((ItemStack) this.grabbedObject).writeToNBT(new NBTTagCompound())), worldObj, new Vector3(this), 64);
		}
		else
		{
			PacketHandler.sendPacketToClients(ResonantInductionTransport.getTilePacket().getPacket(this, "armbotItem", false), worldObj, new Vector3(this), 64);
		}
	}

	@Override
	public boolean simplePacket(String id, ByteArrayDataInput dis, Player player)
	{
		try
		{
			if (this.worldObj.isRemote && !super.simplePacket(id, dis, player))
			{
				if (id.equalsIgnoreCase("armbot"))
				{
					this.functioning = dis.readBoolean();
					this.targetYaw = dis.readInt();
					this.targetPitch = dis.readInt();
					this.actualYaw = dis.readInt();
					this.actualPitch = dis.readInt();
					return true;
				}
				else if (id.equalsIgnoreCase("armbotItem"))
				{
					if (dis.readBoolean())
					{
						this.grabbedObject = ItemStack.loadItemStackFromNBT(PacketHandler.readNBTTagCompound(dis));
					}
					else
					{
						this.grabbedObject = null;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/************************************ Multi Block code *************************************/

	@Override
	public Vector3[] getMultiBlockVectors()
	{
		return new Vector3[] { new Vector3(this).translate(0, 1, 0) };
	}

	/************************************ Armbot API methods *************************************/
	@Override
	public Object getHeldObject()
	{
		return this.grabbedObject;
	}

	@Override
	public boolean grabObject(Object entity)
	{
		if (this.getHeldObject() == null)
		{
			if (entity instanceof ItemStack)
			{
				this.grabbedObject = entity;
				this.sendGrabItemToClient();
				return true;
			}
			else if (entity instanceof EntityItem)
			{
				this.grabbedObject = ((EntityItem) entity).getEntityItem();
				((EntityItem) entity).setDead();
				this.sendGrabItemToClient();
				return true;
			}
			else if (entity instanceof Entity)
			{
				this.grabbedObject = entity;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean dropHeldObject()
	{
		if (this.getHeldObject() != null)
		{
			if (this.getHeldObject() instanceof ItemStack)
			{
				Vector3 handPosition = this.getHandPos();
				HelperMethods.dropItemStack(worldObj, handPosition, (ItemStack) this.getHeldObject(), false);
			}
			this.grabbedObject = null;
			this.sendGrabItemToClient();
			return true;
		}
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return itemstack != null && itemstack.itemID == RecipeLoader.itemDisk.itemID;
	}

	@Override
	public Vector3 getHandPos()
	{
		Vector3 position = new Vector3(this);
		position.translate(0.5);
		position.translate(this.getDeltaHandPosition());
		return position;
	}

	public Vector3 getDeltaHandPosition()
	{
		// The distance of the position relative to the main position.
		double distance = 1f;
		Vector3 delta = new Vector3();
		// The delta Y of the hand.
		delta.y = Math.sin(Math.toRadians(this.actualPitch)) * distance * 2;
		// The horizontal delta of the hand.
		double dH = Math.cos(Math.toRadians(this.actualPitch)) * distance;
		// The delta X and Z.
		delta.x = Math.sin(Math.toRadians(-this.actualYaw)) * dH;
		delta.z = Math.cos(Math.toRadians(-this.actualYaw)) * dH;
		return delta;
	}

	@Override
	public Vector2 getRotation()
	{
		return new Vector2(this.actualYaw, this.actualPitch);
	}

	@Override
	public void setRotation(int yaw, int pitch)
	{
		if (!this.worldObj.isRemote)
		{
			this.actualYaw = yaw;
			this.actualPitch = pitch;
		}
	}

	@Override
	public boolean moveArmTo(int yaw, int pitch)
	{
		if (!this.worldObj.isRemote)
		{
			this.targetYaw = yaw;
			this.targetPitch = pitch;
			return true;
		}
		return false;
	}

	@Override
	public boolean moveTo(ForgeDirection direction)
	{
		if (direction == ForgeDirection.SOUTH)
		{
			this.targetYaw = 0;
			return true;
		}
		else if (direction == ForgeDirection.EAST)
		{
			this.targetYaw = 90;
			return true;
		}
		else if (direction == ForgeDirection.NORTH)
		{

			this.targetYaw = 180;
			return true;
		}
		else if (direction == ForgeDirection.WEST)
		{
			this.targetYaw = 270;
			return true;
		}
		return false;
	}

	@Override
	public IProgram getCurrentProgram()
	{
		if (this.programHelper == null)
		{
			this.programHelper = new ProgramHelper(this);
		}
		if (this.programHelper != null)
		{
			return this.programHelper.getProgram();
		}
		return null;
	}

	@Override
	public void setCurrentProgram(IProgram program)
	{
		if (this.programHelper == null)
		{
			this.programHelper = new ProgramHelper(this);
		}
		if (this.programHelper != null)
		{
			this.programHelper.setProgram(program);
		}
	}

	@Override
	public boolean clear(Object object)
	{
		if (this.grabbedObject != null && this.grabbedObject.equals(object))
		{
			this.grabbedObject = null;
			return true;
		}
		return false;
	}

	@Override
	public Pair<World, Vector3> getLocation()
	{
		return this.location;
	}
}
