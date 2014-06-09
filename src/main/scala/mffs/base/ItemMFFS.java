package mffs.base;

import cpw.mods.fml.common.registry.GameRegistry;
import mffs.MFFSCreativeTab;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import resonant.lib.utility.LanguageUtility;

import java.util.List;

public class ItemMFFS extends Item
{
	public ItemMFFS(int id, String name)
	{
		super(Settings.configuration.getItem(name, id).getInt(id));
		this.setUnlocalizedName(ModularForceFieldSystem.PREFIX + name);
		this.setCreativeTab(MFFSCreativeTab.INSTANCE);
		this.setTextureName(ModularForceFieldSystem.PREFIX + name);
		this.setNoRepair();
		GameRegistry.registerItem(this, this.getUnlocalizedName(), ModularForceFieldSystem.ID);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		String tooltip = LanguageUtility.getLocal(this.getUnlocalizedName() + ".tooltip");

		if (tooltip != null && tooltip.length() > 0)
		{
			info.addAll(LanguageUtility.splitStringPerWord(tooltip, 5));
		}
	}
}