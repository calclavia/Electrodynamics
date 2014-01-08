package resonantinduction.energy.transformer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.base.ItemMultipartBase;
import resonantinduction.energy.wire.EnumWireMaterial;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemTransformer extends ItemMultipartBase
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemTransformer(int id)
	{
		super("transformer", id);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		PartTransformer part = (PartTransformer) MultiPartRegistry.createPart("resonant_induction_transformer", false);

		if (part != null)
		{
			part.preparePlacement(side, itemStack.getItemDamage());
		}

		return part;
	}
}
