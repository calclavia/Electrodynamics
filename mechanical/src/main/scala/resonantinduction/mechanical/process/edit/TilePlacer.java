package resonantinduction.mechanical.process.edit;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonant.api.IRotatable;
import resonant.lib.content.module.TileRender;
import resonant.lib.content.module.prefab.TileInventory;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonant.lib.render.RenderItemOverlayUtility;
import resonant.lib.render.RotatedTextureRenderer;
import resonant.lib.utility.LanguageUtility;
import resonant.lib.utility.inventory.InternalInventoryHandler;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	private byte placeDelay = 0;
	private InternalInventoryHandler invHandler;

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
			if (placeDelay < Byte.MAX_VALUE)
			{
				placeDelay++;
			}

			if (placeDelay >= 5)
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
			placeDelay = 0;
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
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return side == this.getDirection().getOpposite() && slot == 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(IBlockAccess access, int side)
	{
		int meta = access.getBlockMetadata(x(), y(), z());

		if (side == meta)
		{
			return iconFront;
		}
		else if (side == (meta ^ 1))
		{
			return iconBack;
		}

		return getIcon();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == (meta ^ 1))
		{
			return iconFront;
		}
		else if (side == meta)
		{
			return iconBack;
		}

		return getIcon();
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
		return new RotatedTextureRenderer(this)
		{
			@Override
			public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
			{
				if (world() != null && !isItem)
				{
					EnumSet set = EnumSet.allOf(ForgeDirection.class);
					set.remove(getDirection());
					set.remove(getDirection().getOpposite());
					GL11.glPushMatrix();
					RenderItemOverlayUtility.renderItemOnSides(tile(), getStackInSlot(0), position.x, position.y, position.z, LanguageUtility.getLocal("tooltip.noOutput"), set);
					GL11.glPopMatrix();
				}

				return false;
			}
		};
	}
}
