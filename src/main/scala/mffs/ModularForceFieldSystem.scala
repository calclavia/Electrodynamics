package mffs

import java.util.UUID

import com.mojang.authlib.GameProfile
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, SidedProxy}
import ic2.api.tile.ExplosionWhitelist
import mffs.util.FortronUtility
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack}
import org.modstats.{ModstatInfo, Modstats}
import resonant.api.mffs.Blacklist
import resonant.lib.config.ConfigHandler
import resonant.lib.loadable.LoadableHandler
import resonant.lib.network.netty.PacketManager
import resonant.lib.prefab.damage.CustomDamageSource

import scala.collection.convert.wrapAll._

@Mod(modid = Reference.id, name = Reference.name, version = Reference.version, dependencies = "required-after:ResonantEngine", modLanguage = "scala", guiFactory = "mffs.MFFSGuiFactory", useMetadata = true)
@ModstatInfo(prefix = "mffs")
object ModularForceFieldSystem
{
  @SidedProxy(clientSide = "mffs.ClientProxy", serverSide = "mffs.CommonProxy")
  var proxy: CommonProxy = _

  /**
   * Damages
   */
  val damageFieldShock = new CustomDamageSource("fieldShock").setDamageBypassesArmor
  val fakeProfile = new GameProfile(UUID.randomUUID, "mffs")

  val packetHandler = new PacketManager(Reference.channel)
  val modProxy = new LoadableHandler

  @EventHandler
  def preInit(event: FMLPreInitializationEvent)
  {
    Settings.config = new Configuration(event.getSuggestedConfigurationFile)
    ConfigHandler.sync(Settings, Settings.config)

    modProxy.applyModule(packetHandler)
    modProxy.applyModule(Content)

    Settings.config.load

    modProxy.preInit()

    /**
     * Registration
     */
    Modstats.instance.getReporter.registerMod(this)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
    MinecraftForge.EVENT_BUS.register(Content.remoteController)

    /**
     * Fluid Instantiation
     */
    FortronUtility.FLUID_FORTRON = new Fluid("fortron")
    FortronUtility.FLUID_FORTRON.setGaseous(true)
    FluidRegistry.registerFluid(FortronUtility.FLUID_FORTRON)
    FortronUtility.FLUIDSTACK_FORTRON = new FluidStack(FortronUtility.FLUID_FORTRON, 0)

    Settings.config.save()
  }

  @EventHandler
  def load(evt: FMLInitializationEvent)
  {
    modProxy.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    Settings.config.load()

    Blacklist.stabilizationBlacklist.addAll(Settings.stabilizationBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.stabilizationBlacklist.add(Blocks.water)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_water)
    Blacklist.stabilizationBlacklist.add(Blocks.lava)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_lava)

    Blacklist.disintegrationBlacklist.addAll(Settings.disintegrationBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.disintegrationBlacklist.add(Blocks.water)
    Blacklist.disintegrationBlacklist.add(Blocks.flowing_water)
    Blacklist.disintegrationBlacklist.add(Blocks.lava)
    Blacklist.disintegrationBlacklist.add(Blocks.flowing_lava)

    Blacklist.mobilizerBlacklist.addAll(Settings.mobilizerBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.mobilizerBlacklist.add(Blocks.bedrock)
    Blacklist.mobilizerBlacklist.add(Content.forceField)
    ExplosionWhitelist.addWhitelistedBlock(Content.forceField)

    modProxy.postInit()

    Settings.config.save()
  }

}