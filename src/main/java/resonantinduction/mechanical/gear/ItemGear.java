package resonantinduction.mechanical.gear;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.part.ItemMultipartBase;
import resonantinduction.electrical.wire.EnumWireMaterial;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

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
			part.preparePlacement(side, itemStack.getItemDamage());
		}

		return part;
	}
}
