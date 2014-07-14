package resonantinduction.core

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent, FMLServerStartingEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{FMLCommonHandler, FMLLog, Mod, SidedProxy}
import mffs.{Settings, Content}
import net.minecraft.block.Block
import net.minecraft.command.{ICommandManager, ServerCommandManager}
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.oredict.ShapelessOreRecipe
import org.modstats.{ModstatInfo, Modstats}
import resonant.api.recipe.MachineRecipes
import resonant.lib.config.ConfigHandler
import resonant.lib.loadable.LoadableHandler
import resonant.lib.network.netty.PacketManager
import resonant.lib.utility.LanguageUtility
import resonantinduction.core.blocks.IndustrialStoneBlocksRecipes
import resonantinduction.core.handler.TextureHookHandler
import resonantinduction.core.items.{ItemDevStaff, ItemFlour}
import resonantinduction.core.prefab.part.PacketMultiPart
import resonantinduction.core.resource.fluid.{ItemOreResourceBucket, TileFluidMixture}
import resonantinduction.core.resource.{BlockDust, ItemBiomass, ItemOreResource, ResourceGenerator, TileDust}

/** The core module of Resonant Induction
  *
  * @author Calclavia */
@Mod(modid = ResonantInduction.ID, name = ResonantInduction.NAME, version = Reference.VERSION, modLanguage = "scala", dependencies = "required-after:ForgeMultipart@[1.0.0.244,);required-after:ResonantEngine;before:ThermalExpansion;before:Mekanism")
@ModstatInfo(prefix = "resonantin")
object ResonantInduction
{
  /** Mod Information */
  final val ID: String = Reference.idPrefix + ":Core"

  /** Packets */
  val packetHandler = new PacketManager(Reference.channel)
  val loadables = new LoadableHandler

  @SidedProxy(clientSide = "resonantinduction.core.ClientProxy", serverSide = "resonantinduction.core.CommonProxy")
  var proxy: CommonProxy = _

  /** Recipe Types */
  object RecipeType extends Enumeration
  {
    final val CRUSHER, GRINDER, MIXER, SMELTER, SAWMILL = Value
  }

  @EventHandler
  def preInit(evt: FMLPreInitializationEvent)
  {
    /**
     * Registrations
     */
    NetworkRegistry.instance.registerGuiHandler(this, proxy)
    Modstats.instance.getReporter.registerMod(this)

    Settings.config = new Configuration(event.getSuggestedConfigurationFile)
    ConfigHandler.sync(Settings, Settings.config)

    MinecraftForge.EVENT_BUS.register(ResourceGenerator.INSTANCE)
    MinecraftForge.EVENT_BUS.register(new TextureHookHandler)

    loadables.applyModule(proxy)
    loadables.applyModule(packetHandler)
    loadables.applyModule(CoreContent)

    proxy.preInit()

    ResonantTab.itemStack = new ItemStack(blockIndustrialStone)
  }

  @EventHandler
  def init(evt: FMLInitializationEvent)
  {
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