package resonantinduction.electrical.itemrailing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.MultipartUtility;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.ControlKeyModifer;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

/**
 * @author tgame14
 * @since 17/04/14
 */
public class ItemItemRailing extends JItemMultiPart
{
	public ItemItemRailing(int id)
	{
		super(id);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit)
	{
		BlockCoord onPos = pos.copy().offset(side ^ 1);

		if (player.isSneaking() && !ControlKeyModifer.isControlDown(player))
		{
			PartRailing railing = (PartRailing) MultiPartRegistry.createPart("resonant_induction_itemrailing", false);

			if (railing != null)
			{
				railing.preparePlacement(itemStack.getItemDamage());
			}

			return railing;
		}
		else
		{
			if (!MultipartUtility.canPlaceWireOnSide(world, onPos.x, onPos.y, onPos.z, ForgeDirection.getOrientation(side), false))
			{
				return null;
			}

			PartRailing railing = (PartRailing) MultiPartRegistry.createPart("resonant_induction_itemrailing", false);

			if (railing != null)
			{
				railing.preparePlacement(side);
			}

			return railing;
		}
	}

}
