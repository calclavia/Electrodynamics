package resonantinduction.electrical

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.oredict.OreDictionary
import resonant.content.loader.ModManager
import resonant.lib.loadable.LoadableHandler
import resonantinduction.atomic.gate.{PartQuantumGlyph, ItemQuantumGlyph}
import resonantinduction.core.{ResonantPartFactory, Reference, ResonantTab, Settings}
import resonantinduction.electrical.battery.{ItemBlockBattery, TileBattery}
import resonantinduction.electrical.generator.{TileMotor, TileSolarPanel, TileThermopile}
import resonantinduction.electrical.laser.emitter.{BlockLaserEmitter, TileLaserEmitter}
import resonantinduction.electrical.laser.focus.ItemFocusingMatrix
import resonantinduction.electrical.laser.focus.crystal.{BlockFocusCrystal, TileFocusCrystal}
import resonantinduction.electrical.laser.focus.mirror.{BlockMirror, TileMirror}
import resonantinduction.electrical.laser.receiver.{BlockLaserReceiver, TileLaserReceiver}
import resonantinduction.electrical.levitator.{PartLevitator, ItemLevitator}
import resonantinduction.electrical.multimeter.{PartMultimeter, ItemMultimeter}
import resonantinduction.electrical.tesla.TileTesla
import resonantinduction.electrical.transformer.{PartElectricTransformer, ItemElectricTransformer}
import resonantinduction.electrical.wire.ItemWire
import resonantinduction.electrical.wire.base.WireMaterial
import resonantinduction.electrical.wire.flat.PartFlatWire
import resonantinduction.electrical.wire.framed.PartFramedWire

/** Resonant Induction Electrical Module
  *
  * @author Calclavia */

@Mod(modid = "ResonantInduction|Electrical", name = "Resonant Induction Electrical", version = Reference.version, dependencies = "before:ThermalExpansion;before:Mekanism;after:ResonantInduction|Mechanical;required-after:" + Reference.coreID, modLanguage = "scala")
object Electrical
{
  /** Mod Information */
  final val ID: String = "ResonantInduction|Electrical"
  final val NAME: String = Reference.name + " Electrical"

  var INSTANCE = this

  @SidedProxy(clientSide = "resonantinduction.electrical.ClientProxy", serverSide = "resonantinduction.electrical.CommonProxy")
  var proxy: CommonProxy = null

  @Mod.Metadata("ResonantInduction|Electrical")
  var metadata: ModMetadata = null

  final val contentRegistry: ModManager = new ModManager().setPrefix(Reference.prefix).setTab(ResonantTab.tab)

  var modproxies: LoadableHandler = null

  @EventHandler
  def preInit(evt: FMLPreInitializationEvent)
  {
    modproxies = new LoadableHandler
    NetworkRegistry.INSTANCE.registerGuiHandler(this, Electrical.proxy)
    Settings.config.load
    //ElectromagneticCoherence content TODO convert
    ElectricalContent.blockLaserEmitter = new BlockLaserEmitter()
    ElectricalContent.blockLaserReceiver = new BlockLaserReceiver()
    ElectricalContent.blockMirror = new BlockMirror()
    ElectricalContent.blockFocusCrystal = new BlockFocusCrystal()

    GameRegistry.registerTileEntity(classOf[TileLaserEmitter], "EMLaserEmitter");
    GameRegistry.registerTileEntity(classOf[TileLaserReceiver], "EMLaserReceiver");
    GameRegistry.registerTileEntity(classOf[TileMirror], "EMLaserMirror");
    GameRegistry.registerTileEntity(classOf[TileFocusCrystal], "EMFocusCrystal");

    ElectricalContent.itemFocusingMatrix = new ItemFocusingMatrix()

    GameRegistry.registerBlock(ElectricalContent.blockLaserEmitter, "LaserEmitter")
    GameRegistry.registerBlock(ElectricalContent.blockLaserReceiver, "LaserReceiver")
    GameRegistry.registerBlock(ElectricalContent.blockMirror, "Mirror")
    GameRegistry.registerBlock(ElectricalContent.blockFocusCrystal, "FocusCrystal")

    GameRegistry.registerItem(ElectricalContent.itemFocusingMatrix, "FocusingMatrix")


    ElectricalContent.itemWire = Electrical.contentRegistry.newItem(classOf[ItemWire])
    ElectricalContent.itemMultimeter = Electrical.contentRegistry.newItem(classOf[ItemMultimeter])
    ElectricalContent.itemTransformer = Electrical.contentRegistry.newItem(classOf[ItemElectricTransformer])
    ElectricalContent.blockTesla = Electrical.contentRegistry.newBlock(classOf[TileTesla])
    ElectricalContent.blockBattery = Electrical.contentRegistry.newBlock(classOf[TileBattery])
    ElectricalContent.itemLevitator = Electrical.contentRegistry.newItem(classOf[ItemLevitator])
    ElectricalContent.itemInsulation = Electrical.contentRegistry.newItem("insulation", classOf[Item])
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


    /**
     * Register all parts
     */
    ResonantPartFactory.register(classOf[PartFramedWire])
    ResonantPartFactory.register(classOf[PartFlatWire])
    ResonantPartFactory.register(classOf[PartMultimeter])
    ResonantPartFactory.register(classOf[PartElectricTransformer])
    ResonantPartFactory.register(classOf[PartLevitator])
    ResonantPartFactory.register(classOf[PartQuantumGlyph])

    Electrical.proxy.preInit
    modproxies.preInit()
  }

  @EventHandler def init(evt: FMLInitializationEvent)
  {
    Electrical.proxy.init
    modproxies.init()
  }

  @EventHandler def postInit(evt: FMLPostInitializationEvent)
  {
    Electrical.proxy.postInit
    modproxies.postInit()
  }
}