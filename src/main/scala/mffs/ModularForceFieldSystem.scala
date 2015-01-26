package mffs

import java.util.UUID

import com.mojang.authlib.GameProfile
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, SidedProxy}
import mffs.security.MFFSPermissions
import mffs.util.FortronUtility
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fluids.FluidRegistry
import org.modstats.{ModstatInfo, Modstats}
import resonantengine.api.mffs.Blacklist
import resonantengine.core.network.netty.PacketManager
import resonantengine.lib.mod.config.ConfigHandler
import resonantengine.lib.mod.loadable.LoadableHandler
import resonantengine.lib.prefab.damage.CustomDamageSource

@Mod(modid = Reference.id, name = Reference.name, version = Reference.version, dependencies = "required-after:ResonantEngine", modLanguage = "scala", guiFactory = "mffs.MFFSGuiFactory")
@ModstatInfo(prefix = "mffs")
object ModularForceFieldSystem
{
  /**
   * Damages
   */
  val damageFieldShock = new CustomDamageSource("fieldShock").setDamageBypassesArmor
  val fakeProfile = new GameProfile(UUID.randomUUID, "mffs")
  val packetHandler = new PacketManager(Reference.channel)
  val loadables = new LoadableHandler
  @SidedProxy(clientSide = "mffs.ClientProxy", serverSide = "mffs.CommonProxy")
  var proxy: CommonProxy = _

  @EventHandler
  def preInit(event: FMLPreInitializationEvent)
  {
    Settings.config = new Configuration(event.getSuggestedConfigurationFile)

    /**
     * Registration
     */
    Modstats.instance.getReporter.registerMod(this)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
    MinecraftForge.EVENT_BUS.register(Settings)

    ConfigHandler.sync(Settings, Settings.config)

    loadables.applyModule(proxy)
    loadables.applyModule(packetHandler)
    loadables.applyModule(Content)

    Settings.config.load

    loadables.preInit()

    MinecraftForge.EVENT_BUS.register(Content.remoteController)

    /**
     * Fluid Instantiation
     */
    FortronUtility.fluidFortron.setGaseous(true)
    FluidRegistry.registerFluid(FortronUtility.fluidFortron)

    Settings.config.save()
  }

  @EventHandler
  def load(evt: FMLInitializationEvent)
  {
    loadables.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    Settings.config.load()

    /**
     * Add to black lists
     */
    Blacklist.stabilizationBlacklist.add(Blocks.water)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_water)
    Blacklist.stabilizationBlacklist.add(Blocks.lava)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_lava)

    Blacklist.disintegrationBlacklist.add(Blocks.water)
    Blacklist.disintegrationBlacklist.add(Blocks.flowing_water)
    Blacklist.disintegrationBlacklist.add(Blocks.lava)
    Blacklist.disintegrationBlacklist.add(Blocks.flowing_lava)

    Blacklist.mobilizerBlacklist.add(Blocks.bedrock)
    Blacklist.mobilizerBlacklist.add(Content.forceField)

    try
    {
      val clazz = Class.forName("ic2.api.tile.ExplosionWhitelist")
      clazz.getMethod("addWhitelistedBlock", classOf[Block]).invoke(null, Content.forceField)
    }
    catch
      {
        case _: Throwable => Reference.logger.info("IC2 Explosion white list API not found. Ignoring...")
      }

    //Inititate MFFS Permissions
    MFFSPermissions

    loadables.postInit()

    Settings.config.save()
  }

}