package resonantinduction.machine.crusher;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import resonantinduction.ResonantInduction;
import resonantinduction.core.base.ItemBase;
import calclavia.lib.Calclavia;
import calclavia.lib.prefab.TranslationHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
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
	public static final Set<String> ingotNames = new HashSet<String>();
	public static final Set<ItemStack> dusts = new HashSet<ItemStack>();
	public static final HashMap<String, Integer> ingotColors = new HashMap<String, Integer>();

	public ItemDust(int id)
	{
		super("dust", id);
		this.setTextureName(ResonantInduction.PREFIX + "dust");
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		String dustName = getDustFromStack(is);
		ItemStack type = OreDictionary.getOres("ingot" + dustName.substring(0, 1).toUpperCase() + dustName.substring(1)).get(0);

		String name = type.getDisplayName().replace(TranslationHelper.getLocal("misc.resonantinduction.ingot"), "");
		return (TranslationHelper.getLocal(this.getUnlocalizedName() + ".name")).replace("%v", name).replace("  ", " ");
	}

	@ForgeSubscribe
	public void oreRegisterEvent(OreRegisterEvent evt)
	{
		if (evt.Name.startsWith("ingot"))
		{
			String ingotName = evt.Name.replace("ingot", "");
			ingotNames.add(ingotName.toLowerCase());
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void reloadTextures(TextureStitchEvent.Post e)
	{
		computeColors();
	}

	public static void postInit()
	{
		for (String ingotName : ingotNames)
		{
			String name = ingotName.substring(0, 1).toUpperCase() + ingotName.substring(1);

			if (OreDictionary.getOres("dust" + name).size() == 0 && OreDictionary.getOres("ore" + name).size() > 0)
			{
				dusts.add(getStackFromDust(ingotName));
				OreDictionary.registerOre("dust" + name, getStackFromDust(ingotName));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void computeColors()
	{
		for (String ingotName : ingotNames)
		{
			LinkedList<Integer> colorCodes = new LinkedList<Integer>();

			// Compute color
			int totalR = 0;
			int totalG = 0;
			int totalB = 0;

			for (ItemStack ingotStack : OreDictionary.getOres("ingot" + ingotName.substring(0, 1).toUpperCase() + ingotName.substring(1)))
			{

				Item theIngot = ingotStack.getItem();

				Method o = ReflectionHelper.findMethod(Item.class, theIngot, new String[] { "getIconString", "func_" + "111208_A" });
				String iconString;
				try
				{
					iconString = (String) o.invoke(theIngot);
				}
				catch (ReflectiveOperationException e1)
				{
					//e1.printStackTrace();
					break;
				}

				ResourceLocation textureLocation = new ResourceLocation(iconString.replace(":", ":" + ResonantInduction.ITEM_TEXTURE_DIRECTORY) + ".png");
				InputStream inputstream;
				try
				{
					inputstream = Minecraft.getMinecraft().getResourceManager().getResource(textureLocation).getInputStream();

					BufferedImage bufferedimage = ImageIO.read(inputstream);

					int width = bufferedimage.getWidth();
					int height = bufferedimage.getWidth();

					for (int x = 0; x < width; x++)
					{
						for (int y = 0; y < height; y++)
						{
							colorCodes.add(bufferedimage.getRGB(x, y));
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (colorCodes.size() > 0)
			{
				for (int colorCode : colorCodes)
				{
					Color color = new Color(colorCode);

					if (color.getAlpha() != 0)
					{
						totalR += color.getRed();
						totalG += color.getGreen();
						totalB += color.getBlue();
					}
				}

				totalR /= colorCodes.size();
				totalG /= colorCodes.size();
				totalB /= colorCodes.size();

				int resultantColor = new Color(totalR, totalG, totalB).brighter().brighter().getRGB();
				ingotColors.put(ingotName, resultantColor);
			}
			if (!ingotColors.containsKey(ingotName))
			{
				ingotColors.put(ingotName, 0xFFFFFF);
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
		String name = this.getDustFromStack(itemStack);

		if (ingotColors.containsKey(name))
		{
			return ingotColors.get(name);
		}

		return 16777215;
	}
}
