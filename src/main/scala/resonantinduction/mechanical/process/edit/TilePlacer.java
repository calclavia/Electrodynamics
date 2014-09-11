package resonantinduction.mechanical.process.edit;

import java.util.EnumSet;

import com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonant.api.IRotatable;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonant.lib.render.RenderItemOverlayUtility;
import resonant.lib.utility.LanguageUtility;
import resonant.lib.utility.inventory.InternalInventoryHandler;
import resonant.lib.utility.inventory.InventoryUtility;
import universalelectricity.core.transform.vector.Vector3;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import resonant.lib.content.prefab.java.TileInventory;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class TilePlacer extends TileInventory implements IRotatable, IPacketReceiver
{
	@SideOnly(Side.CLIENT)
	private static IIcon iconFront, iconBack;
	private boolean doWork = false;
	private boolean autoPullItems = false;
	private byte placeDelay = 0;
	private InternalInventoryHandler invHandler;

	public TilePlacer()
	{
		super(Material.rock);
		setSizeInventory(1);
        normalRender(false);
        renderStaticBlock_$eq(true);
        this.rotationMask_$eq(Byte.parseByte("111111", 2));
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
	public void onNeighborChanged(Block block)
	{
		work();
	}

	@Override
	public void start()
	{
		super.start();
	}

	@Override
	public void update()
	{
        super.update();
		if (autoPullItems && this.ticks() % 5 == 0)
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
		Vector3 placePos = position().add(getDirection());
		ItemStack placeStack = getStackInSlot(0);

		if (InventoryUtility.placeItemBlock(world(), placePos.xi(), placePos.yi(), placePos.zi(), placeStack, side))
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
	public boolean use(EntityPlayer player, int hitSide, Vector3 hit)
	{
		interactCurrentItem(this, 0, player);
		return true;
	}

	public boolean configure(EntityPlayer player, int side, Vector3 hit)
	{
		if (player.isSneaking())
		{
			this.autoPullItems = !this.autoPullItems;
			player.addChatComponentMessage(new ChatComponentText("AutoExtract: " + this.autoPullItems));
			return true;
		}
		return super.configure(player, side, hit);
	}

	@Override
	public PacketTile getDescPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new PacketTile(this, nbt);
	}

    @Override
    public void onInventoryChanged()
    {
        sendPacket(getDescPacket());
    }

	@Override
	public void read(ByteBuf data, EntityPlayer player, PacketType type)
	{
		try
		{
			readFromNBT(ByteBufUtils.readTag(data));
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
	public IIcon getIcon(IBlockAccess access, int side)
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
	public IIcon getIcon(int side, int meta)
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
	public void registerIcons(IIconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconFront = iconRegister.registerIcon(getTextureName() + "_front");
		iconBack = iconRegister.registerIcon(getTextureName() + "_back");
	}


    @Override
    public void renderDynamic(Vector3 position, float frame, int pass)
    {
        if (world() != null)
        {
            EnumSet set = EnumSet.allOf(ForgeDirection.class);
            set.remove(getDirection());
            set.remove(getDirection().getOpposite());
            set.remove(ForgeDirection.UP);
            set.remove(ForgeDirection.DOWN);
            GL11.glPushMatrix();
            RenderItemOverlayUtility.renderItemOnSides(this,  getStackInSlot(0), position.x(), position.y(), position.z(), LanguageUtility.getLocal("tooltip.noOutput"), set);
            GL11.glPopMatrix();
        }
    }
}
