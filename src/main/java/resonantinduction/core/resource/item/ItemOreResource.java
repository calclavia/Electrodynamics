package resonantinduction.core.resource.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.core.prefab.item.ItemRI;
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
public class ItemOreResource extends ItemRI
{
	public ItemOreResource(int id, String name)
	{
		super(name, id);
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		String dustName = getDustFromStack(is);
		List<ItemStack> list = OreDictionary.getOres("ingot" + dustName.substring(0, 1).toUpperCase() + dustName.substring(1));

		if (list.size() > 0)
		{
			ItemStack type = list.get(0);

			String name = type.getDisplayName().replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" $", "");
			return (LanguageUtility.getLocal(this.getUnlocalizedName() + ".name")).replace("%v", name).replace("  ", " ");
		}

		return "";
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
		for (String materialName : ResourceGenerator.materialNames)
		{
			par3List.add(getStackFromDust(materialName));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int par2)
	{
		/**
		 * Auto-color based on the texture of the ingot.
		 */
		String name = ItemOreResource.getDustFromStack(itemStack);

		if (ResourceGenerator.materialColors.containsKey(name))
		{
			return ResourceGenerator.materialColors.get(name);
		}

		return 16777215;
	}
}
