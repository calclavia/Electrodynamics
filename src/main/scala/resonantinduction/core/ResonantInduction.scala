package resonantinduction.core

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import org.modstats.{ModstatInfo, Modstats}
import resonant.engine.{References, ResonantEngine}
import resonant.lib.config.ConfigHandler
import resonant.lib.loadable.LoadableHandler
import resonantinduction.core.handler.TextureHookHandler
import resonantinduction.core.resource.ResourceGenerator

import scala.collection.convert.wrapAll._

/** The core module of Resonant Induction
  *
  * @author Calclavia */
@Mod(modid = Reference.coreID, name = Reference.name, version = Reference.version, modLanguage = "scala", dependencies = "required-after:ForgeMultipart@[1.0.0.244,);required-after:ResonantEngine;before:ThermalExpansion;before:Mekanism")
@ModstatInfo(prefix = "resonantin")
object ResonantInduction
{
  /** Packets */
  val packetHandler = ResonantEngine.instance.packetHandler
  val loadables = new LoadableHandler

  @SidedProxy(clientSide = "resonantinduction.core.ClientProxy", serverSide = "resonantinduction.core.CommonProxy")
  var proxy: CommonProxy = _

  @Mod.Metadata(References.ID)
  var metadata: ModMetadata = null

  @EventHandler
  def preInit(evt: FMLPreInitializationEvent)
  {
    /**
     * Registrations
     */
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    Modstats.instance.getReporter.registerMod(this)

    Settings.config = new Configuration(evt.getSuggestedConfigurationFile)
    ConfigHandler.sync(Settings, Settings.config)

    MinecraftForge.EVENT_BUS.register(ResourceGenerator)
    MinecraftForge.EVENT_BUS.register(new TextureHookHandler)

    loadables.applyModule(proxy)
    loadables.applyModule(packetHandler)
    loadables.applyModule(CoreContent)

    proxy.preInit()

    ResonantTab.itemStack = new ItemStack(CoreContent.decoration)
  }

  @EventHandler
  def init(evt: FMLInitializationEvent)
  {
    if(ResonantInduction.metadata != null) {
      ResonantInduction.metadata.modId = Reference.name
      ResonantInduction.metadata.name = Reference.name
      ResonantInduction.metadata.description = Reference.name + " is a mod... TODO add description"
      ResonantInduction.metadata.url = "http://calclavia.com/resonant-induction/"
      ResonantInduction.metadata.version = Reference.version + Reference.build
      ResonantInduction.metadata.authorList = List[String]("Calclavia", "DarkCow", "tgame14", "Maxwolf")
      ResonantInduction.metadata.autogenerated = false
    }

    ResourceGenerator.generateOreResources()
    proxy.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    /*
    GameRegistry.addRecipe(new ShapelessOreRecipe(itemFlour, Array[AnyRef](Item.wheat, Item.wheat)))
    FurnaceRecipes.smelting.addSmelting(itemFlour.itemID, 1, new ItemStack(Item.bread), 50f)
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemFlour, 1, 1), Array[AnyRef](itemFlour, Item.bucketWater)))
    MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name, Item.wheat, itemFlour)
    */
    proxy.postInit()
  }
}