package resonantinduction.mechanical.process.edit;

import calclavia.lib.content.module.TileRender;
import calclavia.lib.content.module.prefab.TileInventory;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.render.RenderItemOverlayUtility;
import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
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
	public TilePlacer()
	{
		super(Material.iron);
		normalRender = false;
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
	protected boolean use(EntityPlayer player, int hitSide, Vector3 hit)
	{
		interactCurrentItem(this, 0, player);
		return true;
	}

	public void work()
	{
		if (isIndirectlyPowered())
		{
			ForgeDirection dir = getDirection();
			Vector3 check = position().translate(dir);
			ItemStack placeStack = getStackInSlot(0);

			if (world().isAirBlock(check.intX(), check.intY(), check.intZ()) && placeStack != null && placeStack.getItem() instanceof ItemBlock)
			{
				ItemStack copyPlaceStack = placeStack.copy();
				decrStackSize(0, 1);
				((ItemBlock) copyPlaceStack.getItem()).placeBlockAt(placeStack, null, world(), check.intX(), check.intY(), check.intZ(), 0, 0, 0, 0, copyPlaceStack.getItemDamage());
			}
		}
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
					GL11.glPushMatrix();
					RenderItemOverlayUtility.renderItemOnSides(TilePlacer.this, getStackInSlot(0), position.x, position.y, position.z);
					GL11.glPopMatrix();
				}

				return false;
			}
		};
	}
}
