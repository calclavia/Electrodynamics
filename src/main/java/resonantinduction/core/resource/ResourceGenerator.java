package resonantinduction.core.resource;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.OreDetectionBlackList;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.fluid.BlockFluidMaterial;
import resonantinduction.core.resource.fluid.BlockFluidMixture;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class ResourceGenerator
{
	public static final ResourceGenerator INSTANCE = new ResourceGenerator();
	public static final Set<String> oreDictBlackList = new HashSet<String>();
	public static final List<String> materialNames = new ArrayList<String>();
	public static final HashMap<String, Integer> materialColors = new HashMap<String, Integer>();
	private static final HashMap<Icon, Integer> iconColorMap = new HashMap<Icon, Integer>();

	static
	{
		oreDictBlackList.add("ingotRefinedIron");
	}

	@ForgeSubscribe
	public void oreRegisterEvent(OreRegisterEvent evt)
	{
		if (evt.Name.startsWith("ingot") && !oreDictBlackList.contains(evt.Name))
		{
			String materialName = evt.Name.replace("ingot", "");

			if (OreDetectionBlackList.isIngotBlackListed("ingot" + materialName) || OreDetectionBlackList.isOreBlackListed("ore" + materialName))
				return;

			if (!materialNames.contains(materialName.toLowerCase()))
				materialNames.add(materialName.toLowerCase());
		}
	}

	public static void generateOreResources()
	{
		OreDictionary.registerOre("ingotGold", Item.ingotGold);
		OreDictionary.registerOre("ingotIron", Item.ingotIron);

		OreDictionary.registerOre("oreGold", Block.oreGold);
		OreDictionary.registerOre("oreIron", Block.oreIron);
		OreDictionary.registerOre("oreLapis", Block.oreLapis);

		for (String materialName : materialNames)
		{
			// Caps version of the name
			String nameCaps = materialName.substring(0, 1).toUpperCase() + materialName.substring(1);

			/**
			 * Generate molten fluids
			 */
			Fluid fluidMaterial = new Fluid("molten" + nameCaps);
			fluidMaterial.setDensity(7);
			fluidMaterial.setViscosity(5000);
			fluidMaterial.setTemperature(273 + 1538);
			FluidRegistry.registerFluid(fluidMaterial);
			Block blockFluidMaterial = new BlockFluidMaterial(fluidMaterial);
			GameRegistry.registerBlock(blockFluidMaterial, "molten" + nameCaps);
			ResonantInduction.blockFluidMaterials.add(blockFluidMaterial);

			/**
			 * Generate dust mixture fluids
			 */
			Fluid fluidMixture = new Fluid("mixture" + nameCaps);
			FluidRegistry.registerFluid(fluidMixture);
			Block blockFluidMixture = new BlockFluidMixture(fluidMixture);
			GameRegistry.registerBlock(blockFluidMixture, "mixture" + nameCaps);
			ResonantInduction.blockFluidMixtures.add(blockFluidMixture);

			if (OreDictionary.getOres("ore" + nameCaps).size() > 0)
			{
				OreDictionary.registerOre("dust" + nameCaps, ResonantInduction.itemDust.getStackFromMaterial(materialName));
				OreDictionary.registerOre("rubble" + nameCaps, ResonantInduction.itemRubble.getStackFromMaterial(materialName));
				OreDictionary.registerOre("dustRefined" + nameCaps, ResonantInduction.itemRefinedDust.getStackFromMaterial(materialName));

				MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER, "ore" + nameCaps, "rubble" + nameCaps);
				MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, "rubble" + nameCaps, "dust" + nameCaps, "dust" + nameCaps);
				MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER, "dust" + nameCaps, "dustRefined" + nameCaps);
				MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER, "dustRefined" + nameCaps, "ingot" + nameCaps);

				ItemStack dust = ResonantInduction.itemDust.getStackFromMaterial(materialName);
				FurnaceRecipes.smelting().addSmelting(dust.itemID, dust.getItemDamage(), OreDictionary.getOres("ingot" + nameCaps).get(0).copy(), 0.7f);
				ItemStack refinedDust = ResonantInduction.itemRefinedDust.getStackFromMaterial(materialName);
				ItemStack smeltResult = OreDictionary.getOres("ingot" + nameCaps).get(0).copy();
				smeltResult.stackSize = 2;
				FurnaceRecipes.smelting().addSmelting(refinedDust.itemID, refinedDust.getItemDamage(), smeltResult, 0.7f);
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
		for (String material : materialNames)
		{
			// Compute color
			int totalR = 0;
			int totalG = 0;
			int totalB = 0;

			int colorCount = 0;

			for (ItemStack ingotStack : OreDictionary.getOres("ingot" + material.substring(0, 1).toUpperCase() + material.substring(1)))
			{
				Item theIngot = ingotStack.getItem();
				materialColors.put(material, getAverageColor(ingotStack));
			}

			if (!materialColors.containsKey(material))
			{
				materialColors.put(material, 0xFFFFFF);
			}
		}
	}

	/**
	 * Gets the average color of this item.
	 * 
	 * @param itemStack
	 * @return The RGB hexadecimal color code.
	 */
	@SideOnly(Side.CLIENT)
	public static int getAverageColor(ItemStack itemStack)
	{
		int totalR = 0;
		int totalG = 0;
		int totalB = 0;

		int colorCount = 0;
		Item item = itemStack.getItem();

		try
		{
			Icon icon = item.getIconIndex(itemStack);

			if (iconColorMap.containsKey(icon))
			{
				return iconColorMap.get(icon);
			}

			String iconString = icon.getIconName();

			if (iconString != null && !iconString.contains("MISSING_ICON_ITEM"))
			{
				iconString = (iconString.contains(":") ? iconString.replace(":", ":" + Reference.ITEM_TEXTURE_DIRECTORY) : Reference.ITEM_TEXTURE_DIRECTORY + iconString) + ".png";
				ResourceLocation textureLocation = new ResourceLocation(iconString);

				InputStream inputstream = Minecraft.getMinecraft().getResourceManager().getResource(textureLocation).getInputStream();
				BufferedImage bufferedimage = ImageIO.read(inputstream);

				int width = bufferedimage.getWidth();
				int height = bufferedimage.getWidth();

				for (int x = 0; x < width; x++)
				{
					for (int y = 0; y < height; y++)
					{
						Color rgb = new Color(bufferedimage.getRGB(x, y));

						/**
						 * Ignore things that are too dark. Standard luma calculation.
						 */
						double luma = 0.2126 * rgb.getRed() + 0.7152 * rgb.getGreen() + 0.0722 * rgb.getBlue();

						if (luma > 40)
						{
							totalR += rgb.getRed();
							totalG += rgb.getGreen();
							totalB += rgb.getBlue();
							colorCount++;
						}
					}
				}
			}

			if (colorCount > 0)
			{
				totalR /= colorCount;
				totalG /= colorCount;
				totalB /= colorCount;
				int averageColor = new Color(totalR, totalG, totalB).brighter().getRGB();
				iconColorMap.put(icon, averageColor);
				return averageColor;
			}
		}
		catch (Exception e)
		{
			ResonantInduction.LOGGER.fine("Failed to compute colors for: " + item);
		}

		return 0xFFFFFF;
	}

	public static Block getFluidMaterial(String name)
	{
		return ResonantInduction.blockFluidMaterials.get((getID(name)));
	}

	public static int getID(String name)
	{
		if (!materialNames.contains(name))
		{
			ResonantInduction.LOGGER.severe("Trying to get invalid material name " + name);
			return 0;
		}

		return materialNames.indexOf(name);
	}

	public static String getName(int id)
	{
		return materialNames.size() > id ? materialNames.get(id) : null;
	}

	public static int getColor(String name)
	{
		if (name != null && materialColors.containsKey(name))
		{
			return materialColors.get(name);
		}
		return 0xFFFFFF;

	}
}
