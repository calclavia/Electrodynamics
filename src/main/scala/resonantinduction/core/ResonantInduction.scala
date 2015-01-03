package resonantinduction.core

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import org.modstats.{ModstatInfo, Modstats}
import resonant.engine.{References, ResonantEngine}
import resonant.lib.mod.config.ConfigHandler
import resonant.lib.mod.loadable.LoadableHandler
import resonantinduction.archaic.ArchaicContent
import resonantinduction.atomic.AtomicContent
import resonantinduction.core.handler.TextureHookHandler
import resonantinduction.core.resource.ResourceFactory
import resonantinduction.electrical.ElectricalContent
import resonantinduction.mechanical.{MechanicalContent, MicroblockHighlightHandler}

import scala.collection.convert.wrapAll._

/** The core module of Resonant Induction
  *
  * @author Calclavia */
@Mod(modid = Reference.coreID, name = Reference.name, version = Reference.version, modLanguage = "scala", dependencies = "required-after:ForgeMultipart@[1.0.0.244,);required-after:ResonantEngine;before:ThermalExpansion;before:Mekanism")
@ModstatInfo(prefix = "resonantin")
final object ResonantInduction
{
  /** Packets */
  val packetHandler = ResonantEngine.instance.packetHandler
  val loadables = new LoadableHandler

  @SidedProxy(clientSide = "resonantinduction.core.ClientProxy", serverSide = "resonantinduction.core.CommonProxy")
  var proxy: CommonProxy = _

  @EventHandler
  def preInit(evt: FMLPreInitializationEvent)
  {
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    Modstats.instance.getReporter.registerMod(this)

    Settings.config = new Configuration(evt.getSuggestedConfigurationFile)
    ConfigHandler.sync(Settings, Settings.config)

    MinecraftForge.EVENT_BUS.register(TextureHookHandler)
    MinecraftForge.EVENT_BUS.register(MicroblockHighlightHandler)
    MinecraftForge.EVENT_BUS.register(ResourceFactory)

    loadables.applyModule(proxy)
    loadables.applyModule(packetHandler)
    loadables.applyModule(ArchaicContent)
    loadables.applyModule(ElectricalContent)
    loadables.applyModule(MechanicalContent)
    loadables.applyModule(AtomicContent)

    loadables.preInit()
  }

  @EventHandler
  def init(evt: FMLInitializationEvent)
  {
    ResonantPartFactory.init()
    ResourceFactory.init()
    loadables.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    ResourceFactory.generateAll()
    loadables.postInit()
  }
}