package resonantinduction.machine.crusher;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
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
	public static final Set<ItemStack> dusts = new HashSet<ItemStack>();

	public ItemDust(int id)
	{
		super("dust", id);
		this.setTextureName("gunpowder");
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		String dustName = getDustFromStack(itemStack);
		par3List.add("Type: " + dustName.substring(0, 1).toUpperCase() + dustName.substring(1));
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
				dusts.add(getStackFromDust(ingotName));
				OreDictionary.registerOre(dustName, getStackFromDust(ingotName));

				// Compute color
				int totalR = 0;
				int totalG = 0;
				int totalB = 0;

				ArrayList<Integer> colorCodes = new ArrayList<Integer>();

				if (colorCodes.size() > 0)
				{
					for (int colorCode : colorCodes)
					{
						Color color = new Color(colorCode);
						totalR += color.getRed();
						totalG += color.getGreen();
						totalB += color.getBlue();
					}

					totalR /= colorCodes.size();
					totalG /= colorCodes.size();
					totalB /= colorCodes.size();

					int resultantColor = new Color(totalR, totalG, totalB).getRGB();
				}
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

	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (ItemStack dust : dusts)
		{
			par3List.add(dust);
		}
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
