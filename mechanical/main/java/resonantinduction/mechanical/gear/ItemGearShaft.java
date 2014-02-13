package resonantinduction.mechanical.gear;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemGearShaft extends JItemMultiPart
{
	public ItemGearShaft(int id)
	{
		super(id);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		PartGearShaft part = (PartGearShaft) MultiPartRegistry.createPart("resonant_induction_gear_shaft", false);

		if (part != null)
		{
			part.preparePlacement(side, itemStack.getItemDamage());
		}

		return part;
	}
}
