package resonantinduction.electrical

import cpw.mods.fml.common.Mod.{EventHandler, Instance}
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Loader, Mod, ModMetadata, SidedProxy}
import ic2.api.item.IC2Items
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.{OreDictionary, ShapedOreRecipe, ShapelessOreRecipe}
import resonant.content.loader.ModManager
import resonant.lib.loadable.LoadableHandler
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.atomic.gate.ItemQuantumGlyph
import resonantinduction.core.resource.ItemResourcePart
import resonantinduction.core.{Reference, ResonantTab, Settings}
import resonantinduction.electrical.battery.{BlockBattery, ItemBlockBattery, TileBattery}
import resonantinduction.electrical.generator.{TileMotor, TileSolarPanel, TileThermopile}
import resonantinduction.electrical.levitator.ItemLevitator
import resonantinduction.electrical.multimeter.ItemMultimeter
import resonantinduction.electrical.tesla.TileTesla
import resonantinduction.electrical.transformer.ItemTransformer
import resonantinduction.electrical.wire.{EnumWireMaterial, ItemWire}

/** Resonant Induction Electrical Module
  *
  * @author Calclavia */
object Electrical {
  /** Mod Information */
  final val ID: String = "ResonantInduction|Electrical"
  final val NAME: String = Reference.name + " Electrical"
  @Instance("ResonantInduction|Electrical") var INSTANCE: Electrical = null
  @SidedProxy(clientSide = "ClientProxy", serverSide = "CommonProxy") var proxy: CommonProxy = null
  @Mod.Metadata("ResonantInduction|Electrical") var metadata: ModMetadata = null
  final val contentRegistry: ModManager = new ModManager().setPrefix(Reference.prefix).setTab(ResonantTab.tab)

}

@Mod(modid = "ResonantInduction|Electrical", name = "Resonant Induction Electrical", version = Reference.version, dependencies = "before:ThermalExpansion;before:Mekanism;after:ResonantInduction|Mechanical;required-after:" + Reference.coreID) class Electrical {
  @EventHandler def preInit(evt: FMLPreInitializationEvent) {
    modproxies = new LoadableHandler
    NetworkRegistry.INSTANCE.registerGuiHandler(this, Electrical.proxy)
    Settings.config.load
    ElectricalContent.itemWire = Electrical.contentRegistry.newItem(classOf[ItemWire])
    ElectricalContent.itemMultimeter = Electrical.contentRegistry.newItem(classOf[ItemMultimeter])
    ElectricalContent.itemTransformer = Electrical.contentRegistry.newItem(classOf[ItemTransformer])
    ElectricalContent.blockTesla = Electrical.contentRegistry.newBlock(classOf[TileTesla])
    ElectricalContent.blockBattery = Electrical.contentRegistry.newBlock(classOf[BlockBattery], classOf[ItemBlockBattery], classOf[TileBattery])
    ElectricalContent.itemLevitator = Electrical.contentRegistry.newItem(classOf[ItemLevitator])
    ElectricalContent.itemInsulation = Electrical.contentRegistry.newItem("insulation", classOf[ItemResourcePart])
    ElectricalContent.blockSolarPanel = Electrical.contentRegistry.newBlock(classOf[TileSolarPanel])
    ElectricalContent.blockMotor = Electrical.contentRegistry.newBlock(classOf[TileMotor])
    ElectricalContent.blockThermopile = Electrical.contentRegistry.newBlock(classOf[TileThermopile])
    ElectricalContent.itemQuantumGlyph = Electrical.contentRegistry.newItem(classOf[ItemQuantumGlyph])
    Settings.config.save
    OreDictionary.registerOre("wire", ElectricalContent.itemWire)
    OreDictionary.registerOre("motor", ElectricalContent.blockMotor)
    OreDictionary.registerOre("battery", ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
    OreDictionary.registerOre("batteryBox", ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
    ResonantTab.itemStack(new ItemStack(ElectricalContent.itemTransformer))
    for (material <- EnumWireMaterial.values) {
      material.setWire(ElectricalContent.itemWire)
    }
    Electrical.proxy.preInit
    modproxies.preInit
  }

  @EventHandler def init(evt: FMLInitializationEvent) {
    MultipartElectrical.INSTANCE = new MultipartElectrical
    Electrical.proxy.init
    modproxies.init
  }

  @EventHandler def postInit(evt: FMLPostInitializationEvent) {
    GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.blockTesla, "WEW", " C ", "DID", 'W', "wire", 'E', Items.ender_eye, 'C', UniversalRecipe.BATTERY.get, 'D', Items.diamond, 'I', UniversalRecipe.PRIMARY_PLATE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.itemMultimeter, "WWW", "ICI", 'W', "wire", 'C', UniversalRecipe.BATTERY.get, 'I', UniversalRecipe.PRIMARY_METAL.get))
    val tierOneBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte])
    val tierTwoBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 1.asInstanceOf[Byte])
    val tierThreeBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 2.asInstanceOf[Byte])
    GameRegistry.addRecipe(new ShapedOreRecipe(tierOneBattery, "III", "IRI", "III", 'R', Blocks.redstone_block, 'I', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(tierTwoBattery, "RRR", "RIR", "RRR", 'R', tierOneBattery, 'I', UniversalRecipe.PRIMARY_PLATE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(tierThreeBattery, "RRR", "RIR", "RRR", 'R', tierTwoBattery, 'I', Blocks.diamond_block))
    GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper"))
    GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin"))
    GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Items.iron_ingot))
    GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum"))
    GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SILVER.getWire, "MMM", 'M', "ingotSilver"))
    GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", 'M', "ingotSuperconductor"))
    GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", "MEM", "MMM", 'M', Items.gold_ingot, 'E', Items.ender_eye))
    GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.itemCharger, "WWW", "ICI", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get, 'C', UniversalRecipe.CIRCUIT_T1.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.itemTransformer, "WWW", "WWW", "III", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 0), " CT", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 1), "TCT", "LBL", " CT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 2), "TC ", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 3), "TCT", "LBL", "TC ", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.blockSolarPanel, "CCC", "WWW", "III", 'W', "wire", 'C', Items.coal, 'I', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.blockMotor, "SRS", "SMS", "SWS", 'W', "wire", 'R', Items.redstone, 'M', Blocks.iron_block, 'S', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.blockThermopile, "ORO", "OWO", "OOO", 'W', "wire", 'O', Blocks.obsidian, 'R', Items.redstone))
    //GameRegistry.addRecipe(new ShapedOreRecipe(ElectricalContent.itemLaserGun, "RDR", "RDR", "ICB", 'R', Items.redstone, 'D', Items.diamond, 'I', Items.gold_ingot, 'C', UniversalRecipe.CIRCUIT_T2.get, 'B', ItemBlockBattery.setTier(new ItemStack(Electrical.blockBattery, 1, 0), 0.asInstanceOf[Byte])))
    if (Loader.isModLoaded("IC2")) {
      GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire, IC2Items.getItem("copperCableItem")))
      GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.TIN.getWire, IC2Items.getItem("tinCableItem")))
      GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.IRON.getWire, IC2Items.getItem("ironCableItem")))
      GameRegistry.addRecipe(new ShapelessOreRecipe(IC2Items.getItem("copperCableItem"), EnumWireMaterial.COPPER.getWire))
      GameRegistry.addRecipe(new ShapelessOreRecipe(IC2Items.getItem("tinCableItem"), EnumWireMaterial.TIN.getWire))
      GameRegistry.addRecipe(new ShapelessOreRecipe(IC2Items.getItem("ironCableItem"), EnumWireMaterial.IRON.getWire))
    }
    if (Loader.isModLoaded("Mekanism")) {
      GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire, "universalCable"))
    }
    Electrical.proxy.postInit
    modproxies.postInit
  }

  var modproxies: LoadableHandler = null
}