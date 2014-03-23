package resonantinduction.electrical.transformer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import resonantinduction.core.prefab.part.IHighlight;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FacePlacementGrid$;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemTransformer extends JItemMultiPart implements IHighlight
{
	public ItemTransformer(int id)
	{
		super(id);
	}

	@Override
	public TMultiPart newPart(ItemStack itemStack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 hit)
	{
		side = FacePlacementGrid$.MODULE$.getHitSlot(hit, side);
		PartTransformer part = (PartTransformer) MultiPartRegistry.createPart("resonant_induction_transformer", false);

		if (part != null)
		{
			int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int facing = l == 0 ? 2 : (l == 1 ? 5 : (l == 2 ? 3 : (l == 3 ? 4 : 0)));
			part.preparePlacement(side, facing);
		}

		return part;
	}

	@Override
	public int getHighlightType()
	{
		return 0;
	}
}
