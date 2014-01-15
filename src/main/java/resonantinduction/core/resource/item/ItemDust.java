package resonantinduction.core.resource.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.item.ItemBase;
import resonantinduction.core.resource.ResourceGenerator;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.nbt.NBTUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * An item used for auto-generated dusts based on registered ingots in the OreDict.
 * 
 * @author Calclavia
 * 
 */
public class ItemDust extends ItemBase
{
	public static final Set<ItemStack> dusts = new HashSet<ItemStack>();

	public ItemDust(int id)
	{
		super("oreDust", id);
		this.setTextureName(Reference.PREFIX + "oreDust");
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		String dustName = getDustFromStack(is);
		ItemStack type = OreDictionary.getOres("ingot" + dustName.substring(0, 1).toUpperCase() + dustName.substring(1)).get(0);

		String name = type.getDisplayName().replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" $", "");
		return (LanguageUtility.getLocal(this.getUnlocalizedName() + ".name")).replace("%v", name).replace("  ", " ");
	}

	public ItemStack getStackFromDust(String name)
	{
		ItemStack itemStack = new ItemStack(this);
		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
		nbt.setString("name", name);
		return itemStack;
	}

	public static String getDustFromStack(ItemStack itemStack)
	{
		NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);

		if (nbt.hasKey("name"))
		{
			return nbt.getString("name");
		}

		return null;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (ItemStack dust : dusts)
		{
			par3List.add(dust);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int par2)
	{
		/**
		 * Auto-color based on the texture of the ingot.
		 */
		String name = ItemDust.getDustFromStack(itemStack);

		if (ResourceGenerator.materialColors.containsKey(name))
		{
			return ResourceGenerator.materialColors.get(name);
		}

		return 16777215;
	}
}
