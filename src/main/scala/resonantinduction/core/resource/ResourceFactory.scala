package resonantinduction.core.resource

import java.awt._
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.{List, _}
import javax.imageio.ImageIO

import cpw.mods.fml.common.registry.{GameRegistry, LanguageRegistry}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fluids.{BlockFluidFinite, FluidContainerRegistry, FluidRegistry, FluidStack}
import net.minecraftforge.oredict.OreDictionary
import resonant.api.recipe.MachineRecipes
import resonant.lib.factory.resources.RecipeType
import resonant.lib.prefab.tile.{BlockFluidMaterial, BlockFluidMixture, FluidColored}
import resonant.lib.utility.LanguageUtility
import resonantinduction.core.{Reference, ResonantInduction, Settings}

/**
 * @author Calclavia
 */
object ResourceFactory
{
  def moltenToMaterial(fluidName: String): String =
  {
    return fluidNameToMaterial(fluidName, "molten")
  }

  def materialNameToMolten(fluidName: String): String =
  {
    return materialNameToFluid(fluidName, "molten")
  }

  def mixtureToMaterial(fluidName: String): String =
  {
    return fluidNameToMaterial(fluidName, "mixture")
  }

  def materialNameToMixture(fluidName: String): String =
  {
    return materialNameToFluid(fluidName, "mixture")
  }

  def fluidNameToMaterial(fluidName: String, `type`: String): String =
  {
    return LanguageUtility.decapitalizeFirst(LanguageUtility.underscoreToCamel(fluidName).replace(`type`, ""))
  }

  def materialNameToFluid(materialName: String, `type`: String): String =
  {
    return `type` + "_" + LanguageUtility.camelToLowerUnderscore(materialName)
  }

  def getMixture(name: String): BlockFluidFinite =
  {
    return ResonantInduction.blockMixtureFluids.get(getID(name))
  }

  def getMolten(name: String): BlockFluidFinite =
  {
    return ResonantInduction.blockMoltenFluid.get(getID(name))
  }

  def getName(id: Int): String =
  {
    return materials.inverse.get(id)
  }

  def getName(itemStack: ItemStack): String =
  {
    return LanguageUtility.decapitalizeFirst(OreDictionary.getOreName(OreDictionary.getOreID(itemStack)).replace("dirtyDust", "").replace("dust", "").replace("ore", "").replace("ingot", ""))
  }

  def getColor(name: String): Int =
  {
    if (name != null && materialColorCache.containsKey(name))
    {
      return materialColorCache.get(name)
    }
    return 0xFFFFFF
  }

  @deprecated def getMaterials: List[String] =
  {
    val returnMaterials: List[String] = new ArrayList[String]
    {
      var i: Int = 0
      while (i < materials.size)
      {
        {
          returnMaterials.add(getName(i))
        }
        ({i += 1; i - 1})
      }
    }
    return returnMaterials
  }
}

class ResourceFactory
{
  /**
   * A list of materials
   */
  private[core] final val materials: Set[String] = new HashSet[_]
  /**
   * Reference to color of material
   */
  private[core] final val materialColorCache: HashMap[String, Integer] = new HashMap[String, Integer]
  /**
   * Reference to computed color tint of auto-generated ores
   */
  private[core] final val iconColorCache: HashMap[IIcon, Integer] = new HashMap[IIcon, Integer]
  private[core] final val moltenFluidMap: HashMap[String, Block] = new HashMap[_, _]
  private[core] final val mixtureFluidMap: HashMap[String, Block] = new HashMap[_, _]
  private[core] final val moltenBucketMap: HashMap[String, Item] = new HashMap[_, _]
  private[core] final val mixtureBucketMap: HashMap[String, Item] = new HashMap[_, _]
  private[core] final val rubbleMap: HashMap[String, Item] = new HashMap[_, _]
  private[core] final val dustMap: HashMap[String, Item] = new HashMap[_, _]
  private[core] final val refinedDustMap: HashMap[String, Item] = new HashMap[_, _]

  def generate(materialName: String)
  {
    val nameCaps: String = LanguageUtility.capitalizeFirst(materialName)
    var localizedName: String = materialName
    val list: List[ItemStack] = OreDictionary.getOres("ingot" + nameCaps)
    if (list.size > 0)
    {
      val `type`: ItemStack = list.get(0)
      localizedName = `type`.getDisplayName.trim
      if (LanguageUtility.getLocal(localizedName) != null && LanguageUtility.getLocal(localizedName) ne "")
      {
        localizedName = LanguageUtility.getLocal(localizedName)
      }
      localizedName.replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" $", "")
    }
    val fluidMolten: FluidColored = new FluidColored(ResourceFactory.materialNameToMolten(materialName))
    fluidMolten.setDensity(7)
    fluidMolten.setViscosity(5000)
    fluidMolten.setTemperature(273 + 1538)
    FluidRegistry.registerFluid(fluidMolten)
    LanguageRegistry.instance.addStringLocalization(fluidMolten.getUnlocalizedName, LanguageUtility.getLocal("tooltip.molten") + " " + localizedName)
    val blockFluidMaterial: BlockFluidMaterial = new BlockFluidMaterial(fluidMolten)
    GameRegistry.registerBlock(blockFluidMaterial, "molten" + nameCaps)
    moltenFluidMap.put(materialName, blockFluidMaterial)
    FluidContainerRegistry.registerFluidContainer(fluidMolten, moltenBucketMap.getStackFromMaterial(materialName))
    val fluidMixture: FluidColored = new FluidColored(ResourceFactory.materialNameToMixture(materialName))
    FluidRegistry.registerFluid(fluidMixture)
    val blockFluidMixture: BlockFluidMixture = new BlockFluidMixture(fluidMixture)
    LanguageRegistry.instance.addStringLocalization(fluidMixture.getUnlocalizedName, localizedName + " " + LanguageUtility.getLocal("tooltip.mixture"))
    GameRegistry.registerBlock(blockFluidMixture, "mixture" + nameCaps)
    mixtureFluidMap.put(materialName, blockFluidMixture)
    FluidContainerRegistry.registerFluidContainer(fluidMixture, ResonantInduction.itemBucketMixture.getStackFromMaterial(materialName))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER.name, new FluidStack(fluidMolten, FluidContainerRegistry.BUCKET_VOLUME), "ingot" + nameCaps)
    val dust: ItemStack = ResonantInduction.itemDust.getStackFromMaterial(materialName)
    val rubble: ItemStack = ResonantInduction.itemRubble.getStackFromMaterial(materialName)
    val refinedDust: ItemStack = ResonantInduction.itemRefinedDust.getStackFromMaterial(materialName)
    if (allowOreDictCompatibility)
    {
      OreDictionary.registerOre("rubble" + nameCaps, ResonantInduction.itemRubble.getStackFromMaterial(materialName))
      OreDictionary.registerOre("dirtyDust" + nameCaps, ResonantInduction.itemDust.getStackFromMaterial(materialName))
      OreDictionary.registerOre("dust" + nameCaps, ResonantInduction.itemRefinedDust.getStackFromMaterial(materialName))
      MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, "rubble" + nameCaps, dust, dust)
      MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER.name, "dirtyDust" + nameCaps, refinedDust)
    }
    else
    {
      MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, rubble, dust, dust)
      MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER.name, dust, refinedDust)
    }
    FurnaceRecipes.smelting.addSmelting(dust.itemID, dust.getItemDamage, OreDictionary.getOres("ingot" + nameCaps).get(0).copy, 0.7f)
    val smeltResult: ItemStack = OreDictionary.getOres("ingot" + nameCaps).get(0).copy
    FurnaceRecipes.smelting.addSmelting(refinedDust.itemID, refinedDust.getItemDamage, smeltResult, 0.7f)
    if (OreDictionary.getOres("ore" + nameCaps).size > 0)
    {
      MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, "ore" + nameCaps, "rubble" + nameCaps)
    }
  }

  def generateOreResources
  {
    OreDictionary.registerOre("ingotGold", Items.gold_ingot)
    OreDictionary.registerOre("ingotIron", Items.iron_ingot)
    OreDictionary.registerOre("oreGold", Blocks.gold_ore)
    OreDictionary.registerOre("oreIron", Blocks.iron_ore)
    OreDictionary.registerOre("oreLapis", Blocks.lapis_ore)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER.name, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Blocks.stone))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, Blocks.cobblestone, Blocks.gravel)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, Blocks.stone, Blocks.cobblestone)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.SAWMILL.name, Blocks.log, new ItemStack(Blocks.planks, 7, 0))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Blocks.gravel, Blocks.sand)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Blocks.glass, Blocks.sand)
    val it: Iterator[String] = materials.keySet.iterator
    while (it.hasNext)
    {
      val materialName: String = it.next
      val nameCaps: String = LanguageUtility.capitalizeFirst(materialName)
      if (OreDictionary.getOres("ore" + nameCaps).size > 0)
      {
        generate(materialName)
      }
      else
      {
        it.remove
      }
    }
  }

  @SideOnly(Side.CLIENT) def computeColors
  {
    for (material <- materials.keySet)
    {
      val totalR: Int = 0
      val totalG: Int = 0
      val totalB: Int = 0
      val colorCount: Int = 0
      import scala.collection.JavaConversions._
      for (ingotStack <- OreDictionary.getOres("ingot" + LanguageUtility.capitalizeFirst(material)))
      {
        val theIngot: Item = ingotStack.getItem
        val color: Int = getAverageColor(ingotStack)
        materialColorCache.put(material, color)
      }
      if (!materialColorCache.containsKey(material))
      {
        materialColorCache.put(material, 0xFFFFFF)
      }
    }
  }

  /**
   * Gets the average color of this item.
   *
   * @param itemStack - The itemSstack
   * @return The RGB hexadecimal color code.
   */
  @SideOnly(Side.CLIENT) def getAverageColor(itemStack: ItemStack): Int =
  {
    var totalR: Int = 0
    var totalG: Int = 0
    var totalB: Int = 0
    var colorCount: Int = 0
    val item: Item = itemStack.getItem
    try
    {
      val icon: IIcon = item.getIconIndex(itemStack)
      if (iconColorCache.containsKey(icon))
      {
        return iconColorCache.get(icon)
      }
      var iconString: String = icon.getIconName
      if (iconString != null && !iconString.contains("MISSING_ICON_ITEM"))
      {
        iconString = (if (iconString.contains(":")) iconString.replace(":", ":" + Reference.itemTextureDirectory) else Reference.itemTextureDirectory + iconString) + ".png"
        val textureLocation: ResourceLocation = new ResourceLocation(iconString)
        val inputstream: InputStream = Minecraft.getMinecraft.getResourceManager.getResource(textureLocation).getInputStream
        val bufferedimage: BufferedImage = ImageIO.read(inputstream)
        val width: Int = bufferedimage.getWidth
        val height: Int = bufferedimage.getWidth
        {
          var x: Int = 0
          while (x < width)
          {
            {
              {
                var y: Int = 0
                while (y < height)
                {
                  {
                    val rgb: Color = new Color(bufferedimage.getRGB(x, y))
                    val luma: Double = 0.2126 * rgb.getRed + 0.7152 * rgb.getGreen + 0.0722 * rgb.getBlue
                    if (luma > 40)
                    {
                      totalR += rgb.getRed
                      totalG += rgb.getGreen
                      totalB += rgb.getBlue
                      colorCount += 1
                    }
                  }
                  ({y += 1; y - 1})
                }
              }
            }
            ({x += 1; x - 1})
          }
        }
      }
      if (colorCount > 0)
      {
        totalR /= colorCount
        totalG /= colorCount
        totalB /= colorCount
        val averageColor: Int = new Color(totalR, totalG, totalB).brighter.getRGB
        iconColorCache.put(icon, averageColor)
        return averageColor
      }
    }
    catch
      {
        case e: Exception =>
        {
          Reference.logger.fine("Failed to compute colors for: " + item)
        }
      }
    return 0xFFFFFF
  }

  @ForgeSubscribe def oreRegisterEvent(evt: OreDictionary.OreRegisterEvent)
  {
    if (evt.Name.startsWith("ingot"))
    {
      val oreDictName: String = evt.Name.replace("ingot", "")
      val materialName: String = LanguageUtility.decapitalizeFirst(oreDictName)
      if (!materials.containsKey(materialName))
      {
        Settings.CONFIGURATION.load
        val allowMaterial: Boolean = Settings.CONFIGURATION.get("Resource_Generator", "Enable " + oreDictName, true).getBoolean(true)
        Settings.CONFIGURATION.save
        if (!allowMaterial || OreDetectionBlackList.isIngotBlackListed("ingot" + oreDictName) || OreDetectionBlackList.isOreBlackListed("ore" + oreDictName))
        {
          return
        }
        materials.put(materialName, ({ResourceFactory.maxID += 1; ResourceFactory.maxID - 1}))
      }
    }
  }

  @ForgeSubscribe @SideOnly(Side.CLIENT) def reloadTextures(e: TextureStitchEvent.Post)
  {
    computeColors
  }
}