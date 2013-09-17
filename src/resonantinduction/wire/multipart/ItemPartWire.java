package resonantinduction.wire.multipart;

import resonantinduction.ResonantInduction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;

public class ItemPartWire extends JItemMultiPart
{
	public ItemPartWire(int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_ITEM, "wireMultipart", id).getInt(id));
	}

	@Override
	public TMultiPart newPart(ItemStack arg0, EntityPlayer arg1, World arg2, BlockCoord arg3, int arg4, Vector3 arg5)
	{
		return new PartWire();
	}

}
