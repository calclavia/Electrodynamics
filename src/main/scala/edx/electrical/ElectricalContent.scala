package edx.electrical

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{EDXCreativeTab, Reference, ResonantPartFactory}
import edx.electrical.circuit.component.TileSiren
import edx.electrical.circuit.component.laser.focus.{ItemFocusingMatrix, TileFocusCrystal, TileMirror}
import edx.electrical.circuit.component.laser.{TileLaserEmitter, TileLaserReceiver}
import edx.electrical.circuit.component.tesla.TileTesla
import edx.electrical.circuit.source.battery.{ItemBlockBattery, TileBattery}
import edx.electrical.circuit.source.{TileMotor, TileSolarPanel, TileThermopile}
import edx.electrical.circuit.transformer.{ItemElectricTransformer, PartElectricTransformer}
import edx.electrical.circuit.wire.ItemWire
import edx.electrical.circuit.wire.base.WireMaterial
import edx.electrical.circuit.wire.flat.{PartFlatWire, RenderFlatWire}
import edx.electrical.circuit.wire.framed.{PartFramedWire, RenderFramedWire}
import edx.electrical.multimeter.{ItemMultimeter, PartMultimeter}
import edx.quantum.gate.{ItemQuantumGlyph, PartQuantumGlyph}
import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.oredict.{OreDictionary, ShapelessOreRecipe}
import resonantengine.lib.modcontent.ExplicitContentName
import resonantengine.lib.utility.recipe.UniversalRecipe
import resonantengine.prefab.modcontent.ContentHolder

object ElectricalContent extends ContentHolder
{
  val particleTextures = new ResourceLocation("textures/particle/particles.png")

  var itemWire = new ItemWire
  var itemMultimeter = new ItemMultimeter
  var itemTransformer = new ItemElectricTransformer
  @ExplicitContentName("insulation")
  var itemInsulation = new Item
  var itemQuantumGlyph = new ItemQuantumGlyph

  var itemFocusingMatrix: ItemFocusingMatrix = new ItemFocusingMatrix

  var blockTesla: Block = new TileTesla
  var blockBattery: Block = new TileBattery
  var blockSolarPanel: Block = new TileSolarPanel
  var blockMotor: Block = new TileMotor
  var blockThermopile: Block = new TileThermopile

  var blockLaserEmitter: Block = new TileLaserEmitter
  var blockLaserReceiver: Block = new TileLaserReceiver
  var blockMirror: Block = new TileMirror
  var blockFocusCrystal: Block = new TileFocusCrystal
  var blockSiren: Block = new TileSiren

  var tierOneBattery: ItemStack = null
  var tierTwoBattery: ItemStack = null
  var tierThreeBattery: ItemStack = null

  manager.setTab(EDXCreativeTab).setPrefix(Reference.prefix)

  override def preInit()
  {
    super.preInit()

    tierOneBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0)
    tierTwoBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 1)
    tierThreeBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 2)

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
    super.init()

    OreDictionary.registerOre("wire", ElectricalContent.itemWire)
    OreDictionary.registerOre("motor", ElectricalContent.blockMotor)
    OreDictionary.registerOre("battery", ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
    OreDictionary.registerOre("batteryBox", ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
  }

  override def postInit()
  {
    recipes += shaped(new ItemStack(blockSiren, 2), "NPN", 'N', Blocks.noteblock, 'P', UniversalRecipe.SECONDARY_PLATE.get)
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

  def getWire(t: WireMaterial, count: Int): ItemStack = new ItemStack(itemWire, count, t.ordinal())

  /**
   * Handle wire texture
   */
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def preTextureHook(event: TextureStitchEvent.Pre)
  {
    if (event.map.getTextureType == 0)
    {
      RenderFlatWire.wireIcon = event.map.registerIcon(Reference.prefix + "models/flatWire")
      RenderFramedWire.wireIcon = event.map.registerIcon(Reference.prefix + "models/wire")
      RenderFramedWire.insulationIcon = event.map.registerIcon(Reference.prefix + "models/insulation")
    }
  }
}
