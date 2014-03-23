package resonantinduction.mechanical.process.edit;

import calclavia.lib.content.module.TileRender;
import calclavia.lib.content.module.prefab.TileInventory;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.render.RenderItemOverlayUtility;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.inventory.InternalInventoryHandler;
import calclavia.lib.utility.inventory.InventoryUtility;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class TilePlacer extends TileInventory implements IRotatable, IPacketReceiver
{
	@SideOnly(Side.CLIENT)
	private static Icon iconFront, iconBack;
	private boolean doWork = false;
	private boolean autoPullItems = false;
	private byte place_delay = 0;
	private InternalInventoryHandler invHandler;
	private ForgeDirection renderItemSideA;
	private ForgeDirection renderItemSideB;

	public TilePlacer()
	{
		super(Material.rock);
		normalRender = false;
		maxSlots = 1;
		rotationMask = Byte.parseByte("111111", 2);
	}

	public InternalInventoryHandler getInvHandler()
	{
		if (invHandler == null)
		{
			invHandler = new InternalInventoryHandler(this);
		}
		return invHandler;
	}

	@Override
	public void onAdded()
	{
		work();
	}

	@Override
	public void onNeighborChanged()
	{
		work();
	}

	@Override
	public void initiate()
	{
		super.initiate();
		updateDirection();
	}

	@Override
	public void updateEntity()
	{
		if (autoPullItems && this.ticks % 5 == 0)
		{
			if (getStackInSlot(0) == null)
			{
				this.setInventorySlotContents(0, this.getInvHandler().tryGrabFromPosition(this.getDirection().getOpposite(), 1));
			}
		}
		if (doWork)
		{
			if (place_delay < Byte.MAX_VALUE)
			{
				place_delay++;
			}

			if (place_delay >= 5)
			{
				doWork();
				doWork = false;
			}
		}

	}

	public void work()
	{
		if (isIndirectlyPowered())
		{
			doWork = true;
			place_delay = 0;
		}
	}

	public void doWork()
	{
		//Tries to place the item stack into the world
		int side = 0;
		Vector3 placePos = position().translate(getDirection());
		ItemStack placeStack = getStackInSlot(0);

		if (InventoryUtility.placeItemBlock(world(), placePos.intX(), placePos.intY(), placePos.intZ(), placeStack, side))
		{
			if (placeStack.stackSize <= 0)
			{
				setInventorySlotContents(0, null);
			}

			markUpdate();
			doWork = false;
		}
	}

	@Override
	protected boolean use(EntityPlayer player, int hitSide, Vector3 hit)
	{
		interactCurrentItem(this, 0, player);
		return true;
	}

	protected boolean configure(EntityPlayer player, int side, Vector3 hit)
	{
		if (player.isSneaking())
		{
			this.autoPullItems = !this.autoPullItems;
			player.sendChatToPlayer(ChatMessageComponent.createFromText("AutoExtract: " + this.autoPullItems));
			return true;
		}
		return super.configure(player, side, hit);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			readFromNBT(PacketHandler.readNBTTagCompound(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.autoPullItems = nbt.getBoolean("autoPull");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("autoPull", this.autoPullItems);
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		super.setDirection(direction);
		this.updateDirection();
	}

	@SuppressWarnings("incomplete-switch")
	public void updateDirection()
	{
		switch (this.getDirection())
		{
			case EAST:
			case WEST:
				this.renderItemSideA = ForgeDirection.NORTH;
				this.renderItemSideB = ForgeDirection.SOUTH;
				break;
			case NORTH:
			case SOUTH:
				this.renderItemSideA = ForgeDirection.EAST;
				this.renderItemSideB = ForgeDirection.WEST;
				break;

		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == meta)
		{
			return iconFront;
		}
		else if (ForgeDirection.getOrientation(meta).getOpposite().ordinal() == side)
		{
			return iconBack;
		}
		return super.getIcon(side, meta);
	}
	
	@Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        return side == this.getDirection().getOpposite() && slot == 0;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconFront = iconRegister.registerIcon(getTextureName() + "_front");
		iconBack = iconRegister.registerIcon(getTextureName() + "_back");
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected TileRender newRenderer()
	{
		return new TileRender()
		{
			@Override
			public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
			{
				if (!isItem)
				{
					if (TilePlacer.this.worldObj != null && (TilePlacer.this.renderItemSideA == null || TilePlacer.this.renderItemSideB == null))
					{
						TilePlacer.this.updateDirection();
					}
					GL11.glPushMatrix();
					RenderItemOverlayUtility.renderItemOnSides(TilePlacer.this, getStackInSlot(0), position.x, position.y, position.z, LanguageUtility.getLocal("tooltip.noOutput"), TilePlacer.this.renderItemSideA, TilePlacer.this.renderItemSideB);
					GL11.glPopMatrix();
				}

				return false;
			}
		};
	}
}
