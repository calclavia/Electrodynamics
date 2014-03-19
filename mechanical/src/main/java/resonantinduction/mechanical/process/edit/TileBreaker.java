package resonantinduction.mechanical.process.edit;

import calclavia.lib.content.module.TileRender;
import calclavia.lib.content.module.prefab.TileInventory;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.render.RenderItemOverlayUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import org.lwjgl.opengl.GL11;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

import java.util.ArrayList;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class TileBreaker extends TileInventory implements IRotatable, IPacketReceiver
{
	public TileBreaker()
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

	public void work()
	{
		if (isIndirectlyPowered())
		{
			ForgeDirection dir = getDirection();
			Vector3 check = position().translate(dir);
			VectorWorld put = (VectorWorld) position().translate(dir.getOpposite());

			Block block = Block.blocksList[check.getBlockID(world())];

			if (block != null)
			{
				int candidateMeta = world().getBlockMetadata(check.intX(), check.intY(), check.intZ());
				boolean flag = true;

				ArrayList<ItemStack> drops = block.getBlockDropped(getWorldObj(), check.intX(), check.intY(), check.intZ(), candidateMeta, 0);

				for (ItemStack stack : drops)
				{
					if (!canInsertItem(0, stack, dir.ordinal() ^ 1))
					{
						flag = false;
					}
				}
				if (flag)
				{
					getWorldObj().destroyBlock(check.intX(), check.intY(), check.intZ(), false);

					for (ItemStack stack : drops)
					{
						InventoryUtility.putStackInInventory((VectorWorld) put.translate(0.5), stack, dir.ordinal(), false);
					}
				}
			}
		}
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return true;
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
					RenderItemOverlayUtility.renderItemOnSides(TileBreaker.this, getStackInSlot(0), position.x, position.y, position.z);
					GL11.glPopMatrix();
				}

				return false;
			}
		};
	}
}
