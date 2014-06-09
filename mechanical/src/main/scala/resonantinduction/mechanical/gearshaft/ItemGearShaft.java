package resonantinduction.mechanical.gearshaft;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import resonantinduction.core.prefab.part.IHighlight;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

public class ItemGearShaft extends JItemMultiPart implements IHighlight
{
	public ItemGearShaft(int id)
	{
		super(id);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage();
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

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for (int i = 0; i < 3; i++)
		{
			listToAddTo.add(new ItemStack(itemID, 1, i));
		}
	}

	@Override
	public int getHighlightType()
	{
		return 0;
	}
}
