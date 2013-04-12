package mffs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class MFFSCreativeTab extends CreativeTabs
{
	public static CreativeTabs INSTANCE = new MFFSCreativeTab(CreativeTabs.getNextID(), "MFFS");

	public MFFSCreativeTab(int par1, String par2Str)
	{
		super(par1, par2Str);
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return new ItemStack(ModularForceFieldSystem.blockFortronCapacitor);
	}
}