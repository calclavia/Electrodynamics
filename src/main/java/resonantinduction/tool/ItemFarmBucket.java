package resonantinduction.tool;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import resonantinduction.transport.ResonantInductionTransport;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Bucket containing compost for farming
 * 
 * @author DarkGuardsman
 */
public class ItemFarmBucket extends ItemBucket
{
	public ItemFarmBucket(int itemID, int blockID)
	{
		super(ResonantInductionTransport.CONFIGURATION.getItem("Bucket", itemID).getInt(), blockID);
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setHasSubtypes(true);
		this.setContainerItem(Item.bucketEmpty);
		this.setUnlocalizedName("farmBucket");
		this.setTextureName(ResonantInductionTransport.PREFIX + "farmBucket");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(ResonantInductionTransport.PREFIX + "compostBucket");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIconFromDamage(int par1)
	{
		return this.itemIcon;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(this.itemID, 1, 0));
	}

}
