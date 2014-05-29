package resonantinduction.core.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;

public class ItemBakingTrayWithBread extends Item{
	
	public ItemBakingTrayWithBread(int par1) {
		super(par1);
		this.setCreativeTab(CreativeTabs.tabFood);
		this.setTextureName(Reference.DOMAIN + ":itemBakingTrayWithBread");
		this.setUnlocalizedName("itemBakingTrayWithBread");
	}
		
		public ItemStack getContainerItemStack(ItemStack itemStack)
		{
	       return new ItemStack(ResonantInduction.itemFlour,1,2);
	    }
		
		public boolean hasContainerItem()
	    {
	        return true;
	    }
		
}
