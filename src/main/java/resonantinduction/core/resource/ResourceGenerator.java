package resonantinduction.core.resource;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.OreDetectionBlackList;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.fluid.FluidColored;
import resonantinduction.core.resource.fluid.BlockFluidMaterial;
import resonantinduction.core.resource.fluid.BlockFluidMixture;
import calclavia.lib.configurable.Config;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.nbt.IVirtualObject;
import calclavia.lib.utility.nbt.NBTUtility;
import calclavia.lib.utility.nbt.SaveManager;

import com.google.common.collect.HashBiMap;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** @author Calclavia */
public class ResourceGenerator implements IVirtualObject
{
	public static final ResourceGenerator INSTANCE = new ResourceGenerator();

	@Config(comment = " Allow the Resource Generator to make ore dictionary compatible recipes?")
	private static boolean allowOreDictCompatibility = true;

	/**
	 * A list of material names. They are all camelCase reference of ore dictionary names without
	 * the "ore" or "ingot" prefix.
	 * 
	 * Name, ID
	 */
	static int maxID = 0;
	static final HashBiMap<String, Integer> materials = HashBiMap.create();

	static final HashMap<String, Integer> materialColorCache = new HashMap<String, Integer>();
	static final HashMap<Icon, Integer> iconColorCache = new HashMap<Icon, Integer>();

	static
	{
		OreDetectionBlackList.addIngot("ingotRefinedIron");
		OreDetectionBlackList.addIngot("uranium");
		SaveManager.registerClass("resourceGenerator", ResourceGenerator.class);
		SaveManager.register(INSTANCE);
	}

	@ForgeSubscribe
	public void oreRegisterEvent(OreRegisterEvent evt)
	{
		if (evt.Name.startsWith("ingot"))
		{
			String oreDictName = evt.Name.replace("ingot", "");
			String materialName = LanguageUtility.decapitalizeFirst(oreDictName);

			if (!materials.containsKey(materialName))
			{
				Settings.CONFIGURATION.load();
				boolean allowMaterial = Settings.CONFIGURATION.get("Resource_Generator", "Enable " + oreDictName, true).getBoolean(true);
				Settings.CONFIGURATION.save();

				if (!allowMaterial || OreDetectionBlackList.isIngotBlackListed("ingot" + oreDictName) || OreDetectionBlackList.isOreBlackListed("ore" + oreDictName))
					return;

				materials.put(materialName, maxID++);
			}
		}
	}

	public static void generateOreResources()
	{
		OreDictionary.registerOre("ingotGold", Item.ingotGold);
		OreDictionary.registerOre("ingotIron", Item.ingotIron);

		OreDictionary.registerOre("oreGold", Block.oreGold);
		OreDictionary.registerOre("oreIron", Block.oreIron);
		OreDictionary.registerOre("oreLapis", Block.oreLapis);
		regenerateOreResources();
	}

	public static void regenerateOreResources()
	{
		// Vanilla fluid recipes
		MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Block.stone));

		// Vanilla crusher recipes
		MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER, Block.cobblestone, Block.gravel);
		MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER, Block.stone, Block.cobblestone);
		MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER, Block.chest, new ItemStack(Block.planks, 7, 0));

		// Vanilla grinder recipes
		MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, Block.cobblestone, Block.sand);
		MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, Block.gravel, Block.sand);
		MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, Block.glass, Block.sand);

		for (String materialName : materials.keySet())
		{
			// Caps version of the name
			String nameCaps = LanguageUtility.capitalizeFirst(materialName);
			String localizedName = materialName;

			List<ItemStack> list = OreDictionary.getOres("ingot" + materialName.substring(0, 1).toUpperCase() + materialName.substring(1));

			if (list.size() > 0)
			{
				ItemStack type = list.get(0);
				localizedName = type.getDisplayName();

				if (LanguageUtility.getLocal(localizedName) != null && LanguageUtility.getLocal(localizedName) != "")
				{
					localizedName = LanguageUtility.getLocal(localizedName);
				}

				localizedName.replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" $", "");
			}

			/** Generate molten fluids */
			FluidColored fluidMolten = new FluidColored(materialNameToMolten(materialName));
			fluidMolten.setDensity(7);
			fluidMolten.setViscosity(5000);
			fluidMolten.setTemperature(273 + 1538);
			FluidRegistry.registerFluid(fluidMolten);
			LanguageRegistry.instance().addStringLocalization(fluidMolten.getUnlocalizedName(), LanguageUtility.getLocal("tooltip.molten") + " " + localizedName);
			BlockFluidMaterial blockFluidMaterial = new BlockFluidMaterial(fluidMolten);
			GameRegistry.registerBlock(blockFluidMaterial, "molten" + nameCaps);
			ResonantInduction.blockMoltenFluid.put(getID(materialName), blockFluidMaterial);
			FluidContainerRegistry.registerFluidContainer(fluidMolten, ResonantInduction.itemBucketMolten.getStackFromMaterial(materialName));

			/** Generate dust mixture fluids */
			FluidColored fluidMixture = new FluidColored(materialNameToMixture(materialName));
			FluidRegistry.registerFluid(fluidMixture);
			BlockFluidMixture blockFluidMixture = new BlockFluidMixture(fluidMixture);
			LanguageRegistry.instance().addStringLocalization(fluidMixture.getUnlocalizedName(), localizedName + " " + LanguageUtility.getLocal("tooltip.mixture"));
			GameRegistry.registerBlock(blockFluidMixture, "mixture" + nameCaps);
			ResonantInduction.blockMixtureFluids.put(getID(materialName), blockFluidMixture);
			FluidContainerRegistry.registerFluidContainer(fluidMixture, ResonantInduction.itemBucketMixture.getStackFromMaterial(materialName));

			ItemStack dust = ResonantInduction.itemDust.getStackFromMaterial(materialName);
			ItemStack rubble = ResonantInduction.itemRubble.getStackFromMaterial(materialName);
			ItemStack refinedDust = ResonantInduction.itemRefinedDust.getStackFromMaterial(materialName);

			if (allowOreDictCompatibility)
			{
				OreDictionary.registerOre("dust" + nameCaps, ResonantInduction.itemDust.getStackFromMaterial(materialName));
				OreDictionary.registerOre("rubble" + nameCaps, ResonantInduction.itemRubble.getStackFromMaterial(materialName));
				OreDictionary.registerOre("dustRefined" + nameCaps, ResonantInduction.itemRefinedDust.getStackFromMaterial(materialName));

				MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, "rubble" + nameCaps, dust, dust);
				MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER, "dust" + nameCaps, refinedDust);
				MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER, new FluidStack(fluidMolten, FluidContainerRegistry.BUCKET_VOLUME), "ingot" + nameCaps);
			}
			else
			{
				MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER, rubble, dust, dust);
				MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER, dust, refinedDust);
				MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER, new FluidStack(fluidMolten, FluidContainerRegistry.BUCKET_VOLUME), "ingot" + nameCaps);
			}

			FurnaceRecipes.smelting().addSmelting(dust.itemID, dust.getItemDamage(), OreDictionary.getOres("ingot" + nameCaps).get(0).copy(), 0.7f);
			ItemStack smeltResult = OreDictionary.getOres("ingot" + nameCaps).get(0).copy();
			FurnaceRecipes.smelting().addSmelting(refinedDust.itemID, refinedDust.getItemDamage(), smeltResult, 0.7f);

			if (OreDictionary.getOres("ore" + nameCaps).size() > 0)
			{
				MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER, "ore" + nameCaps, "rubble" + nameCaps);
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
		for (String material : materials.keySet())
		{
			// Compute color
			int totalR = 0;
			int totalG = 0;
			int totalB = 0;

			int colorCount = 0;

			for (ItemStack ingotStack : OreDictionary.getOres("ingot" + LanguageUtility.capitalizeFirst(material)))
			{
				Item theIngot = ingotStack.getItem();
				int color = getAverageColor(ingotStack);
				materialColorCache.put(material, color);
			}

			if (!materialColorCache.containsKey(material))
			{
				materialColorCache.put(material, 0xFFFFFF);
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

			if (iconColorCache.containsKey(icon))
			{
				return iconColorCache.get(icon);
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

						/** Ignore things that are too dark. Standard luma calculation. */
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
				iconColorCache.put(icon, averageColor);
				return averageColor;
			}
		}
		catch (Exception e)
		{
			ResonantInduction.LOGGER.fine("Failed to compute colors for: " + item);
		}

		return 0xFFFFFF;
	}

	public static String moltenToMaterial(String fluidName)
	{
		return fluidNameToMaterial(fluidName, "molten");
	}

	public static String materialNameToMolten(String fluidName)
	{
		return materialNameToFluid(fluidName, "molten");
	}

	public static String mixtureToMaterial(String fluidName)
	{
		return fluidNameToMaterial(fluidName, "mixture");
	}

	public static String materialNameToMixture(String fluidName)
	{
		return materialNameToFluid(fluidName, "mixture");
	}

	public static String fluidNameToMaterial(String fluidName, String type)
	{
		return LanguageUtility.decapitalizeFirst(LanguageUtility.underscoreToCamel(fluidName).replace(type, ""));
	}

	public static String materialNameToFluid(String materialName, String type)
	{
		return type + "_" + LanguageUtility.camelToLowerUnderscore(materialName);
	}

	public static BlockFluidFinite getMixture(String name)
	{
		return ResonantInduction.blockMixtureFluids.get(getID(name));
	}

	public static BlockFluidFinite getMolten(String name)
	{
		return ResonantInduction.blockMoltenFluid.get(getID(name));
	}

	public static int getID(String name)
	{
		if (!materials.containsKey(name))
		{
			ResonantInduction.LOGGER.severe("Trying to get invalid material name " + name);
			return 0;
		}

		return materials.get(name);
	}

	public static String getName(int id)
	{
		return materials.inverse().get(id);
	}

	public static String getName(ItemStack itemStack)
	{
		return LanguageUtility.decapitalizeFirst(OreDictionary.getOreName(OreDictionary.getOreID(itemStack)).replace("dust", "").replace("ore", "").replace("ingot", ""));
	}

	public static int getColor(String name)
	{
		if (name != null && materialColorCache.containsKey(name))
		{
			return materialColorCache.get(name);
		}

		return 0xFFFFFF;
	}

	public static List<String> getMaterials()
	{
		List<String> returnMaterials = new ArrayList<String>();

		for (int i = 0; i < materials.size(); i++)
		{
			returnMaterials.add(getName(i));
		}

		return returnMaterials;
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();

		for (Entry<String, Integer> entry : materials.entrySet())
		{
			NBTTagCompound node = new NBTTagCompound();
			node.setString("materialName", entry.getKey());
			node.setInteger("materialID", entry.getValue());
			list.appendTag(node);
		}

		nbt.setTag("materialIDMap", list);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		if (nbt.hasKey("materialIDMap"))
		{
			materials.clear();
			NBTTagList nbtList = nbt.getTagList("materialIDMap");

			for (int i = 0; i < nbtList.tagCount(); ++i)
			{
				NBTTagCompound node = (NBTTagCompound) nbtList.tagAt(i);
				materials.put(node.getString("materialName"), node.getInteger("materialID"));
			}
		}
	}

	@Override
	public File getSaveFile()
	{
		return new File(NBTUtility.getSaveDirectory(), "Resource_Generator.dat");
	}

	@Override
	public void setSaveFile(File file)
	{

	}
}
