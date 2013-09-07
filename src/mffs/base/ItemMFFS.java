package mffs.base;

import java.util.List;

import mffs.MFFSCreativeTab;
import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.prefab.TranslationHelper;

public class ItemMFFS extends Item
{
	public ItemMFFS(int id, String name)
	{
		super(Settings.CONFIGURATION.getItem(name, id).getInt(id));
		this.setUnlocalizedName(ModularForceFieldSystem.PREFIX + name);
		this.setCreativeTab(MFFSCreativeTab.INSTANCE);
		this.setTextureName(ModularForceFieldSystem.PREFIX + name);
		this.setNoRepair();
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		String tooltip = TranslationHelper.getLocal(this.getUnlocalizedName() + ".tooltip");

		if (tooltip != null && tooltip.length() > 0)
		{
			info.addAll(MFFSHelper.splitStringPerWord(tooltip, 5));
		}
	}
}