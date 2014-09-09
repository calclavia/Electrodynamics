package resonantinduction.atomic

import java.util.List

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.eventhandler.{Event, SubscribeEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.{EntityRegistry, GameRegistry}
import cpw.mods.fml.common.{Loader, Mod, ModMetadata, SidedProxy}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items
import net.minecraft.item.{ItemBucket, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.{ForgeChunkManager, MinecraftForge}
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.fluids.{Fluid, FluidContainerRegistry, FluidRegistry, FluidStack}
import net.minecraftforge.oredict.{OreDictionary, ShapelessOreRecipe}
import resonant.api.IElectromagnet
import resonant.api.event.PlasmaEvent
import resonant.api.recipe.QuantumAssemblerRecipes
import resonant.content.loader.ModManager
import resonant.engine.content.debug.TileCreativeBuilder
import resonant.engine.grid.thermal.EventThermal
import resonant.lib.ore.OreGenReplaceStone
import resonant.lib.render.RenderUtility
import resonantinduction.atomic.blocks._
import resonantinduction.atomic.items._
import resonantinduction.atomic.machine.TileFunnel
import resonantinduction.atomic.machine.accelerator.{EntityParticle, TileAccelerator}
import resonantinduction.atomic.machine.boiler.TileNuclearBoiler
import resonantinduction.atomic.machine.centrifuge.TileCentrifuge
import resonantinduction.atomic.machine.extractor.TileChemicalExtractor
import resonantinduction.atomic.machine.fulmination.{FulminationHandler, TileFulmination}
import resonantinduction.atomic.machine.plasma.{TilePlasma, TilePlasmaHeater}
import resonantinduction.atomic.machine.quantum.TileQuantumAssembler
import resonantinduction.atomic.machine.reactor.{TileControlRod, TileReactorCell}
import resonantinduction.atomic.machine.thermometer.TileThermometer
import resonantinduction.atomic.schematic.{SchematicAccelerator, SchematicBreedingReactor, SchematicFissionReactor, SchematicFusionReactor}
import resonantinduction.core.{Reference, ResonantTab, Settings}
import universalelectricity.core.transform.vector.VectorWorld

@Mod(modid = "ResonantInduction|Atomic", name = "Resonant Induction Atomic", version = Reference.version, dependencies = "required-after:ResonantEngine;after:IC2;after:ResonantInduction|Electrical;required-after:" + Reference.coreID, modLanguage = "scala")
object Atomic
{
  final val ID: String = "ResonantInduction|Atomic"
  final val ENTITY_ID_PREFIX: Int = 49
  final val SECOND_IN_TICKS: Int = 20
  final val NAME: String = Reference.name + " Atomic"
  final val contentRegistry: ModManager = new ModManager().setPrefix(Reference.prefix).setTab(ResonantTab)
  private final val SUPPORTED_LANGUAGES: Array[String] = Array[String]("en_US", "pl_PL", "de_DE", "ru_RU")
  var INSTANCE = this
  @SidedProxy(clientSide = "resonantinduction.atomic.ClientProxy", serverSide = "resonantinduction.atomic.CommonProxy")
  var proxy: CommonProxy = null
  @Mod.Metadata("ResonantInduction|Atomic")
  var metadata: ModMetadata = null

  /** Is this ItemStack a cell?
    *
    * @param itemStack
    * @return */
  def isItemStackEmptyCell(itemStack: ItemStack): Boolean =
  {
    return isItemStackOreDictionaryCompatible(itemStack, "cellEmpty")
  }

  /** Compare to Ore Dict
    *
    * @param itemStack
    * @return */
  def isItemStackOreDictionaryCompatible(itemStack: ItemStack, names: String*): Boolean =
  {
    if (itemStack != null && names != null && names.length > 0)
    {
      val name: String = OreDictionary.getOreName(OreDictionary.getOreID(itemStack))
      for (compareName <- names)
      {
        if (name == compareName)
        {
          return true
        }
      }
    }
    return false
  }

  def isItemStackWaterCell(itemStack: ItemStack): Boolean =
  {
    return isItemStackOreDictionaryCompatible(itemStack, "cellWater")
  }

  def isItemStackUraniumOre(itemStack: ItemStack): Boolean =
  {
    return isItemStackOreDictionaryCompatible(itemStack, "dropUranium", "oreUranium")
  }

  def isItemStackDeuteriumCell(itemStack: ItemStack): Boolean =
  {
    return isItemStackOreDictionaryCompatible(itemStack, "molecule_1d", "molecule_1h2", "cellDeuterium")
  }

  def isItemStackTritiumCell(itemStack: ItemStack): Boolean =
  {
    return isItemStackOreDictionaryCompatible(itemStack, "molecule_h3", "cellTritium")
  }

  def getFluidAmount(fluid: FluidStack): Int =
  {
    if (fluid != null)
    {
      return fluid.amount
    }
    return 0
  }

  @EventHandler def preInit(event: FMLPreInitializationEvent)
  {
    Atomic.INSTANCE = this
    MinecraftForge.EVENT_BUS.register(this)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, Atomic.proxy)
    TileCreativeBuilder.register(new SchematicAccelerator)
    TileCreativeBuilder.register(new SchematicBreedingReactor)
    TileCreativeBuilder.register(new SchematicFissionReactor)
    TileCreativeBuilder.register(new SchematicFusionReactor)
    Settings.config.load
    AtomicContent.FLUID_URANIUM_HEXAFLOURIDE = new Fluid("uraniumhexafluoride").setGaseous(true)
    AtomicContent.FLUID_STEAM = new Fluid("steam").setGaseous(true)
    AtomicContent.FLUID_DEUTERIUM = new Fluid("deuterium").setGaseous(true)
    AtomicContent.FLUID_TRITIUM = new Fluid("tritium").setGaseous(true)
    AtomicContent.FLUID_TOXIC_WASTE = new Fluid("toxicwaste")
    AtomicContent.FLUID_PLASMA = new Fluid("plasma").setGaseous(true)
    FluidRegistry.registerFluid(AtomicContent.FLUID_URANIUM_HEXAFLOURIDE)
    FluidRegistry.registerFluid(AtomicContent.FLUID_STEAM)
    FluidRegistry.registerFluid(AtomicContent.FLUID_TRITIUM)
    FluidRegistry.registerFluid(AtomicContent.FLUID_DEUTERIUM)
    FluidRegistry.registerFluid(AtomicContent.FLUID_TOXIC_WASTE)
    FluidRegistry.registerFluid(AtomicContent.FLUID_PLASMA)
    AtomicContent.FLUIDSTACK_WATER = new FluidStack(FluidRegistry.WATER, 0)
    AtomicContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE = new FluidStack(AtomicContent.FLUID_URANIUM_HEXAFLOURIDE, 0)
    AtomicContent.FLUIDSTACK_STEAM = new FluidStack(FluidRegistry.getFluidID("steam"), 0)
    AtomicContent.FLUIDSTACK_DEUTERIUM = new FluidStack(FluidRegistry.getFluidID("deuterium"), 0)
    AtomicContent.FLUIDSTACK_TRITIUM = new FluidStack(FluidRegistry.getFluidID("tritium"), 0)
    AtomicContent.FLUIDSTACK_TOXIC_WASTE = new FluidStack(FluidRegistry.getFluidID("toxicwaste"), 0)

    AtomicContent.blockRadioactive = new BlockRadioactive(Material.rock).setBlockName(Reference.prefix + "radioactive").setBlockTextureName(Reference.prefix + "radioactive").setCreativeTab(CreativeTabs.tabBlock)
    AtomicContent.blockUraniumOre = new BlockUraniumOre
    AtomicContent.blockToxicWaste = new BlockToxicWaste().setCreativeTab(null)

    GameRegistry.registerBlock(AtomicContent.blockRadioactive, "blockRadioactive");
    GameRegistry.registerBlock(AtomicContent.blockUraniumOre, "blockUraniumOre");
    GameRegistry.registerBlock(AtomicContent.blockToxicWaste, "blockToxicWaste");

    AtomicContent.blockCentrifuge = Atomic.contentRegistry.newBlock(classOf[TileCentrifuge])
    AtomicContent.blockReactorCell = Atomic.contentRegistry.newBlock(classOf[TileReactorCell])
    AtomicContent.blockNuclearBoiler = Atomic.contentRegistry.newBlock(classOf[TileNuclearBoiler])
    AtomicContent.blockChemicalExtractor = Atomic.contentRegistry.newBlock(classOf[TileChemicalExtractor])
    AtomicContent.blockFusionCore = Atomic.contentRegistry.newBlock(classOf[TilePlasmaHeater])
    AtomicContent.blockControlRod = Atomic.contentRegistry.newBlock(classOf[TileControlRod])
    AtomicContent.blockThermometer = Atomic.contentRegistry.newBlock(classOf[TileThermometer])
    AtomicContent.blockPlasma = Atomic.contentRegistry.newBlock(classOf[TilePlasma])
    AtomicContent.blockElectromagnet = Atomic.contentRegistry.newBlock(classOf[TileElectromagnet])
    AtomicContent.blockSiren = Atomic.contentRegistry.newBlock(classOf[TileSiren])
    AtomicContent.blockSteamFunnel = Atomic.contentRegistry.newBlock(classOf[TileFunnel])
    AtomicContent.blockAccelerator = Atomic.contentRegistry.newBlock(classOf[TileAccelerator])
    AtomicContent.blockFulmination = Atomic.contentRegistry.newBlock(classOf[TileFulmination])
    AtomicContent.blockQuantumAssembler = Atomic.contentRegistry.newBlock(classOf[TileQuantumAssembler])

    AtomicContent.itemHazmatTop = new ItemHazmat("HazmatMask", 0).setCreativeTab(ResonantTab)
    AtomicContent.itemHazmatBody = new ItemHazmat("HazmatBody", 1).setCreativeTab(ResonantTab)
    AtomicContent.itemHazmatLeggings = new ItemHazmat("HazmatLeggings", 2).setCreativeTab(ResonantTab)
    AtomicContent.itemHazmatBoots = new ItemHazmat("HazmatBoots", 3).setCreativeTab(ResonantTab)
    AtomicContent.itemCell = new ItemCell("cellEmpty")
    AtomicContent.itemFissileFuel = new ItemFissileFuel()
    AtomicContent.itemDeuteriumCell = new ItemCell("cellDeuterium")
    AtomicContent.itemTritiumCell = new ItemCell("cellTritium")
    AtomicContent.itemWaterCell = new ItemCell("cellWater")
    AtomicContent.itemDarkMatter = new ItemCell("darkMatter")
    AtomicContent.itemAntimatter = new ItemAntimatter()
    AtomicContent.itemBreedingRod = new ItemBreederFuel()
    AtomicContent.itemYellowCake = new ItemRadioactive().setUnlocalizedName("yellowcake").setTextureName("yellowcake").setCreativeTab(ResonantTab)
    AtomicContent.itemUranium = Atomic.contentRegistry.newItem(classOf[ItemUranium]).setCreativeTab(ResonantTab)

    GameRegistry.registerItem(AtomicContent.itemHazmatTop, "HazmatMask", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemHazmatBody, "HazmatBody", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemHazmatLeggings, "HazmatLeggins", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemHazmatBoots, "HazmatBoots", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemCell, "cellEmpty", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemFissileFuel, "FissileFuel", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemDeuteriumCell, "DeuteriumCell", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemTritiumCell, "TritiumCell", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemWaterCell, "WaterCell", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemDarkMatter, "DarkMatter", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemAntimatter, "ItemAntimatter", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemBreedingRod, "BreedingRod", "ResonantInduction|Atomic");
    GameRegistry.registerItem(AtomicContent.itemYellowCake, "YellowCake", "ResonantInduction|Atomic");

    AtomicContent.FLUID_PLASMA.setBlock(AtomicContent.blockPlasma)
    AtomicContent.itemBucketToxic = new ItemBucket(AtomicContent.blockPlasma).setCreativeTab(ResonantTab.tab).setUnlocalizedName(Reference.prefix + "bucketToxicWaste").setContainerItem(Items.bucket).setTextureName(Reference.prefix + "bucketToxicWaste")
    FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("toxicwaste"), new ItemStack(AtomicContent.itemBucketToxic), new ItemStack(Items.bucket))
    FluidContainerRegistry.registerFluidContainer(FluidRegistry.WATER, new ItemStack(AtomicContent.itemWaterCell), new ItemStack(AtomicContent.itemCell))
    FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("deuterium"), 200), new ItemStack(AtomicContent.itemDeuteriumCell), new ItemStack(AtomicContent.itemCell))
    FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("tritium"), 200), new ItemStack(AtomicContent.itemTritiumCell), new ItemStack(AtomicContent.itemCell))
    if (OreDictionary.getOres("oreUranium").size > 1 && Settings.config.get(Configuration.CATEGORY_GENERAL, "Auto Disable Uranium If Exist", false).getBoolean(false))
    {

    }
    else
    {
      AtomicContent.uraniumOreGeneration = new OreGenReplaceStone("Uranium Ore", new ItemStack(AtomicContent.blockUraniumOre), 25, 9, 3)
      AtomicContent.uraniumOreGeneration.enable(Settings.config)
      //OreGenerator.addOre(AtomicContent.uraniumOreGeneration)
    }
    Settings.config.save
    MinecraftForge.EVENT_BUS.register(AtomicContent.itemAntimatter)
    MinecraftForge.EVENT_BUS.register(FulminationHandler.INSTANCE)
    if (Settings.allowOreDictionaryCompatibility)
    {
      OreDictionary.registerOre("ingotUranium", AtomicContent.itemUranium)
      OreDictionary.registerOre("dustUranium", AtomicContent.itemYellowCake)
    }
    OreDictionary.registerOre("breederUranium", new ItemStack(AtomicContent.itemUranium, 1, 1))
    OreDictionary.registerOre("blockRadioactive", AtomicContent.blockRadioactive)
    OreDictionary.registerOre("cellEmpty", AtomicContent.itemCell)
    OreDictionary.registerOre("cellUranium", AtomicContent.itemFissileFuel)
    OreDictionary.registerOre("cellTritium", AtomicContent.itemTritiumCell)
    OreDictionary.registerOre("cellDeuterium", AtomicContent.itemDeuteriumCell)
    OreDictionary.registerOre("cellWater", AtomicContent.itemWaterCell)
    OreDictionary.registerOre("strangeMatter", AtomicContent.itemDarkMatter)
    OreDictionary.registerOre("antimatterMilligram", new ItemStack(AtomicContent.itemAntimatter, 1, 0))
    OreDictionary.registerOre("antimatterGram", new ItemStack(AtomicContent.itemAntimatter, 1, 1))
    ForgeChunkManager.setForcedChunkLoadingCallback(this, new ForgeChunkManager.LoadingCallback
    {
      def ticketsLoaded(tickets: List[ForgeChunkManager.Ticket], world: World)
      {
        import scala.collection.JavaConversions._
        for (ticket <- tickets)
        {
          if (ticket.getType eq Type.ENTITY)
          {
            if (ticket.getEntity != null)
            {
              if (ticket.getEntity.isInstanceOf[EntityParticle])
              {
                (ticket.getEntity.asInstanceOf[EntityParticle]).updateTicket = ticket
              }
            }
          }
        }
      }
    })
    Settings.config.save
    ResonantTab.itemStack(new ItemStack(AtomicContent.blockReactorCell))
    Atomic.proxy.preInit
  }

  @EventHandler def init(evt: FMLInitializationEvent)
  {
    Atomic.proxy.init
  }

  @EventHandler def postInit(event: FMLPostInitializationEvent)
  {
    if (Loader.isModLoaded("IC2") && Settings.allowAlternateRecipes)
    {
      OreDictionary.registerOre("cellEmpty", IC2Items.getItem("cell"))
      val cellEmptyName: String = OreDictionary.getOreName(OreDictionary.getOreID("cellEmpty"))
      if (cellEmptyName eq "Unknown")
      {
      }
      GameRegistry.addRecipe(new ShapelessOreRecipe(AtomicContent.itemYellowCake, IC2Items.getItem("reactorUraniumSimple")))
      GameRegistry.addRecipe(new ShapelessOreRecipe(IC2Items.getItem("cell"), AtomicContent.itemCell))
      GameRegistry.addRecipe(new ShapelessOreRecipe(AtomicContent.itemCell, "cellEmpty"))
    }
    EntityRegistry.registerGlobalEntityID(classOf[EntityParticle], "ASParticle", EntityRegistry.findGlobalUniqueEntityId)
    EntityRegistry.registerModEntity(classOf[EntityParticle], "ASParticle", Atomic.ENTITY_ID_PREFIX, this, 80, 3, true)
    Atomic.proxy.init
    Settings.config.load
    for (oreName <- OreDictionary.getOreNames)
    {
      if (oreName.startsWith("ingot"))
      {
        import scala.collection.JavaConversions._
        for (itemStack <- OreDictionary.getOres(oreName))
        {
          if (itemStack != null)
          {
            QuantumAssemblerRecipes.addRecipe(itemStack)
          }
        }
      }
    }
    Settings.config.save
  }

  @SubscribeEvent def thermalEventHandler(evt: EventThermal.EventThermalUpdate)
  {
    val pos: VectorWorld = evt.position
    val block: Block = pos.getBlock
    if (block == AtomicContent.blockElectromagnet)
    {
      evt.heatLoss = evt.deltaTemperature * 0.6f
    }
  }

  @SubscribeEvent def plasmaEvent(evt: PlasmaEvent.SpawnPlasmaEvent)
  {
    val block: Block = evt.world.getBlock(evt.x, evt.y, evt.z)
    if (block != null && block.getBlockHardness(evt.world, evt.x, evt.y, evt.z) >= 0)
    {
      val tile: TileEntity = evt.world.getTileEntity(evt.x, evt.y, evt.z)
      if (tile.isInstanceOf[TilePlasma])
      {
        (tile.asInstanceOf[TilePlasma]).setTemperature(evt.temperature)
        return
      }
      else if (tile.isInstanceOf[IElectromagnet])
      {
        return
      }
      else
      {
        evt.world.setBlockToAir(evt.x, evt.y, evt.z)
        evt.world.setBlock(evt.x, evt.y, evt.z, AtomicContent.blockPlasma)
      }
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT) def preTextureHook(event: TextureStitchEvent.Pre)
  {
    if (event.map.getTextureType == 0)
    {
      RenderUtility.registerIcon(Reference.prefix + "uraniumHexafluoride", event.map)
      RenderUtility.registerIcon(Reference.prefix + "steam", event.map)
      RenderUtility.registerIcon(Reference.prefix + "deuterium", event.map)
      RenderUtility.registerIcon(Reference.prefix + "tritium", event.map)
      RenderUtility.registerIcon(Reference.prefix + "atomic_edge", event.map)
      RenderUtility.registerIcon(Reference.prefix + "funnel_edge", event.map)
      RenderUtility.registerIcon(Reference.prefix + "glass", event.map)
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT) def postTextureHook(event: TextureStitchEvent.Post)
  {
    AtomicContent.FLUID_URANIUM_HEXAFLOURIDE.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "uraniumHexafluoride"))
    AtomicContent.FLUID_STEAM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "steam"))
    AtomicContent.FLUID_DEUTERIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "deuterium"))
    AtomicContent.FLUID_TRITIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "tritium"))
    AtomicContent.FLUID_TOXIC_WASTE.setIcons(AtomicContent.blockToxicWaste.getIcon(0, 0))
    AtomicContent.FLUID_PLASMA.setIcons(AtomicContent.blockPlasma.getIcon(0, 0))
  }

  @SubscribeEvent def fillBucketEvent(evt: FillBucketEvent)
  {
    if (!evt.world.isRemote && evt.target != null && evt.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
    {
      val pos: VectorWorld = new VectorWorld(evt.world, evt.target)
      if (pos.getBlock eq AtomicContent.blockToxicWaste)
      {
        pos.setBlockToAir
        evt.result = new ItemStack(AtomicContent.itemBucketToxic)
        evt.setResult(Event.Result.ALLOW)
      }
    }
  }
}