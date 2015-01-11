package resonantinduction.core.resource

import java.awt._
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.LanguageRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fluids.{FluidContainerRegistry, FluidRegistry, FluidStack}
import net.minecraftforge.oredict.OreDictionary
import resonant.api.recipe.{MachineRecipes, RecipeType}
import resonant.lib.factory.resources.ResourceFactory
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.StringWrapper._
import resonantinduction.archaic.ArchaicContent
import resonantinduction.core.resource.content._
import resonantinduction.core.{Reference, Settings}

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * @author Calclavia
 */
object AutoResourceFactory
{
  /**
   * A list of materials
   */
  val materials = mutable.Set.empty[String]

  val blackList = Array("uranium")

  /**
   * Reference to computed color tint of auto-generated ores
   */
  val iconColorCache = mutable.Map.empty[IIcon, Integer]
  val moltenFluidMap = mutable.Map.empty[String, Block]
  val moltenBucketMap = mutable.Map.empty[String, Item]
  val mixtureBucketMap = mutable.Map.empty[String, Item]

  def init()
  {

    //Register resource types
    ResourceFactory.registerResourceItem("rubble", classOf[ItemRubble])
    ResourceFactory.registerResourceItem("dust", classOf[ItemDust])
    ResourceFactory.registerResourceItem("refinedDust", classOf[ItemRefinedDust])
    ResourceFactory.registerResourceItem("bucketMolten", classOf[ItemMoltenBucket])

    //Create copper and tin ingots and ores
    ResourceFactory.registerMaterial("copper")
    ResourceFactory.requestItem("ingot", "copper")
    ResourceFactory.requestBlock("ore", "copper")
    ResourceFactory.registerMaterialColor("copper", 0xB5634E)

    ResourceFactory.registerMaterial("tin")
    ResourceFactory.requestItem("ingot", "tin")
    ResourceFactory.requestBlock("ore", "tin")
    ResourceFactory.registerMaterialColor("tin", 0xAFBFB2)

    OreDictionary.initVanillaEntries()

    //Add vanilla ingots to auto generate
    registerAutoMaterial("gold")
    registerAutoMaterial("iron")
    registerAutoMaterial("copper")
    registerAutoMaterial("tin")
  }

  def postInit()
  {
    //Add vanilla ore processing recipes
    MachineRecipes.instance.addRecipe(RecipeType.SMELTER.name, new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Blocks.stone))
    MachineRecipes.instance.addRecipe(RecipeType.CRUSHER.name, Blocks.cobblestone, Blocks.gravel)
    MachineRecipes.instance.addRecipe(RecipeType.CRUSHER.name, Blocks.stone, Blocks.cobblestone)
    MachineRecipes.instance.addRecipe(RecipeType.SAWMILL.name, Blocks.log, new ItemStack(Blocks.planks, 7, 0))
    MachineRecipes.instance.addRecipe(RecipeType.GRINDER.name, Blocks.gravel, Blocks.sand)
    MachineRecipes.instance.addRecipe(RecipeType.GRINDER.name, Blocks.glass, Blocks.sand)

    //Call generate() on all materials
    materials.foreach(generate)

    Reference.logger.fine("Resource Factory generated " + materials.size + " resources.")
  }

  def generate(material: String)
  {
    ResourceFactory.registerMaterial(material)

    val nameCaps: String = LanguageUtility.capitalizeFirst(material)
    var localizedName: String = material

    //Fix the material name
    val ingotNames = OreDictionary.getOres("ingot" + nameCaps)

    if (ingotNames.size > 0)
    {
      val firstIngotStack = ingotNames(0)
      localizedName = firstIngotStack.getDisplayName.trim

      if (localizedName.getLocal != null && localizedName.getLocal != "")
        localizedName = localizedName.getLocal

      localizedName = localizedName.replaceAll("misc.ingot".getLocal, "").trim
    }

    //Generate molten fluid
    val fluidMolten = new FluidColored("molten" + nameCaps).setDensity(7).setViscosity(5000).setTemperature(273 + 1538)
    FluidRegistry.registerFluid(fluidMolten)
    LanguageRegistry.instance.addStringLocalization(fluidMolten.getUnlocalizedName, LanguageUtility.getLocal("misc.molten") + " " + localizedName)
    val blockFluidMaterial = new BlockFluidMaterial(fluidMolten)
    ArchaicContent.manager.newBlock("molten" + nameCaps, blockFluidMaterial)
    moltenFluidMap += (material -> blockFluidMaterial)

    //Generate resource items: moltenBucket, rubble, dust and refined dust
    val results = ResourceFactory.requestItems(material, "ingot")
    results.foreach(keyVal => LanguageRegistry.instance.addStringLocalization(keyVal._2.getUnlocalizedName + ".name", localizedName + " " + ("misc." + keyVal._1).getLocal))

    val rubble = new ItemStack(ResourceFactory.getItem("rubble", material))
    val dust = new ItemStack(ResourceFactory.getItem("dust", material))
    val refinedDust = new ItemStack(ResourceFactory.getItem("refinedDust", material))

    FluidContainerRegistry.registerFluidContainer(fluidMolten, new ItemStack(ResourceFactory.getItem("bucketMolten", material)))
    //Add recipes

    MachineRecipes.instance.addRecipe(RecipeType.SMELTER.name, new FluidStack(fluidMolten, FluidContainerRegistry.BUCKET_VOLUME), "ingot" + nameCaps)
    MachineRecipes.instance.addRecipe(RecipeType.GRINDER.name, "rubble" + nameCaps, dust, dust)
    MachineRecipes.instance.addRecipe(RecipeType.MIXER.name, "dust" + nameCaps, refinedDust)
    FurnaceRecipes.smelting.func_151394_a(dust, OreDictionary.getOres("ingot" + nameCaps).get(0).copy, 0.7f)
    FurnaceRecipes.smelting.func_151394_a(refinedDust, OreDictionary.getOres("ingot" + nameCaps).get(0).copy, 0.7f)

    if (OreDictionary.getOres("ore" + nameCaps).size > 0)
      MachineRecipes.instance.addRecipe(RecipeType.CRUSHER.name, "ore" + nameCaps, "rubble" + nameCaps)
  }

  @SubscribeEvent
  def oreRegisterEvent(evt: OreDictionary.OreRegisterEvent)
  {
    if (evt.Name.startsWith("ingot"))
    {
      val oreDictName = evt.Name.replace("ingot", "")
      val materialName = oreDictName.decapitalizeFirst
      registerAutoMaterial(materialName)
    }
  }

  def registerAutoMaterial(material: String)
  {
    if (!materials.contains(material) && OreDictionary.getOres("ore" + material.capitalizeFirst).size > 0)
    {
      Settings.config.load()
      val allowMaterial = Settings.config.get("Resource-Generator", "Enable " + material, true).getBoolean(true)
      Settings.config.save()

      if (allowMaterial && !blackList.contains(material))
      {
        materials += material
      }
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def reloadTextures(e: TextureStitchEvent.Post)
  {
    computeColors()
  }

  @SideOnly(Side.CLIENT)
  def computeColors()
  {
    for (material <- materials)
    {
      for (ingotStack <- OreDictionary.getOres("ingot" + LanguageUtility.capitalizeFirst(material)))
      {
        ResourceFactory.registerMaterialColor(material, getAverageColor(ingotStack))
      }
    }
  }

  /**
   * Gets the average color of this item by looking at each pixel of the texture.
   *
   * @param itemStack - The itemStack
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

      if (iconColorCache.containsKey(icon))
      {
        return iconColorCache(icon)
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

        for (x <- 0 until width; y <- 0 until height)
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
}