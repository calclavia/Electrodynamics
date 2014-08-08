package resonantinduction.atomic

import cpw.mods.fml.common.eventhandler.Event
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import ic2.api.item.IC2Items
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.util.EnumHelper
import resonant.api.event.PlasmaEvent
import resonant.content.loader.ModManager
import resonant.engine.content.debug.TileCreativeBuilder
import resonant.engine.grid.thermal.EventThermal
import resonant.lib.network.discriminator.PacketAnnotation
import resonant.lib.network.discriminator.PacketAnnotationManager
import resonant.lib.ore.OreGenReplaceStone
import resonant.lib.ore.OreGenerator
import resonantinduction.atomic.blocks._
import resonantinduction.atomic.items._
import resonantinduction.atomic.machine.TileFunnel
import resonantinduction.atomic.machine.boiler.TileNuclearBoiler
import resonantinduction.atomic.machine.extractor.TileChemicalExtractor
import resonantinduction.atomic.machine.fulmination.FulminationHandler
import resonantinduction.atomic.machine.fulmination.TileFulmination
import resonantinduction.atomic.machine.plasma.BlockPlasmaHeater
import resonantinduction.atomic.machine.plasma.TilePlasma
import resonantinduction.atomic.machine.quantum.TileQuantumAssembler
import resonantinduction.atomic.machine.reactor.TileReactorCell
import resonantinduction.atomic.schematic.SchematicBreedingReactor
import resonantinduction.mechanical.turbine.TileElectricTurbine
import universalelectricity.core.transform.vector.VectorWorld
import ic2.api.recipe.IRecipeInput
import ic2.api.recipe.RecipeOutput
import ic2.api.recipe.Recipes
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Map.Entry
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBucket
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback
import net.minecraftforge.common.ForgeChunkManager.Ticket
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidContainerRegistry
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import net.minecraftforge.oredict.ShapelessOreRecipe
import resonant.api.IElectromagnet
import resonant.api.event.PlasmaEvent.SpawnPlasmaEvent
import resonant.api.recipe.QuantumAssemblerRecipes
import resonant.lib.recipe.UniversalRecipe
import resonant.lib.render.RenderUtility
import resonantinduction.atomic.machine.accelerator.EntityParticle
import resonantinduction.atomic.machine.accelerator.TileAccelerator
import resonantinduction.atomic.machine.centrifuge.TileCentrifuge
import resonantinduction.atomic.machine.plasma.TilePlasmaHeater
import resonantinduction.atomic.machine.reactor.TileControlRod
import resonantinduction.atomic.machine.thermometer.TileThermometer
import resonantinduction.atomic.schematic.SchematicAccelerator
import resonantinduction.atomic.schematic.SchematicFissionReactor
import resonantinduction.atomic.schematic.SchematicFusionReactor
import resonantinduction.core.Reference
import resonantinduction.core.ResonantInduction
import resonantinduction.core.ResonantTab
import resonantinduction.core.Settings
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.Mod.Instance
import cpw.mods.fml.common.ModMetadata
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.EntityRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

object Atomic {
  /** Is this ItemStack a cell?
    *
    * @param itemStack
    * @return*/
  def isItemStackEmptyCell(itemStack: ItemStack): Boolean = {
    return isItemStackOreDictionaryCompatible(itemStack, "cellEmpty")
  }

  def isItemStackWaterCell(itemStack: ItemStack): Boolean = {
    return isItemStackOreDictionaryCompatible(itemStack, "cellWater")
  }

  def isItemStackUraniumOre(itemStack: ItemStack): Boolean = {
    return isItemStackOreDictionaryCompatible(itemStack, "dropUranium", "oreUranium")
  }

  def isItemStackDeuteriumCell(itemStack: ItemStack): Boolean = {
    return isItemStackOreDictionaryCompatible(itemStack, "molecule_1d", "molecule_1h2", "cellDeuterium")
  }

  def isItemStackTritiumCell(itemStack: ItemStack): Boolean = {
    return isItemStackOreDictionaryCompatible(itemStack, "molecule_h3", "cellTritium")
  }

  /** Compare to Ore Dict
    *
    * @param itemStack
    * @return*/
  def isItemStackOreDictionaryCompatible(itemStack: ItemStack, names: String*): Boolean = {
    if (itemStack != null && names != null && names.length > 0) {
      val name: String = OreDictionary.getOreName(OreDictionary.getOreID(itemStack))
      for (compareName <- names) {
        if (name == compareName) {
          return true
        }
      }
    }
    return false
  }

  def getFluidAmount(fluid: FluidStack): Int = {
    if (fluid != null) {
      return fluid.amount
    }
    return 0
  }

  final val ID: String = "ResonantInduction|Atomic"
  final val TEXTURE_DIRECTORY: String = "textures/"
  final val GUI_TEXTURE_DIRECTORY: String = TEXTURE_DIRECTORY + "gui/"
  final val ENTITY_ID_PREFIX: Int = 49
  final val SECOND_IN_TICKS: Int = 20
  final val NAME: String = Reference.name + " Atomic"
  final val contentRegistry: ModManager = new ModManager().setPrefix(Reference.prefix).setTab(ResonantTab.tab)
  private final val SUPPORTED_LANGUAGES: Array[String] = Array[String]("en_US", "pl_PL", "de_DE", "ru_RU")
  @Instance(ID) var INSTANCE: Atomic = null
  @SidedProxy(clientSide = "ClientProxy", serverSide = "CommonProxy") var proxy: CommonProxy = null
  @Mod.Metadata(ID) var metadata: ModMetadata = null
  /** Block and Items */
  var blockRadioactive: Block = null
  var blockCentrifuge: Block = null
  var blockNuclearBoiler: Block = null
  var blockControlRod: Block = null
  var blockThermometer: Block = null
  var blockFusionCore: Block = null
  var blockPlasma: Block = null
  var blockElectromagnet: Block = null
  var blockChemicalExtractor: Block = null
  var blockSiren: Block = null
  var blockSteamFunnel: Block = null
  var blockAccelerator: Block = null
  var blockFulmination: Block = null
  var blockQuantumAssembler: Block = null
  var blockReactorCell: Block = null
  var blockUraniumOre: Block = null
  var itemCell: Item = null
  var itemFissileFuel: Item = null
  var itemBreedingRod: Item = null
  var itemDarkMatter: Item = null
  var itemAntimatter: Item = null
  var itemDeuteriumCell: Item = null
  var itemTritiumCell: Item = null
  var itemWaterCell: Item = null
  var itemBucketToxic: Item = null
  var itemYellowCake: Item = null
  var itemUranium: Item = null
  var itemHazmatTop: Item = null
  var itemHazmatBody: Item = null
  var itemHazmatLeggings: Item = null
  var itemHazmatBoots: Item = null
  /** Fluids */
  var blockToxicWaste: Block = null
  /** Water, Uranium Hexafluoride, Steam, Deuterium, Toxic waste */
  var FLUIDSTACK_WATER: FluidStack = null
  var FLUIDSTACK_URANIUM_HEXAFLOURIDE: FluidStack = null
  var FLUIDSTACK_STEAM: FluidStack = null
  var FLUIDSTACK_DEUTERIUM: FluidStack = null
  var FLUIDSTACK_TRITIUM: FluidStack = null
  var FLUIDSTACK_TOXIC_WASTE: FluidStack = null
  var FLUID_URANIUM_HEXAFLOURIDE: Fluid = null
  var FLUID_PLASMA: Fluid = null
  var FLUID_STEAM: Fluid = null
  var FLUID_DEUTERIUM: Fluid = null
  var FLUID_TRITIUM: Fluid = null
  var FLUID_TOXIC_WASTE: Fluid = null
  var uraniumOreGeneration: OreGenerator = null

}

@Mod(modid = Atomic.ID, name = Atomic.NAME, version = Reference.version, dependencies = "required-after:ResonantEngine;after:IC2;after:ResonantInduction|Electrical;required-after:" + Reference.coreID)
class Atomic {
  @EventHandler def preInit(event: FMLPreInitializationEvent) {
    Atomic.INSTANCE = this
    MinecraftForge.EVENT_BUS.register(this)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, Atomic.proxy)
    TileCreativeBuilder.register(new SchematicAccelerator)
    TileCreativeBuilder.register(new SchematicBreedingReactor)
    TileCreativeBuilder.register(new SchematicFissionReactor)
    TileCreativeBuilder.register(new SchematicFusionReactor)
    Settings.config.load
    Atomic.FLUID_URANIUM_HEXAFLOURIDE = new Fluid("uraniumhexafluoride").setGaseous(true)
    Atomic.FLUID_STEAM = new Fluid("steam").setGaseous(true)
    Atomic.FLUID_DEUTERIUM = new Fluid("deuterium").setGaseous(true)
    Atomic.FLUID_TRITIUM = new Fluid("tritium").setGaseous(true)
    Atomic.FLUID_TOXIC_WASTE = new Fluid("toxicwaste")
    Atomic.FLUID_PLASMA = new Fluid("plasma").setGaseous(true)
    FluidRegistry.registerFluid(Atomic.FLUID_URANIUM_HEXAFLOURIDE)
    FluidRegistry.registerFluid(Atomic.FLUID_STEAM)
    FluidRegistry.registerFluid(Atomic.FLUID_TRITIUM)
    FluidRegistry.registerFluid(Atomic.FLUID_DEUTERIUM)
    FluidRegistry.registerFluid(Atomic.FLUID_TOXIC_WASTE)
    FluidRegistry.registerFluid(Atomic.FLUID_PLASMA)
    Atomic.FLUIDSTACK_WATER = new FluidStack(FluidRegistry.WATER, 0)
    Atomic.FLUIDSTACK_URANIUM_HEXAFLOURIDE = new FluidStack(Atomic.FLUID_URANIUM_HEXAFLOURIDE, 0)
    Atomic.FLUIDSTACK_STEAM = new FluidStack(FluidRegistry.getFluidID("steam"), 0)
    Atomic.FLUIDSTACK_DEUTERIUM = new FluidStack(FluidRegistry.getFluidID("deuterium"), 0)
    Atomic.FLUIDSTACK_TRITIUM = new FluidStack(FluidRegistry.getFluidID("tritium"), 0)
    Atomic.FLUIDSTACK_TOXIC_WASTE = new FluidStack(FluidRegistry.getFluidID("toxicwaste"), 0)
    Atomic.blockRadioactive = new BlockRadioactive(Material.rock).setBlockName(Reference.prefix + "radioactive").setBlockTextureName(Reference.prefix + "radioactive").setCreativeTab(CreativeTabs.tabBlock)
    Atomic.blockUraniumOre = new BlockUraniumOre
    Atomic.blockToxicWaste = new BlockToxicWaste().setCreativeTab(null)
    Atomic.blockCentrifuge = Atomic.contentRegistry.newBlock(classOf[TileCentrifuge])
    Atomic.blockReactorCell = Atomic.contentRegistry.newBlock(classOf[TileReactorCell])
    Atomic.blockNuclearBoiler = Atomic.contentRegistry.newBlock(classOf[TileNuclearBoiler])
    Atomic.blockChemicalExtractor =Atomic. contentRegistry.newBlock(classOf[TileChemicalExtractor])
    Atomic.blockFusionCore = Atomic.contentRegistry.newBlock(classOf[TilePlasmaHeater])
    Atomic.blockControlRod = Atomic.contentRegistry.newBlock(classOf[TileControlRod])
    Atomic.blockThermometer = Atomic.contentRegistry.newBlock(classOf[TileThermometer])
    Atomic.blockPlasma = Atomic.contentRegistry.newBlock(classOf[TilePlasma])
    Atomic.blockElectromagnet = Atomic.contentRegistry.newBlock(classOf[TileElectromagnet])
    Atomic.blockSiren = Atomic.contentRegistry.newBlock(classOf[TileSiren])
    Atomic.blockSteamFunnel = Atomic.contentRegistry.newBlock(classOf[TileFunnel])
    Atomic.blockAccelerator = Atomic.contentRegistry.newBlock(classOf[TileAccelerator])
    Atomic.blockFulmination = Atomic.contentRegistry.newBlock(classOf[TileFulmination])
    Atomic.blockQuantumAssembler = Atomic.contentRegistry.newBlock(classOf[TileQuantumAssembler])
    Atomic.itemHazmatTop = new ItemHazmat("HazmatMask", 0)
    Atomic.itemHazmatBody = new ItemHazmat("HazmatBody", 1)
    Atomic.itemHazmatLeggings = new ItemHazmat("HazmatLeggings", 2)
    Atomic.itemHazmatBoots = new ItemHazmat("HazmatBoots", 3)
    Atomic.itemCell = new Item().setUnlocalizedName("cellEmpty")
    Atomic.itemFissileFuel = new ItemFissileFuel().setUnlocalizedName("rodFissileFuel")
    Atomic.itemDeuteriumCell = new ItemCell().setUnlocalizedName("cellDeuterium")
    Atomic.itemTritiumCell = new ItemCell().setUnlocalizedName("cellTritium")
    Atomic.itemWaterCell = new ItemCell().setUnlocalizedName("cellWater")
    Atomic.itemDarkMatter = new ItemDarkMatter().setUnlocalizedName("darkMatter")
    Atomic.itemAntimatter = new ItemAntimatter().setUnlocalizedName("antimatter")
    Atomic.itemBreedingRod = new ItemBreederFuel().setUnlocalizedName("rodBreederFuel")
    Atomic.itemYellowCake = new ItemRadioactive().setUnlocalizedName("yellowcake")
    Atomic.itemUranium = Atomic.contentRegistry.newItem(classOf[ItemUranium])
    Atomic.FLUID_PLASMA.setBlock(Atomic.blockPlasma)
    Atomic.itemBucketToxic = new ItemBucket(Atomic.blockPlasma).setCreativeTab(ResonantTab.tab).setUnlocalizedName(Reference.prefix + "bucketToxicWaste").setContainerItem(Items.bucket).setTextureName(Reference.prefix + "bucketToxicWaste")
    FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("toxicwaste"), new ItemStack(Atomic.itemBucketToxic), new ItemStack(Items.bucket))
    FluidContainerRegistry.registerFluidContainer(FluidRegistry.WATER, new ItemStack(Atomic.itemWaterCell), new ItemStack(Atomic.itemCell))
    FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("deuterium"), 200), new ItemStack(Atomic.itemDeuteriumCell), new ItemStack(Atomic.itemCell))
    FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("tritium"), 200), new ItemStack(Atomic.itemTritiumCell), new ItemStack(Atomic.itemCell))
    if (OreDictionary.getOres("oreUranium").size > 1 && Settings.config.get(Configuration.CATEGORY_GENERAL, "Auto Disable Uranium If Exist", false).getBoolean(false)) {
    }
    else {
      Atomic.uraniumOreGeneration = new OreGenReplaceStone("Uranium Ore", new ItemStack(Atomic.blockUraniumOre), 25, 9, 3)
      Atomic.uraniumOreGeneration.enable(Settings.config)
      OreGenerator.addOre(Atomic.uraniumOreGeneration)
    }
    Settings.config.save
    MinecraftForge.EVENT_BUS.register(Atomic.itemAntimatter)
    MinecraftForge.EVENT_BUS.register(FulminationHandler.INSTANCE)
    if (Settings.allowOreDictionaryCompatibility) {
      OreDictionary.registerOre("ingotUranium", Atomic.itemUranium)
      OreDictionary.registerOre("dustUranium", Atomic.itemYellowCake)
    }
    OreDictionary.registerOre("breederUranium", new ItemStack(Atomic.itemUranium, 1, 1))
    OreDictionary.registerOre("blockRadioactive", Atomic.blockRadioactive)
    OreDictionary.registerOre("cellEmpty", Atomic.itemCell)
    OreDictionary.registerOre("cellUranium", Atomic.itemFissileFuel)
    OreDictionary.registerOre("cellTritium", Atomic.itemTritiumCell)
    OreDictionary.registerOre("cellDeuterium", Atomic.itemDeuteriumCell)
    OreDictionary.registerOre("cellWater", Atomic.itemWaterCell)
    OreDictionary.registerOre("strangeMatter", Atomic.itemDarkMatter)
    OreDictionary.registerOre("antimatterMilligram", new ItemStack(Atomic.itemAntimatter, 1, 0))
    OreDictionary.registerOre("antimatterGram", new ItemStack(Atomic.itemAntimatter, 1, 1))
    ForgeChunkManager.setForcedChunkLoadingCallback(this, new ForgeChunkManager.LoadingCallback {
      def ticketsLoaded(tickets: List[ForgeChunkManager.Ticket], world: World) {
        import scala.collection.JavaConversions._
        for (ticket <- tickets) {
          if (ticket.getType eq Type.ENTITY) {
            if (ticket.getEntity != null) {
              if (ticket.getEntity.isInstanceOf[EntityParticle]) {
                (ticket.getEntity.asInstanceOf[EntityParticle]).updateTicket = ticket
              }
            }
          }
        }
      }
    })
    Settings.config.save
    ResonantTab.itemStack(new ItemStack(Atomic.blockReactorCell))
  }

  @EventHandler def init(evt: FMLInitializationEvent) {
    Atomic.proxy.init
  }

  @EventHandler def postInit(event: FMLPostInitializationEvent) {
    if (Loader.isModLoaded("IC2") && Settings.allowAlternateRecipes) {
      OreDictionary.registerOre("cellEmpty", IC2Items.getItem("cell"))
      val cellEmptyName: String = OreDictionary.getOreName(OreDictionary.getOreID("cellEmpty"))
      if (cellEmptyName eq "Unknown") {
      }
      GameRegistry.addRecipe(new ShapelessOreRecipe(Atomic.itemYellowCake, IC2Items.getItem("reactorUraniumSimple")))
      GameRegistry.addRecipe(new ShapelessOreRecipe(IC2Items.getItem("cell"), Atomic.itemCell))
      GameRegistry.addRecipe(new ShapelessOreRecipe(Atomic.itemCell, "cellEmpty"))
    }
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Atomic.itemAntimatter, 1, 1), Array[AnyRef](Atomic.itemAntimatter, Atomic.itemAntimatter, Atomic.itemAntimatter, Atomic.itemAntimatter, Atomic.itemAntimatter, Atomic.itemAntimatter, Atomic.itemAntimatter, Atomic.itemAntimatter)))
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Atomic.itemAntimatter, 8, 0), Array[AnyRef](new ItemStack(Atomic.itemAntimatter, 1, 1))))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Atomic.blockSteamFunnel, 2), Array[AnyRef](" B ", "B B", "B B", 'B', UniversalRecipe.SECONDARY_METAL.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Atomic.blockSteamFunnel, 2), Array[AnyRef](" B ", "B B", "B B", 'B', "ingotIron")))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockQuantumAssembler, Array[AnyRef]("CCC", "SXS", "SSS", 'X', Atomic.blockCentrifuge, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockFulmination, Array[AnyRef]("OSO", "SCS", "OSO", 'O', Blocks.obsidian, 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockAccelerator, Array[AnyRef]("SCS", "CMC", "SCS", 'M', UniversalRecipe.MOTOR.get, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockCentrifuge, Array[AnyRef]("BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockNuclearBoiler, Array[AnyRef]("S S", "FBF", "SMS", 'F', Blocks.furnace, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', Items.bucket, 'M', UniversalRecipe.MOTOR.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockChemicalExtractor, Array[AnyRef]("BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Atomic.blockSiren, 2), Array[AnyRef]("NPN", 'N', Blocks.noteblock, 'P', UniversalRecipe.SECONDARY_PLATE.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockReactorCell, Array[AnyRef]("SCS", "MEM", "SCS", 'E', "cellEmpty", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'M', UniversalRecipe.MOTOR.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockFusionCore, Array[AnyRef]("CPC", "PFP", "CPC", 'P', UniversalRecipe.PRIMARY_PLATE.get, 'F', Atomic.blockReactorCell, 'C', UniversalRecipe.CIRCUIT_T3.get)))
   GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Atomic.itemCell, 16), Array[AnyRef](" T ", "TGT", " T ", 'T', "ingotTin", 'G', Blocks.glass)))
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Atomic.itemWaterCell), Array[AnyRef]("cellEmpty", Items.water_bucket)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockThermometer, Array[AnyRef]("SSS", "GCG", "GSG", 'S', UniversalRecipe.PRIMARY_METAL.get, 'G', Blocks.glass, 'C', UniversalRecipe.CIRCUIT_T1.get)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.blockControlRod, Array[AnyRef]("I", "I", "I", 'I', Items.iron_ingot)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.itemFissileFuel, Array[AnyRef]("CUC", "CUC", "CUC", 'U', "ingotUranium", 'C', "cellEmpty")))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.itemBreedingRod, Array[AnyRef]("CUC", "CUC", "CUC", 'U', "breederUranium", 'C', "cellEmpty")))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Atomic.blockElectromagnet, 2, 0), Array[AnyRef]("BBB", "BMB", "BBB", 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)))
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Atomic.blockElectromagnet, 1, 1), Array[AnyRef](Atomic.blockElectromagnet, Blocks.glass)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.itemHazmatTop, Array[AnyRef]("SSS", "BAB", "SCS", 'A', Items.leather_helmet, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.itemHazmatBody, Array[AnyRef]("SSS", "BAB", "SCS", 'A', Items.leather_chestplate, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.itemHazmatLeggings, Array[AnyRef]("SSS", "BAB", "SCS", 'A', Items.leather_leggings, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)))
    GameRegistry.addRecipe(new ShapedOreRecipe(Atomic.itemHazmatBoots, Array[AnyRef]("SSS", "BAB", "SCS", 'A', Items.leather_boots, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)))
    EntityRegistry.registerGlobalEntityID(classOf[EntityParticle], "ASParticle", EntityRegistry.findGlobalUniqueEntityId)
    EntityRegistry.registerModEntity(classOf[EntityParticle], "ASParticle", Atomic.ENTITY_ID_PREFIX, this, 80, 3, true)
    Atomic.proxy.init
    Settings.config.load
    for (oreName <- OreDictionary.getOreNames) {
      if (oreName.startsWith("ingot")) {
        import scala.collection.JavaConversions._
        for (itemStack <- OreDictionary.getOres(oreName)) {
          if (itemStack != null) {
            QuantumAssemblerRecipes.addRecipe(itemStack)
          }
        }
      }
    }
    Settings.config.save
  }

  @SubscribeEvent def thermalEventHandler(evt: EventThermal.EventThermalUpdate) {
    val pos: VectorWorld = evt.position
    val block: Block = pos.getBlock
    if (block == Atomic.blockElectromagnet) {
      evt.heatLoss = evt.deltaTemperature * 0.6f
    }
  }

  @EventHandler def plasmaEvent(evt: PlasmaEvent.SpawnPlasmaEvent) {
    val block: Block = evt.world.getBlock(evt.x, evt.y, evt.z)
    if (block != null && block.getBlockHardness(evt.world, evt.x, evt.y, evt.z) >= 0) {
      val tile: TileEntity = evt.world.getTileEntity(evt.x, evt.y, evt.z)
      if (tile.isInstanceOf[TilePlasma]) {
        (tile.asInstanceOf[TilePlasma]).setTemperature(evt.temperature)
        return
      }
      else if (tile.isInstanceOf[IElectromagnet]) {
        return
      }
      else {
        evt.world.setBlockToAir(evt.x, evt.y, evt.z)
        evt.world.setBlock(evt.x, evt.y, evt.z, Atomic.blockPlasma)
      }
    }
  }

  @EventHandler
  @SideOnly(Side.CLIENT) def preTextureHook(event: TextureStitchEvent.Pre) {
    if (event.map.getTextureType == 0) {
      RenderUtility.registerIcon(Reference.prefix + "uraniumHexafluoride", event.map)
      RenderUtility.registerIcon(Reference.prefix + "steam", event.map)
      RenderUtility.registerIcon(Reference.prefix + "deuterium", event.map)
      RenderUtility.registerIcon(Reference.prefix + "tritium", event.map)
      RenderUtility.registerIcon(Reference.prefix + "atomic_edge", event.map)
      RenderUtility.registerIcon(Reference.prefix + "funnel_edge", event.map)
      RenderUtility.registerIcon(Reference.prefix + "glass", event.map)
    }
  }

  @EventHandler
  @SideOnly(Side.CLIENT) def postTextureHook(event: TextureStitchEvent.Post) {
    Atomic.FLUID_URANIUM_HEXAFLOURIDE.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "uraniumHexafluoride"))
    Atomic.FLUID_STEAM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "steam"))
    Atomic.FLUID_DEUTERIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "deuterium"))
    Atomic.FLUID_TRITIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "tritium"))
    Atomic.FLUID_TOXIC_WASTE.setIcons(Atomic.blockToxicWaste.getIcon(0, 0))
    Atomic.FLUID_PLASMA.setIcons(Atomic.blockPlasma.getIcon(0, 0))
  }

  @EventHandler def fillBucketEvent(evt: FillBucketEvent) {
    if (!evt.world.isRemote && evt.target != null && evt.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
      val pos: VectorWorld = new VectorWorld(evt.world, evt.target)
      if (pos.getBlock eq Atomic.blockToxicWaste) {
        pos.setBlockToAir
        evt.result = new ItemStack(Atomic.itemBucketToxic)
        evt.setResult(Event.Result.ALLOW)
      }
    }
  }
}