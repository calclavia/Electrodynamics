package resonantinduction.core.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Derp
 * 
 * @author Zerotheliger
 * 
 */


public class ItemFlour extends Item {

	@SideOnly(Side.CLIENT)
	Icon doughIcon;

	/*@SideOnly(Side.CLIENT)
	Icon bakingtrayIcon;

	@SideOnly(Side.CLIENT)
	Icon bakingtraywithdoughIcon;*/


	public ItemFlour(int par1) {
		super(par1);
		this.setHasSubtypes(true);
	}

	@Override
	public Icon getIconFromDamage(int meta) {
		switch (meta) {
		case 1:
			return doughIcon;

		/*case 2:
			return bakingtrayIcon;

		case 3:
			return bakingtraywithdoughIcon;*/

		default:
			return super.getIconFromDamage(meta);

		}

	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		super.registerIcons(iconRegister);
		this.doughIcon = iconRegister.registerIcon(Reference.PREFIX + "dough");
		/*this.bakingtrayIcon = iconRegister.registerIcon(Reference.PREFIX + "bakingtray");
		this.bakingtraywithdoughIcon = iconRegister.registerIcon(Reference.PREFIX + "bakingtraywithdough");*/

		

	}

	public String getUnlocalizedName(ItemStack par1ItemStack) {
		switch (par1ItemStack.getItemDamage()) {

		case 1:
			return "item.dough";
		/*case 2:
			return "item.bakingtray";
		case 3:
			return "item.bakingtraywithdough";*/


		default:
			return super.getUnlocalizedName();

		}
	}

	@Override
	public void getSubItems(int par1, CreativeTabs tab, List items) {
		super.getSubItems(par1, tab, items);
		items.add(new ItemStack(par1, 1, 1));
		/*items.add(new ItemStack(par1, 1, 2));
		items.add(new ItemStack(par1, 1, 3));*/
	
	}
	
	}
