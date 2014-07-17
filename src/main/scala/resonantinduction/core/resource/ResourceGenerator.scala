package resonantinduction.core.resource

import java.awt.Color
import javax.imageio.ImageIO

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fluids.{BlockFluidFinite, FluidContainerRegistry, FluidRegistry, FluidStack}
import net.minecraftforge.oredict.OreDictionary
import resonant.api.recipe.MachineRecipes
import resonant.lib.config.Config
import resonant.lib.recipe.Recipes
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.StringWrapper._
import resonantinduction.core.ResonantInduction.RecipeType
import resonantinduction.core.prefab.FluidColored
import resonantinduction.core.resource.fluid.{BlockFluidMaterial, BlockFluidMixture}
import resonantinduction.core.{CoreContent, Reference, Settings}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Generates the resources based on available ores in Resonant Induction
 * @author Calclavia
 */
object ResourceGenerator
{
  final val materials = mutable.Set.empty[String]
  private final val materialColorCache = mutable.HashMap.empty[String, Integer]
  private final val iconColorCache = mutable.HashMap.empty[IIcon, Integer]

  private final val category = "resource-generator"

  @Config(category = category)
  var enableAll: Boolean = true
  @Config(category = category)
  var enableAllFluids: Boolean = true

  /**
   * A list of material names. They are all camelCase reference of ore dictionary names without
   * the "ore" or "ingot" prefix.
   * <p/>
   * Name, ID
   */
  private[resource] var maxID: Int = 0
  @Config(category = "resource-generator", comment = "Allow the Resource Generator to make ore dictionary compatible recipes?")
  private val allowOreDictCompatibility: Boolean = true

  /**
   * Automatically generate resources from available ingots
   */
  def generateOreResources()
  {
    OreDictionary.registerOre("ingotGold", Items.gold_ingot)
    OreDictionary.registerOre("ingotIron", Items.iron_ingot)
    OreDictionary.registerOre("oreGold", Blocks.gold_ore)
    OreDictionary.registerOre("oreIron", Blocks.iron_ore)
    OreDictionary.registerOre("oreLapis", Blocks.lapis_ore)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER.name, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Blocks.stone))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, Blocks.cobblestone, Blocks.gravel)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, Blocks.stone, Blocks.cobblestone)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, Blocks.chest, new ItemStack(Blocks.planks, 7, 0))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Blocks.cobblestone, Blocks.sand)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Blocks.gravel, Blocks.sand)
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Blocks.glass, Blocks.sand)

    materials filter (name => OreDictionary.getOres("ore" + name.capitalizeFirst).size > 0) foreach (generate)
  }

  def generate(materialName: String)
  {
    val nameCaps = materialName.capitalizeFirst
    var localizedName = materialName

    val list = OreDictionary.getOres("ingot" + nameCaps)

    if (list.size > 0)
    {
      localizedName = list.get(0).getDisplayName.trim
      if (LanguageUtility.getLocal(localizedName) != null && LanguageUtility.getLocal(localizedName) != "")
      {
        localizedName = LanguageUtility.getLocal(localizedName)
      }

      localizedName.replace(LanguageUtility.getLocal("misc.resonantinduction.ingot"), "").replaceAll("^ ", "").replaceAll(" #", "")
    }

    if (enableAllFluids)
    {
      /**
       * Generate molten fluid
       */
      val fluidMolten = new FluidColored(materialNameToMolten(materialName)).setDensity(7).setViscosity(5000).setTemperature(273 + 1538)
      FluidRegistry.registerFluid(fluidMolten)
      val blockFluidMaterial = new BlockFluidMaterial(fluidMolten)
      CoreContent.manager.newBlock("molten" + nameCaps, blockFluidMaterial)
      FluidContainerRegistry.registerFluidContainer(fluidMolten, CoreContent.bucketMolten.getStackFromMaterial(materialName))

      /**
       * Generate mixture fluid
       */
      val fluidMixture = new FluidColored(materialNameToMixture(materialName))
      FluidRegistry.registerFluid(fluidMixture)
      val blockFluidMixture = new BlockFluidMixture(fluidMixture)
      GameRegistry.registerBlock(blockFluidMixture, "mixture" + nameCaps)
      FluidContainerRegistry.registerFluidContainer(fluidMixture, CoreContent.bucketMixture.getStackFromMaterial(materialName))

      if (allowOreDictCompatibility)
        MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER.name, new FluidStack(fluidMolten, FluidContainerRegistry.BUCKET_VOLUME), "ingot" + nameCaps)
      else
        MachineRecipes.INSTANCE.addRecipe(RecipeType.SMELTER.name, new FluidStack(fluidMolten, FluidContainerRegistry.BUCKET_VOLUME), "ingot" + nameCaps)

    }

    val dust: ItemStack = CoreContent.dust.getStackFromMaterial(materialName)
    val rubble: ItemStack = CoreContent.rubble.getStackFromMaterial(materialName)
    val refinedDust: ItemStack = CoreContent.refinedDust.getStackFromMaterial(materialName)

    if (allowOreDictCompatibility)
    {
      OreDictionary.registerOre("rubble" + nameCaps, CoreContent.rubble.getStackFromMaterial(materialName))
      OreDictionary.registerOre("dirtyDust" + nameCaps, CoreContent.dust.getStackFromMaterial(materialName))
      OreDictionary.registerOre("dust" + nameCaps, CoreContent.refinedDust.getStackFromMaterial(materialName))
      MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, "rubble" + nameCaps, dust, dust)
      MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER.name, "dirtyDust" + nameCaps, refinedDust)
    }
    else
    {
      MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, rubble, dust, dust)
      MachineRecipes.INSTANCE.addRecipe(RecipeType.MIXER.name, dust, refinedDust)
    }

    Recipes +=(dust.copy, OreDictionary.getOres("ingot" + nameCaps).get(0).copy, 0.7f)
    val smeltResult = OreDictionary.getOres("ingot" + nameCaps).get(0).copy
    Recipes +=(refinedDust.copy, smeltResult, 0.7f)

    if (OreDictionary.getOres("ore" + nameCaps).size > 0)
    {
      MachineRecipes.INSTANCE.addRecipe(RecipeType.CRUSHER.name, "ore" + nameCaps, "rubble" + nameCaps)
    }
  }

  @SideOnly(Side.CLIENT)
  def computeColors
  {
    for (material <- materials)
    {
      val totalR: Int = 0
      val totalG: Int = 0
      val totalB: Int = 0
      val colorCount: Int = 0
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
   * @param itemStack
   * @return The RGB hexadecimal color code.
   */
  @SideOnly(Side.CLIENT)
  def getAverageColor(itemStack: ItemStack): Int =
  {
    var totalR: Int = 0
    var totalG: Int = 0
    var totalB: Int = 0
    var colorCount: Int = 0
    val item: Item = itemStack.getItem
    try
    {
      val icon: IIcon = item.getIconIndex(itemStack)

      if (iconColorCache.contains(icon))
      {
        return iconColorCache(icon)
      }

      var iconString: String = icon.getIconName
      if (iconString != null && !iconString.contains("MISSING_ICON_ITEM"))
      {
        iconString = (if (iconString.contains(":")) iconString.replace(":", ":" + Reference.itemTextureDirectory) else Reference.itemTextureDirectory + iconString) + ".png"
        val textureLocation = new ResourceLocation(iconString)
        val inputStream = Minecraft.getMinecraft.getResourceManager.getResource(textureLocation).getInputStream
        val bufferedImage = ImageIO.read(inputStream)
        val width: Int = bufferedImage.getWidth
        val height: Int = bufferedImage.getWidth

        /**
         * Read every single pixel of the texture.
         */
        for (x <- 0 until width; y <- 0 until height)
        {
          val rgb: Color = new Color(bufferedImage.getRGB(x, y))
          val luma: Double = 0.2126 * rgb.getRed + 0.7152 * rgb.getGreen + 0.0722 * rgb.getBlue

          if (luma > 40)
          {
            totalR += rgb.getRed
            totalG += rgb.getGreen
            totalB += rgb.getBlue
            colorCount += 1
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
    return Block.blockRegistry.getObject("mixture" + name).asInstanceOf[BlockFluidFinite]
  }

  def getMolten(name: String): BlockFluidFinite =
  {
    return Block.blockRegistry.getObject("molten" + LanguageUtility.capitalizeFirst(name)).asInstanceOf[BlockFluidFinite]
  }

  /**
   * Gets the ItemStack of the ore dust with this material name.
   */
  def getDust(name: String, quantity: Int = 1): ItemStack =
  {
    val itemStack = new ItemStack(CoreContent.dust, quantity)
    val nbt = new NBTTagCompound
    nbt.setString("material", name)
    itemStack.setTagCompound(nbt)
    return itemStack
  }

  /**
   * Gets the ItemStack of the refined ore dust with this material name.
   */
  def getRefinedDust(name: String, quantity: Int = 1): ItemStack =
  {
    val itemStack = new ItemStack(CoreContent.refinedDust, quantity)
    val nbt = new NBTTagCompound
    nbt.setString("material", name)
    itemStack.setTagCompound(nbt)
    return itemStack
  }

  /**
   * Gets the material of this ItemStack
   */
  def getMaterial(stack: ItemStack): String =
  {
    return NBTUtility.getNBTTagCompound(stack).getString("material")
  }

  def getName(itemStack: ItemStack): String =
  {
    return LanguageUtility.decapitalizeFirst(OreDictionary.getOreName(OreDictionary.getOreID(itemStack)).replace("dirtyDust", "").replace("dust", "").replace("ore", "").replace("ingot", ""))
  }

  def getColor(name: String): Int =
  {
    if (name != null && materialColorCache.contains(name))
    {
      return materialColorCache(name)
    }
    return 0xFFFFFF
  }

  @SubscribeEvent
  def oreRegisterEvent(evt: OreDictionary.OreRegisterEvent)
  {
    if (evt.Name.startsWith("ingot"))
    {
      val oreDictName = evt.Name.replace("ingot", "")
      val materialName = oreDictName.decapitalizeFirst

      if (!materials.contains(materialName))
      {
        Settings.config.load()
        val allowMaterial = Settings.config.get(category, "Enable " + oreDictName, true).getBoolean(true)
        Settings.config.save()

        if (!allowMaterial || OreDetectionBlackList.isIngotBlackListed("ingot" + oreDictName) || OreDetectionBlackList.isOreBlackListed("ore" + oreDictName))
        {
          return
        }

        materials += materialName
      }
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def reloadTextures(e: TextureStitchEvent.Post)
  {
    computeColors
  }

}