package resonantinduction.core.resource;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.old.Reference;
import resonantinduction.old.mechanics.item.ItemDust;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class ResourceGenerator
{
	public static final Set<String> materialNames = new HashSet<String>();
	public static final HashMap<String, Integer> materialColors = new HashMap<String, Integer>();

	public static void generateDusts()
	{
		for (String materialName : ResourceGenerator.materialNames)
		{
			String name = materialName.substring(0, 1).toUpperCase() + materialName.substring(1);

			if (OreDictionary.getOres("ore" + name).size() > 0)
			{
				if (OreDictionary.getOres("dust" + name).size() == 0)
				{
					ItemDust.dusts.add(ItemDust.getStackFromDust(materialName));
					OreDictionary.registerOre("dust" + name, ItemDust.getStackFromDust(materialName));

				}

				// Add to machine recipes

				ItemStack dust = OreDictionary.getOres("dust" + name).get(0).copy();
				dust.stackSize = 2;
				MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, "ore" + name, dust);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void computeColors()
	{
		for (String ingotName : materialNames)
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
					// e1.printStackTrace();
					break;
				}

				ResourceLocation textureLocation = new ResourceLocation(iconString.replace(":", ":" + Reference.ITEM_TEXTURE_DIRECTORY) + ".png");
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
					// e.printStackTrace();
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
				materialColors.put(ingotName, resultantColor);
			}
			if (!materialColors.containsKey(ingotName))
			{
				materialColors.put(ingotName, 0xFFFFFF);
			}
		}
	}
}
