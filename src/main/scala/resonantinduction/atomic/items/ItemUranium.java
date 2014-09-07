package resonantinduction.atomic.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import resonant.lib.render.EnumColor;
import resonant.lib.utility.LanguageUtility;

import java.util.List;

/**
 * Uranium
 */
public class ItemUranium extends ItemRadioactive
{
	public ItemUranium()
	{
		super();
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		String tooltip = LanguageUtility.getLocal(getUnlocalizedName(itemStack) + ".tooltip");

		if (tooltip != null && tooltip.length() > 0)
		{
			if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				list.add(LanguageUtility.getLocal("tooltip.noShift").replace("%0", EnumColor.AQUA.toString()).replace("%1", EnumColor.GREY.toString()));
			}
			else
			{
				list.addAll(LanguageUtility.splitStringPerWord(tooltip, 5));
			}
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return this.getUnlocalizedName() + "." + itemStack.getItemDamage();
	}

	@Override
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List list)
	{
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
	}
}
