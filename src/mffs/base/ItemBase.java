package mffs.base;

import mffs.MFFSCreativeTab;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import net.minecraft.item.Item;

public class ItemBase extends Item
{
	public ItemBase(int id, String name)
	{
		super(Settings.CONFIGURATION.getItem(name, id).getInt(id));
		this.setUnlocalizedName(ModularForceFieldSystem.PREFIX + name);
		this.setCreativeTab(MFFSCreativeTab.INSTANCE);
		this.setNoRepair();
	}
}