package resonantinduction.mechanical.gear;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.part.ItemMultipartBase;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.ControlKeyModifer;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class ItemGear extends ItemMultipartBase
{
	public ItemGear()
	{
		super("gear", Settings.getNextItemID());
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		PartGear part = (PartGear) MultiPartRegistry.createPart("resonant_induction_gear", false);

		if (part != null)
		{
			if (ControlKeyModifer.isControlDown(player))
				pos.offset(side ^ 1, -1);

			TileEntity tile = world.getBlockTileEntity(pos.x, pos.y, pos.z);

			if (tile instanceof TileMultipart)
			{
				if (!(((TileMultipart) tile).partMap(side) instanceof PartGear))
				{
					side = ForgeDirection.getOrientation(side).getOpposite().ordinal();
				}
			}

			part.preparePlacement(side, itemStack.getItemDamage());
		}

		return part;
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for (int i = 0; i < 3; i++)
		{
			listToAddTo.add(new ItemStack(itemID, 1, i));
		}
	}
}
