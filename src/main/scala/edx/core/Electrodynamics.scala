package edx.core

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, SidedProxy}
import edx.basic.BasicContent
import edx.core.handler.TextureHookHandler
import edx.core.resource.AutoResourceFactory
import edx.electrical.ElectricalContent
import edx.mechanical.{MechanicalContent, MicroblockHighlightHandler}
import edx.quantum.QuantumContent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import org.modstats.{ModstatInfo, Modstats}
import resonant.engine.ResonantEngine
import resonant.lib.mod.config.ConfigHandler
import resonant.lib.mod.loadable.LoadableHandler

/** The core module of Resonant Induction
  *
  * @author Calclavia */
@Mod(modid = Reference.id, name = Reference.name, version = Reference.version, modLanguage = "scala", dependencies = "required-after:ForgeMultipart@[1.0.0.244,);required-after:ResonantEngine;before:ThermalExpansion;before:Mekanism")
@ModstatInfo(prefix = "edx")
object Electrodynamics
{
  /** Packets */
  val packetHandler = ResonantEngine.packetHandler
  val loadables = new LoadableHandler

  @SidedProxy(clientSide = "edx.core.ClientProxy", serverSide = "edx.core.CommonProxy")
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
    MinecraftForge.EVENT_BUS.register(AutoResourceFactory)

    loadables.applyModule(proxy)
    loadables.applyModule(packetHandler)
    loadables.applyModule(BasicContent)
    loadables.applyModule(ElectricalContent)
    loadables.applyModule(MechanicalContent)
    loadables.applyModule(QuantumContent)

    loadables.preInit()
  }

  @EventHandler
  def init(evt: FMLInitializationEvent)
  {
    ResonantPartFactory.init()
    AutoResourceFactory.init()
    loadables.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    AutoResourceFactory.postInit()
    loadables.postInit()
  }
}