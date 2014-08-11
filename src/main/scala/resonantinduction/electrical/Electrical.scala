package resonantinduction.electrical

import cpw.mods.fml.common.Mod.{EventHandler, Instance}
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import resonant.content.loader.ModManager
import resonant.lib.loadable.LoadableHandler
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
    Electrical.proxy.postInit
    modproxies.postInit
  }

  var modproxies: LoadableHandler = null
}