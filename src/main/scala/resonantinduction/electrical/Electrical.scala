package resonantinduction.electrical

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.oredict.OreDictionary
import resonant.content.loader.ModManager
import resonant.lib.loadable.LoadableHandler
import resonantinduction.atomic.gate.{ItemQuantumGlyph, PartQuantumGlyph}
import resonantinduction.core.{Reference, ResonantPartFactory, ResonantTab, Settings}
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
import resonantinduction.electrical.wire.flat.{PartFlatWire, RenderFlatWire}
import resonantinduction.electrical.wire.framed.{PartFramedWire, RenderFramedWire}

/** Resonant Induction Electrical Module
  *
  * @author Calclavia
  */
@Mod(modid = "ResonantInduction|Electrical", name = "Resonant Induction Electrical", version = Reference.version, dependencies = "before:ThermalExpansion;before:Mekanism;after:ResonantInduction|Mechanical;required-after:" + Reference.coreID, modLanguage = "scala")
object Electrical
{
  /** Mod Information */
  final val ID = "ResonantInduction|Electrical"
  final val NAME = Reference.name + " Electrical"

  var INSTANCE = this

  @SidedProxy(clientSide = "resonantinduction.electrical.ClientProxy", serverSide = "resonantinduction.electrical.CommonProxy")
  var proxy: CommonProxy = null

  @Mod.Metadata("ResonantInduction|Electrical")
  var metadata: ModMetadata = null

  @deprecated
  val contentRegistry: ModManager = new ModManager().setPrefix(Reference.prefix).setTab(ResonantTab.tab)

  val loadable = new LoadableHandler()

  @EventHandler
  def preInit(evt: FMLPreInitializationEvent)
  {
    NetworkRegistry.INSTANCE.registerGuiHandler(this, Electrical.proxy)
    MinecraftForge.EVENT_BUS.register(this)

    Settings.config.load()
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
    ResonantPartFactory.register(classOf[PartQuantumGlyph])

    Electrical.proxy.preInit
    loadable.preInit()
  }

  @EventHandler
  def init(evt: FMLInitializationEvent)
  {
    Electrical.proxy.init
    loadable.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    Electrical.proxy.postInit
    loadable.postInit()
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