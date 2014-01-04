package resonantinduction.machine.crusher;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import resonantinduction.ResonantInduction;
import resonantinduction.core.base.ItemBase;
import calclavia.lib.Calclavia;
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
	public static final Set<String> ingots = new HashSet<String>();

	public ItemDust(int id)
	{
		super("dust", id);
		this.setTextureName("gunpowder");
	}

	@ForgeSubscribe
	public void oreRegisterEvent(OreRegisterEvent evt)
	{
		if (evt.Name.startsWith("ingot"))
		{
			String ingotName = evt.Name.replace("ingot", "");
			ingots.add(ingotName.toLowerCase());
		}
	}

	public static void postInit()
	{
		for (String ingotName : ingots)
		{
			String dustName = "dust" + ingotName.substring(0, 1).toUpperCase() + ingotName.substring(1);

			if (OreDictionary.getOres(dustName).size() > 0)
			{
				OreDictionary.registerOre(dustName, getStackFromDust(ingotName));
			}
		}
	}

	public static ItemStack getStackFromDust(String name)
	{
		ItemStack itemStack = new ItemStack(ResonantInduction.itemDust);
		NBTTagCompound nbt = Calclavia.getNBTTagCompound(itemStack);
		nbt.setString("name", name);
		return itemStack;
	}

	public static String getDustFromStack(ItemStack itemStack)
	{
		NBTTagCompound nbt = Calclavia.getNBTTagCompound(itemStack);

		if (nbt.hasKey("name"))
		{
			return nbt.getString("name");
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int par2)
	{
		/**
		 * Auto-color based on the texture of the ingot.
		 */
		return 16777215;
	}
}
