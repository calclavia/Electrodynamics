package resonantinduction.core.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.OreDetectionBlackList;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.item.ItemDust;
import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.render.TextureUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class ResourceGenerator
{
	public static final ResourceGenerator INSTANCE = new ResourceGenerator();
	public static final Set<String> materialNames = new HashSet<String>();
	public static final HashMap<String, Integer> materialColors = new HashMap<String, Integer>();

	@ForgeSubscribe
	public void oreRegisterEvent(OreRegisterEvent evt)
	{
		if (evt.Name.startsWith("ingot"))
		{
			String ingotName = evt.Name.replace("ingot", "");

			if (OreDetectionBlackList.isIngotBlackListed("ingot" + ingotName) || OreDetectionBlackList.isOreBlackListed("ore" + ingotName))
				return;

			ResourceGenerator.materialNames.add(ingotName.toLowerCase());
		}
	}

	public static void generateDusts()
	{
		for (String materialName : materialNames)
		{
			String name = materialName.substring(0, 1).toUpperCase() + materialName.substring(1);

			if (OreDictionary.getOres("ore" + name).size() > 0)
			{
				// if (OreDictionary.getOres("dust" + name).size() == 0)
				{
					ItemDust.dusts.add(ResonantInduction.itemDust.getStackFromDust(materialName));
					OreDictionary.registerOre("dust" + name, ResonantInduction.itemDust.getStackFromDust(materialName));

				}

				// Add to machine recipes
				ItemStack dust = OreDictionary.getOres("dust" + name).get(0).copy();
				dust.stackSize = 2;
				MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, "ore" + name, dust);
			}
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void reloadTextures(TextureStitchEvent.Post e)
	{
		computeColors();
	}

	@SideOnly(Side.CLIENT)
	public static void computeColors()
	{
		for (String ingotName : materialNames)
		{
			// Compute color
			int totalR = 0;
			int totalG = 0;
			int totalB = 0;

			int colorCount = 0;

			for (ItemStack ingotStack : OreDictionary.getOres("ingot" + ingotName.substring(0, 1).toUpperCase() + ingotName.substring(1)))
			{
				Item theIngot = ingotStack.getItem();

				try
				{
					Icon icon = theIngot.getIconIndex(ingotStack);
					String iconString = icon.getIconName();

					if (iconString != null && !iconString.contains("MISSING_ICON_ITEM"))
					{
						System.out.println("LOAD:" + iconString);
						iconString = iconString.contains(":") ? iconString.replace(":", ":" + Reference.ITEM_TEXTURE_DIRECTORY) + ".png" : Reference.ITEM_TEXTURE_DIRECTORY + iconString;
						System.out.println(iconString);
						ResourceLocation textureLocation = new ResourceLocation(iconString);
						Colour[] colors = TextureUtils.loadTextureColours(textureLocation);

						for (Colour color : colors)
						{
							totalR += color.r;
							totalG += color.g;
							totalB += color.b;
							colorCount++;
						}
					}
				}
				catch (Exception e)
				{
					System.out.println("Failed to compute colors for: " + theIngot);
					e.printStackTrace();
				}
			}

			if (colorCount > 0)
			{
				totalR /= colorCount;
				totalG /= colorCount;
				totalB /= colorCount;
				int resultantColor = new ColourRGBA(totalR, totalG, totalB, 0).rgb();
				materialColors.put(ingotName, resultantColor);
			}

			if (!materialColors.containsKey(ingotName))
			{
				materialColors.put(ingotName, 0xFFFFFF);
			}
		}
	}
}
