package resonantinduction.electrical

import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import net.minecraftforge.oredict.ShapelessOreRecipe
import resonant.content.loader.ModManager
import resonant.lib.loadable.LoadableHandler
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.core.Reference
import resonantinduction.core.ResonantInduction
import resonantinduction.core.ResonantTab
import resonantinduction.core.Settings
import resonantinduction.core.resource.ItemResourcePart
import resonantinduction.electrical.battery.BlockBattery
import resonantinduction.electrical.battery.ItemBlockBattery
import resonantinduction.electrical.battery.TileBattery
import resonantinduction.electrical.generator.TileMotor
import resonantinduction.electrical.generator.TileSolarPanel
import resonantinduction.electrical.generator.TileThermopile
import resonantinduction.electrical.levitator.ItemLevitator
import resonantinduction.electrical.multimeter.ItemMultimeter
import resonantinduction.electrical.tesla.TileTesla
import resonantinduction.electrical.transformer.ItemTransformer
import resonantinduction.electrical.wire.EnumWireMaterial
import resonantinduction.electrical.wire.ItemWire
import resonantinduction.atomic.gate.ItemQuantumGlyph
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.Mod.Instance
import cpw.mods.fml.common.ModMetadata
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry

/** Resonant Induction Electrical Module
  *
  * @author Calclavia */
object Electrical {
  /** Mod Information */
  final val ID: String = "ResonantInduction|Electrical"
  final val NAME: String = Reference.name + " Electrical"
  @Instance(ID) var INSTANCE: Electrical = null
  @SidedProxy(clientSide = "ClientProxy", serverSide = "CommonProxy") var proxy: CommonProxy = null
  @Mod.Metadata(ID) var metadata: ModMetadata = null
  final val contentRegistry: ModManager = new ModManager().setPrefix(Reference.prefix).setTab(ResonantTab.tab)
  var itemWire: Item = null
  var itemMultimeter: Item = null
  var itemTransformer: Item = null
  var itemCharger: Item = null
  var blockTesla: Block = null
  var blockBattery: Block = null
  var blockEncoder: Block = null
  var itemRailing: Item = null
  var blockSolarPanel: Block = null
  var blockMotor: Block = null
  var blockThermopile: Block = null
  var itemLevitator: Item = null
  var blockArmbot: Block = null
  var itemDisk: Item = null
  var itemInsulation: Item = null
  var blockQuantumGate: Block = null
  var itemQuantumGlyph: Item = null
  var itemLaserGun: Item = null
}

@Mod(modid = Electrical.ID, name = Electrical.NAME, version = Reference.version, dependencies = "before:ThermalExpansion;before:Mekanism;after:ResonantInduction|Mechanical;required-after:" + Reference.coreID) class Electrical {
  @EventHandler def preInit(evt: FMLPreInitializationEvent) {
    modproxies = new LoadableHandler
    NetworkRegistry.INSTANCE.registerGuiHandler(this, Electrical.proxy)
    Settings.config.load
    Electrical.itemWire = Electrical.contentRegistry.newItem(classOf[ItemWire])
    Electrical.itemMultimeter = Electrical.contentRegistry.newItem(classOf[ItemMultimeter])
    Electrical.itemTransformer = Electrical.contentRegistry.newItem(classOf[ItemTransformer])
    Electrical.blockTesla = Electrical.contentRegistry.newBlock(classOf[TileTesla])
    Electrical.blockBattery = Electrical.contentRegistry.newBlock(classOf[BlockBattery], classOf[ItemBlockBattery], classOf[TileBattery])
    Electrical.itemLevitator = Electrical.contentRegistry.newItem(classOf[ItemLevitator])
    Electrical.itemInsulation = Electrical.contentRegistry.newItem("insulation", classOf[ItemResourcePart])
    Electrical.blockSolarPanel = Electrical.contentRegistry.newBlock(classOf[TileSolarPanel])
    Electrical.blockMotor = Electrical.contentRegistry.newBlock(classOf[TileMotor])
    Electrical.blockThermopile = Electrical.contentRegistry.newBlock(classOf[TileThermopile])
    Electrical.itemQuantumGlyph = Electrical.contentRegistry.newItem(classOf[ItemQuantumGlyph])
    Settings.config.save
    OreDictionary.registerOre("wire", Electrical.itemWire)
    OreDictionary.registerOre("motor", Electrical.blockMotor)
    OreDictionary.registerOre("battery", ItemBlockBattery.setTier(new ItemStack(Electrical.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
    OreDictionary.registerOre("batteryBox", ItemBlockBattery.setTier(new ItemStack(Electrical.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
    ResonantTab.itemStack(new ItemStack(Electrical.itemTransformer))
    for (material <- EnumWireMaterial.values) {
      material.setWire(Electrical.itemWire)
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
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.blockTesla, "WEW", " C ", "DID", 'W', "wire", 'E', Items.ender_eye, 'C', UniversalRecipe.BATTERY.get, 'D', Items.diamond, 'I', UniversalRecipe.PRIMARY_PLATE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.itemMultimeter, "WWW", "ICI", 'W', "wire", 'C', UniversalRecipe.BATTERY.get, 'I', UniversalRecipe.PRIMARY_METAL.get))
    val tierOneBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(Electrical.blockBattery, 1, 0), 0.asInstanceOf[Byte])
    val tierTwoBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(Electrical.blockBattery, 1, 0), 1.asInstanceOf[Byte])
    val tierThreeBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(Electrical.blockBattery, 1, 0), 2.asInstanceOf[Byte])
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
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.itemCharger, "WWW", "ICI", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get, 'C', UniversalRecipe.CIRCUIT_T1.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.itemTransformer, "WWW", "WWW", "III", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Electrical.itemQuantumGlyph, 1, 0), " CT", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', Electrical.itemLevitator, 'C', Electrical.itemCharger, 'T', Electrical.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Electrical.itemQuantumGlyph, 1, 1), "TCT", "LBL", " CT", 'B', Blocks.diamond_block, 'L', Electrical.itemLevitator, 'C', Electrical.itemCharger, 'T', Electrical.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Electrical.itemQuantumGlyph, 1, 2), "TC ", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', Electrical.itemLevitator, 'C', Electrical.itemCharger, 'T', Electrical.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Electrical.itemQuantumGlyph, 1, 3), "TCT", "LBL", "TC ", 'B', Blocks.diamond_block, 'L', Electrical.itemLevitator, 'C', Electrical.itemCharger, 'T', Electrical.blockTesla))
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.blockSolarPanel, "CCC", "WWW", "III", 'W', "wire", 'C', Items.coal, 'I', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.blockMotor, "SRS", "SMS", "SWS", 'W', "wire", 'R', Items.redstone, 'M', Blocks.iron_block, 'S', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.blockThermopile, "ORO", "OWO", "OOO", 'W', "wire", 'O', Blocks.obsidian, 'R', Items.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(Electrical.itemLaserGun, "RDR", "RDR", "ICB", 'R', Items.redstone, 'D', Items.diamond, 'I', Items.gold_ingot, 'C', UniversalRecipe.CIRCUIT_T2.get, 'B', ItemBlockBattery.setTier(new ItemStack(Electrical.blockBattery, 1, 0), 0.asInstanceOf[Byte])))
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