package resonantinduction.electrical

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.util.{EnumChatFormatting, ResourceLocation}
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.oredict.{OreDictionary, ShapelessOreRecipe}
import resonant.content.loader.ContentHolder
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.atomic.gate.{ItemQuantumGlyph, PartQuantumGlyph}
import resonantinduction.core.{Reference, ResonantPartFactory, ResonantTab}
import resonantinduction.electrical.battery.{ItemBlockBattery, TileBattery}
import resonantinduction.electrical.generator.{TileMotor, TileSolarPanel, TileThermopile}
import resonantinduction.electrical.laser.emitter.{BlockLaserEmitter, TileLaserEmitter}
import resonantinduction.electrical.laser.focus.ItemFocusingMatrix
import resonantinduction.electrical.laser.focus.crystal.{BlockFocusCrystal, TileFocusCrystal}
import resonantinduction.electrical.laser.focus.mirror.{BlockMirror, TileMirror}
import resonantinduction.electrical.laser.receiver.{BlockLaserReceiver, TileLaserReceiver}
import resonantinduction.electrical.multimeter.{ItemMultimeter, PartMultimeter}
import resonantinduction.electrical.tesla.TileTesla
import resonantinduction.electrical.transformer.{ItemElectricTransformer, PartElectricTransformer}
import resonantinduction.electrical.wire.ItemWire
import resonantinduction.electrical.wire.base.WireMaterial
import resonantinduction.electrical.wire.flat.{PartFlatWire, RenderFlatWire}
import resonantinduction.electrical.wire.framed.{PartFramedWire, RenderFramedWire}

/**
 * Created by robert on 8/11/2014.
 */
object ElectricalContent extends ContentHolder
{
  val particleTextures = new ResourceLocation("textures/particle/particles.png")

  var itemWire: Item = null
  var itemMultimeter: Item = null
  var itemTransformer: Item = null
  var itemInsulation: Item = null
  var itemQuantumGlyph: Item = null

  var blockTesla: Block = null
  var blockBattery: Block = null
  var blockSolarPanel: Block = null
  var blockMotor: Block = null
  var blockThermopile: Block = null

  var blockLaserEmitter: BlockLaserEmitter = null
  var blockLaserReceiver: BlockLaserReceiver = null
  var blockMirror: BlockMirror = null
  var blockFocusCrystal: BlockFocusCrystal = null

  var itemFocusingMatrix: ItemFocusingMatrix = null

  var tierOneBattery: ItemStack = null
  var tierTwoBattery: ItemStack = null
  var tierThreeBattery: ItemStack = null

  var guideBook: ItemStack = createGuide

  override def preInit()
  {
    super.preInit
    itemWire = manager.newItem(classOf[ItemWire])
    itemMultimeter = manager.newItem(classOf[ItemMultimeter])
    itemTransformer = manager.newItem(classOf[ItemElectricTransformer])
    itemInsulation = manager.newItem("insulation", classOf[Item])
    itemQuantumGlyph = manager.newItem(classOf[ItemQuantumGlyph])

    blockTesla = manager.newBlock(classOf[TileTesla])
    blockBattery = manager.newBlock(classOf[TileBattery])
    blockSolarPanel = manager.newBlock(classOf[TileSolarPanel])
    blockMotor = manager.newBlock(classOf[TileMotor])
    blockThermopile = manager.newBlock(classOf[TileThermopile])

    blockLaserEmitter = new BlockLaserEmitter()
    blockLaserReceiver = new BlockLaserReceiver()
    blockMirror = new BlockMirror()
    blockFocusCrystal = new BlockFocusCrystal()

    itemFocusingMatrix = new ItemFocusingMatrix()

    tierOneBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte])
    tierTwoBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 1.asInstanceOf[Byte])
    tierThreeBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 2.asInstanceOf[Byte])

    /** Register all parts */
    ResonantPartFactory.register(classOf[PartFramedWire])
    ResonantPartFactory.register(classOf[PartFlatWire])
    ResonantPartFactory.register(classOf[PartMultimeter])
    ResonantPartFactory.register(classOf[PartElectricTransformer])
    ResonantPartFactory.register(classOf[PartQuantumGlyph])

    MinecraftForge.EVENT_BUS.register(this)
  }

  override def init()
  {
    super.init

    ResonantTab.itemStack(new ItemStack(ElectricalContent.itemTransformer))

    GameRegistry.registerTileEntity(classOf[TileLaserEmitter], "EMLaserEmitter");
    GameRegistry.registerTileEntity(classOf[TileLaserReceiver], "EMLaserReceiver");
    GameRegistry.registerTileEntity(classOf[TileMirror], "EMLaserMirror");
    GameRegistry.registerTileEntity(classOf[TileFocusCrystal], "EMFocusCrystal");

    OreDictionary.registerOre("wire", ElectricalContent.itemWire)
    OreDictionary.registerOre("motor", ElectricalContent.blockMotor)
    OreDictionary.registerOre("battery", ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
    OreDictionary.registerOre("batteryBox", ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
  }

  override def postInit
  {
    super.postInit
    recipes += shaped(blockTesla, "WEW", " C ", "DID", 'W', "wire", 'E', Items.ender_eye, 'C', UniversalRecipe.BATTERY.get, 'D', Items.diamond, 'I', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(itemMultimeter, "WWW", "ICI", 'W', "wire", 'C', UniversalRecipe.BATTERY.get, 'I', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(tierOneBattery, "III", "IRI", "III", 'R', Blocks.redstone_block, 'I', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(tierTwoBattery, "RRR", "RIR", "RRR", 'R', tierOneBattery, 'I', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(tierThreeBattery, "RRR", "RIR", "RRR", 'R', tierTwoBattery, 'I', Blocks.diamond_block)
    recipes += shaped(getWire(WireMaterial.COPPER, 3), "MMM", 'M', "ingotCopper")
    recipes += shaped(getWire(WireMaterial.TIN, 3), "MMM", 'M', "ingotTin")
    recipes += shaped(getWire(WireMaterial.IRON, 3), "MMM", 'M', Items.iron_ingot)
    recipes += shaped(getWire(WireMaterial.ALUMINUM, 3), "MMM", 'M', "ingotAluminum")
    recipes += shaped(getWire(WireMaterial.SILVER, 3), "MMM", 'M', "ingotSilver")
    recipes += shaped(getWire(WireMaterial.SUPERCONDUCTOR, 3), "MMM", 'M', "ingotSuperconductor")
    recipes += shaped(getWire(WireMaterial.SUPERCONDUCTOR, 3), "MMM", "MEM", "MMM", 'M', Items.gold_ingot, 'E', Items.ender_eye)
    //recipes += shaped(ElectricalContent.itemCharger, "WWW", "ICI", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get, 'C', UniversalRecipe.CIRCUIT_T1.get)
    recipes += shaped(itemTransformer, "WWW", "WWW", "III", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get)
    //recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 0), " CT", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    //recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 1), "TCT", "LBL", " CT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    //recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 2), "TC ", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    //recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 3), "TCT", "LBL", "TC ", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    recipes += shaped(blockSolarPanel, "CCC", "WWW", "III", 'W', "wire", 'C', Items.coal, 'I', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(blockMotor, "SRS", "SMS", "SWS", 'W', "wire", 'R', Items.redstone, 'M', Blocks.iron_block, 'S', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(blockThermopile, "ORO", "OWO", "OOO", 'W', "wire", 'O', Blocks.obsidian, 'R', Items.redstone)

    recipes += shaped(blockLaserEmitter, "IGI", "IDI", "III", 'G', Blocks.glass, 'I', Items.iron_ingot, 'D', Items.diamond)
    recipes += shaped(blockLaserReceiver, "IGI", "IRI", "III", 'G', Blocks.glass, 'I', Items.iron_ingot, 'R', Blocks.redstone_block)
    recipes += shaped(blockMirror, "GGG", "III", "GGG", 'G', Blocks.glass, 'I', Items.iron_ingot)
    recipes += shaped(blockFocusCrystal, "GGG", "GDG", "GGG", 'G', Blocks.glass, 'D', Items.diamond)
    recipes += shaped(itemFocusingMatrix, "GGG", "GNG", "GGG", 'G', Items.redstone, 'N', Items.quartz)


    if (Loader.isModLoaded("IC2"))
    {
      recipes += shapeless(getWire(WireMaterial.COPPER, 1), IC2Items.getItem("copperCableItem"))
      recipes += shapeless(getWire(WireMaterial.TIN, 1), IC2Items.getItem("tinCableItem"))
      recipes += shapeless(getWire(WireMaterial.IRON, 1), IC2Items.getItem("ironCableItem"))
      recipes += shapeless(IC2Items.getItem("copperCableItem"), getWire(WireMaterial.COPPER, 1))
      recipes += shapeless(IC2Items.getItem("tinCableItem"), getWire(WireMaterial.TIN, 1))
      recipes += shapeless(IC2Items.getItem("ironCableItem"), getWire(WireMaterial.IRON, 1))
    }
    if (Loader.isModLoaded("Mekanism"))
    {
      GameRegistry.addRecipe(new ShapelessOreRecipe(getWire(WireMaterial.COPPER, 1), "universalCable"))
    }

  }

  def getWire(t: WireMaterial, count: Int): ItemStack =
  {
    return new ItemStack(itemWire, count, t.ordinal())
  }

  def createGuide: ItemStack =
  {

    val guideBook = new ItemStack(Items.written_book)
    val bookNBT = new NBTTagCompound()
    bookNBT.setString("title", "Electromagnetic Coherence Guide")
    bookNBT.setString("author", "Calclavia")

    val pages = new NBTTagList()
    pages.appendTag(new NBTTagString(EnumChatFormatting.RED + "Guidebook:\n\n" + EnumChatFormatting.BLACK + "Electromagnetic Coherence is a mod all about lasers.\n\nYou can find all the blocks in the mod's creative tab."))
    pages.appendTag(new NBTTagString("A laser can be focused through a  " + EnumChatFormatting.RED + "laser emitter" + EnumChatFormatting.BLACK + ". By default, the color of the laser is white. The color can be changed by placing stained glass in front of it. Different combinations of glass would result in mixed colors."))
    pages.appendTag(new NBTTagString("To create a laser beam, provide a redstone pulse to the laser emitter. The intensity of the redstone would determine the intensity of the laser. Lasers with high intensities can burn and melt through blocks, hurting entities."))
    pages.appendTag(new NBTTagString("A laser beam can also be reflected using a  " + EnumChatFormatting.RED + "mirror" + EnumChatFormatting.BLACK + " with reduced intensity. Mirrors can be rotated by right clicking on it. Shift-right clicking a mirror focuses it to a side. Mirrors can also be auto-rotated with a redstone signal based on the direction of the signal propagation."))
    pages.appendTag(new NBTTagString("A " + EnumChatFormatting.RED + "laser receiver" + EnumChatFormatting.BLACK + " outputs a redstone signal with a strength based on the laser incident on its front. Using this, laser trip-wires can be made as entities walking through a laser will block its path."))
    pages.appendTag(new NBTTagString("The " + EnumChatFormatting.RED + "focusing matrix" + EnumChatFormatting.BLACK + " allows the player to focus mirrors and focus crystals. First, right click on a mirror/crystal to select it. Then, right click on a point to focus. Clicking the point twice will aim the laser at that point instead of making the device look at the point."))
    pages.appendTag(new NBTTagString("The " + EnumChatFormatting.RED + "Focus Crystal" + EnumChatFormatting.BLACK + " allows you to focus multiple laser beams into a single one, adding their strength together. All beams aiming at the crystal will be sent in the direction the crystal is facing. Focus Crystals can be rotated the same way as mirrors can."))
    pages.appendTag(new NBTTagString(EnumChatFormatting.RED + "Usages\n\n" + EnumChatFormatting.BLACK + "- Light Shows\n- Mining\n- Killing\n- Burning\n- Redstone Detection\n- Smelting (Aim strong laser at furnace)\n\nComing Soon:\n- Energy Transfer\n- Crafting"))

    bookNBT.setTag("pages", pages)
    guideBook.setTagCompound(bookNBT)

    return guideBook
  }

  /**
   * Handle wire texture
   */
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def preTextureHook(event: TextureStitchEvent.Pre)
  {
    if (event.map.getTextureType() == 0)
    {
      RenderFlatWire.wireIcon = event.map.registerIcon(Reference.prefix + "models/flatWire")
      RenderFramedWire.wireIcon = event.map.registerIcon(Reference.prefix + "models/wire")
      RenderFramedWire.insulationIcon = event.map.registerIcon(Reference.prefix + "models/insulation")
    }
  }

}
