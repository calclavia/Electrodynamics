package com.calclavia.edx.quantum

import java.util.List

import com.calclavia.edx.basic.fluid.tank.TileTank
import com.calclavia.edx.quantum.blocks.{TileElectromagnet, BlockRadioactive, BlockToxicWaste}
import com.calclavia.edx.quantum.items._
import com.calclavia.edx.quantum.machine.TileFunnel
import com.calclavia.edx.quantum.machine.accelerator.{EntityParticle, TileAccelerator}
import com.calclavia.edx.quantum.machine.boiler.TileNuclearBoiler
import com.calclavia.edx.quantum.machine.centrifuge.TileCentrifuge
import com.calclavia.edx.quantum.machine.extractor.TileChemicalExtractor
import com.calclavia.edx.quantum.machine.fulmination.{TileFulmination, FulminationHandler}
import com.calclavia.edx.quantum.machine.plasma.{TilePlasma, TilePlasmaHeater}
import com.calclavia.edx.quantum.machine.quantum.TileQuantumAssembler
import com.calclavia.edx.quantum.reactor.{TileControlRod, TileReactorCell}
import com.calclavia.edx.quantum.schematic.{SchematicFissionReactor, SchematicFusionReactor, SchematicAccelerator, SchematicBreedingReactor}
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.{Event, SubscribeEvent}
import cpw.mods.fml.common.registry.{EntityRegistry, GameRegistry}
import edx.core.wrapper.FluidStackWrapper._
import edx.core.{EDXCreativeTab, Electrodynamics, Reference, Settings}
import edx.quantum.blocks._
import edx.quantum.items._
import edx.quantum.machine.accelerator.EntityParticle
import edx.quantum.machine.fulmination.TileFulmination
import edx.quantum.machine.plasma.TilePlasma
import edx.quantum.reactor.TileControlRod
import edx.quantum.schematic.SchematicFissionReactor
import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemBucket, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.{ForgeChunkManager, MinecraftForge}
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.fluids._
import net.minecraftforge.oredict.{OreDictionary, ShapelessOreRecipe}
import resonantengine.api.edx.recipe.QuantumAssemblerRecipes
import resonantengine.api.event.PlasmaEvent
import resonantengine.api.tile.IElectromagnet
import resonantengine.lib.factory.resources.block.OreGenerator
import resonantengine.lib.grid.thermal.ThermalEvent
import resonantengine.lib.modcontent.ExplicitContentName
import resonantengine.lib.schematic.SchematicRegistry
import resonantengine.lib.transform.vector.VectorWorld
import resonantengine.lib.utility.recipe.UniversalRecipe
import resonantengine.prefab.modcontent.ContentHolder

import scala.collection.JavaConversions._

object QuantumContent extends ContentHolder
{
  //Constructor
  manager.setTab(EDXCreativeTab).setPrefix(Reference.prefix)

  val ENTITY_ID_PREFIX: Int = 49
  val SECOND_IN_TICKS: Int = 20

  // Blocks
  var blockRadioactive: Block = new BlockRadioactive(Material.rock).setBlockTextureName(Reference.prefix + "radioactive").setCreativeTab(CreativeTabs.tabBlock)
  var blockCentrifuge: Block = new TileCentrifuge
  var blockNuclearBoiler: Block = new TileNuclearBoiler
  var blockControlRod: Block = new TileControlRod
  var blockFusionCore: Block = new TilePlasmaHeater
  var blockPlasma: Block = new TilePlasma
  var blockElectromagnet: Block = new TileElectromagnet
  var blockChemicalExtractor: Block = new TileChemicalExtractor
  var blockSteamFunnel: Block = new TileFunnel
  var blockAccelerator: Block = new TileAccelerator
  var blockFulmination: Block = new TileFulmination
  var blockQuantumAssembler: Block = new TileQuantumAssembler
  var blockReactorCell: Block = new TileReactorCell
  var blockToxicWaste: Block = new BlockToxicWaste().setCreativeTab(null)

  //Cells
  @ExplicitContentName(value = "cellEmpty")
  var itemCell: Item = new ItemCell("cellEmpty")
  var itemFissileFuel: Item = new ItemFuelRod
  var itemBreedingRod: Item = new ItemBreederFuel
  @ExplicitContentName
  var itemDarkMatter: Item = new ItemCell("darkMatter")
  var itemAntimatter: Item = new ItemAntimatter
  @ExplicitContentName(value = "cellDeuterium")
  var itemDeuteriumCell: Item = new ItemCell("cellDeuterium")
  @ExplicitContentName(value = "cellTritium")
  var itemTritiumCell: Item = new ItemCell("cellTritium")
  @ExplicitContentName(value = "cellWater")
  var itemWaterCell: Item = new ItemCell("cellWater")
  @ExplicitContentName
  var itemYellowCake: Item = new ItemRadioactive().setTextureName(Reference.prefix + "yellowcake").setCreativeTab(EDXCreativeTab)
  var itemUranium: Item = new ItemUranium().setCreativeTab(EDXCreativeTab)

  //Buckets
  var itemBucketToxic: Item = null

  //Hazmat suit
  @ExplicitContentName
  var itemHazmatMask: Item = new ItemHazmat("hazmatMask", 0)
  @ExplicitContentName
  var itemHazmatBody: Item = new ItemHazmat("hazmatBody", 1)
  @ExplicitContentName
  var itemHazmatLeggings: Item = new ItemHazmat("hazmatLeggings", 2)
  @ExplicitContentName
  var itemHazmatBoots: Item = new ItemHazmat("hazmatBoots", 3)

  var uraniumOreGeneration: OreGenerator = null

  override def preInit()
  {
    MinecraftForge.EVENT_BUS.register(this)
    MinecraftForge.EVENT_BUS.register(FulminationHandler.INSTANCE)

    //Register Fluids
    FluidRegistry.registerFluid(QuantumContent.fluidUraniumHexaflouride)
    FluidRegistry.registerFluid(QuantumContent.fluidSteam)
    FluidRegistry.registerFluid(QuantumContent.getFluidTritium)
    FluidRegistry.registerFluid(QuantumContent.FLUID_DEUTERIUM)
    FluidRegistry.registerFluid(QuantumContent.getFluidToxicWaste)
    FluidRegistry.registerFluid(QuantumContent.FLUID_PLASMA)

    Settings.config.load()

    super.preInit()

    //Buckets
    itemBucketToxic = manager.newItem("bucketToxicWaste", new ItemBucket(QuantumContent.blockPlasma)).setCreativeTab(EDXCreativeTab).setContainerItem(Items.bucket).setTextureName(Reference.prefix + "bucketToxicWaste")

    //Schematics
    SchematicRegistry.register("resonantInduction.atomic.accelerator", new SchematicAccelerator)
    SchematicRegistry.register("resonantInduction.atomic.breedingReactor", new SchematicBreedingReactor)
    SchematicRegistry.register("resonantInduction.atomic.fissionReactor", new SchematicFissionReactor)
    SchematicRegistry.register("resonantInduction.atomic.fusionReactor", new SchematicFusionReactor)

    //Fluid Containers
    QuantumContent.FLUID_PLASMA.setBlock(QuantumContent.blockPlasma)
    FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("toxicwaste"), new ItemStack(QuantumContent.itemBucketToxic), new ItemStack(Items.bucket))
    FluidContainerRegistry.registerFluidContainer(FluidRegistry.WATER, new ItemStack(QuantumContent.itemWaterCell), new ItemStack(QuantumContent.itemCell))
    FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("deuterium"), 200), new ItemStack(QuantumContent.itemDeuteriumCell), new ItemStack(QuantumContent.itemCell))
    FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("tritium"), 200), new ItemStack(QuantumContent.itemTritiumCell), new ItemStack(QuantumContent.itemCell))

    //Uranium ore Gen settings TODO re-enable
    //AtomicContent.uraniumOreGeneration = new OreGenReplaceStone("Uranium Ore", new ItemStack(AtomicContent.blockUraniumOre), 25, 9, 3)
    //AtomicContent.uraniumOreGeneration.enable(Settings.config)
    //OreGenerator.addOre(AtomicContent.uraniumOreGeneration)

    //Ore dictionary support
    OreDictionary.registerOre("ingotUranium", itemUranium)
    OreDictionary.registerOre("dustUranium", itemYellowCake)
    OreDictionary.registerOre("breederUranium", new ItemStack(itemUranium, 1, 1))
    OreDictionary.registerOre("blockRadioactive", blockRadioactive)
    OreDictionary.registerOre("cellEmpty", itemCell)
    OreDictionary.registerOre("cellUranium", itemFissileFuel)
    OreDictionary.registerOre("cellTritium", itemTritiumCell)
    OreDictionary.registerOre("cellDeuterium", itemDeuteriumCell)
    OreDictionary.registerOre("cellWater", itemWaterCell)
    OreDictionary.registerOre("strangeMatter", itemDarkMatter)
    OreDictionary.registerOre("antimatterMilligram", new ItemStack(itemAntimatter, 1, 0))
    OreDictionary.registerOre("antimatterGram", new ItemStack(itemAntimatter, 1, 1))


    //Chunk loader for Accelerator
    ForgeChunkManager.setForcedChunkLoadingCallback(this, new ForgeChunkManager.LoadingCallback
    {
      def ticketsLoaded(tickets: List[ForgeChunkManager.Ticket], world: World)
      {
        for (ticket <- tickets)
        {
          if (ticket.getType == Type.ENTITY)
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

  }

  def FLUID_PLASMA: Fluid = new Fluid("plasma").setGaseous(true)

  def fluidUraniumHexaflouride: Fluid =
  {
    var fluid: Fluid = FluidRegistry.getFluid("uraniumhexafluoride")
    if (fluid == null)
    {
      fluid = new Fluid("uraniumhexafluoride").setGaseous(true)
      FluidRegistry.registerFluid(fluid)
    }
    return fluid
  }

  def FLUID_DEUTERIUM: Fluid =
  {
    var fluid: Fluid = FluidRegistry.getFluid("deuterium")
    if (fluid == null)
    {
      fluid = new Fluid("deuterium").setGaseous(true)
      FluidRegistry.registerFluid(fluid)
    }
    return fluid
  }

  /** Gets the Fluid instance of Tritium */
  def getFluidTritium: Fluid =
  {
    var fluid: Fluid = FluidRegistry.getFluid("tritium")
    if (fluid == null)
    {
      fluid = new Fluid("tritium").setGaseous(true)
      FluidRegistry.registerFluid(fluid)
    }
    return fluid
  }

  /** Gets the Fluid instance of Toxic Waste */
  def getFluidToxicWaste: Fluid =
  {
    var fluid: Fluid = FluidRegistry.getFluid("toxicwaste")
    if (fluid == null)
    {
      fluid = new Fluid("toxicwaste").setGaseous(true)
      FluidRegistry.registerFluid(fluid)
    }
    return fluid
  }

  def fluidSteam: Fluid =
  {
    var fluid = FluidRegistry.getFluid("steam")

    if (fluid == null)
    {
      fluid = new Fluid("steam").setGaseous(true)
      FluidRegistry.registerFluid(fluid)
    }

    return fluid
  }

  override def postInit()
  {
    super.postInit()

    if (Loader.isModLoaded("IC2") && Settings.allowAlternateRecipes)
    {
      OreDictionary.registerOre("cellEmpty", IC2Items.getItem("cell"))
      val cellEmptyName: String = OreDictionary.getOreName(OreDictionary.getOreID("cellEmpty"))
      if (cellEmptyName == "Unknown")
      {
      }
      GameRegistry.addRecipe(new ShapelessOreRecipe(QuantumContent.itemYellowCake, IC2Items.getItem("reactorUraniumSimple")))
      GameRegistry.addRecipe(new ShapelessOreRecipe(IC2Items.getItem("cell"), QuantumContent.itemCell))
      GameRegistry.addRecipe(new ShapelessOreRecipe(QuantumContent.itemCell, "cellEmpty"))
    }

    EntityRegistry.registerGlobalEntityID(classOf[EntityParticle], "ASParticle", EntityRegistry.findGlobalUniqueEntityId)
    EntityRegistry.registerModEntity(classOf[EntityParticle], "ASParticle", ENTITY_ID_PREFIX, Electrodynamics, 80, 3, true)
    Settings.config.load()

    for (oreName <- OreDictionary.getOreNames)
    {
      if (oreName.startsWith("ingot"))
      {
        for (itemStack <- OreDictionary.getOres(oreName))
        {
          if (itemStack != null)
          {
            QuantumAssemblerRecipes.addRecipe(itemStack)
          }
        }
      }
    }

    Settings.config.save()

    recipes += shapeless(new ItemStack(itemAntimatter, 1, 1), itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, QuantumContent.itemAntimatter, itemAntimatter, itemAntimatter)
    recipes += shapeless(new ItemStack(itemAntimatter, 8, 0), new ItemStack(QuantumContent.itemAntimatter, 1, 1))


    recipes += shaped(new ItemStack(blockSteamFunnel, 2), " B ", "B B", "B B", 'B', UniversalRecipe.SECONDARY_METAL.get)
    recipes += shaped(new ItemStack(blockSteamFunnel, 2), " B ", "B B", "B B", 'B', "ingotIron")

    recipes += shaped(blockQuantumAssembler, "CCC", "SXS", "SSS", 'X', blockCentrifuge, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(blockFulmination, "OSO", "SCS", "OSO", 'O', Blocks.obsidian, 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(blockAccelerator, "SCS", "CMC", "SCS", 'M', UniversalRecipe.MOTOR.get, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(blockCentrifuge, "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)
    recipes += shaped(blockNuclearBoiler, "S S", "FBF", "SMS", 'F', Blocks.furnace, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', Items.bucket, 'M', UniversalRecipe.MOTOR.get)
    recipes += shaped(blockChemicalExtractor, "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)

    recipes += shaped(blockReactorCell, "SCS", "MEM", "SCS", 'E', "cellEmpty", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'M', UniversalRecipe.MOTOR.get)
    recipes += shaped(blockFusionCore, "CPC", "PFP", "CPC", 'P', UniversalRecipe.PRIMARY_PLATE.get, 'F', QuantumContent.blockReactorCell, 'C', UniversalRecipe.CIRCUIT_T3.get)
    recipes += shaped(new ItemStack(itemCell, 16), " T ", "TGT", " T ", 'T', "ingotTin", 'G', Blocks.glass)
    recipes += shaped(blockControlRod, "I", "I", "I", 'I', Items.iron_ingot)
    recipes += shaped(itemFissileFuel, "CUC", "CUC", "CUC", 'U', "ingotUranium", 'C', "cellEmpty")
    recipes += shaped(itemBreedingRod, "CUC", "CUC", "CUC", 'U', "breederUranium", 'C', "cellEmpty")

    //Hazmat recipes
    recipes += shaped(itemHazmatMask, "SSS", "BAB", "SCS", 'A', Items.leather_helmet, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
    recipes += shaped(itemHazmatBody, "SSS", "BAB", "SCS", 'A', Items.leather_chestplate, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
    recipes += shaped(itemHazmatLeggings, "SSS", "BAB", "SCS", 'A', Items.leather_leggings, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
    recipes += shaped(itemHazmatBoots, "SSS", "BAB", "SCS", 'A', Items.leather_boots, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)

    //Electro magnets
    recipes += shaped(new ItemStack(blockElectromagnet, 2, 0), "BBB", "BMB", "BBB", 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)
    recipes += shapeless(new ItemStack(QuantumContent.blockElectromagnet, 1, 1), QuantumContent.blockElectromagnet, Blocks.glass)

    recipes += shapeless(new ItemStack(QuantumContent.itemWaterCell), "cellEmpty", Items.water_bucket)

  }

  @SubscribeEvent
  def fillBucketEvent(evt: FillBucketEvent)
  {
    if (!evt.world.isRemote && evt.target != null && evt.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
    {
      val pos: VectorWorld = new VectorWorld(evt.world, evt.target)
      if (pos.getBlock eq QuantumContent.blockToxicWaste)
      {
        pos.setBlockToAir
        evt.result = new ItemStack(QuantumContent.itemBucketToxic)
        evt.setResult(Event.Result.ALLOW)
      }
    }
  }

  @SubscribeEvent
  def thermalEventHandler(evt: ThermalEvent.EventThermalUpdate)
  {
    val pos = evt.position

    val tile = pos.getTileEntity

    /**
     * Heat up fluid in containers
     */
    if (tile.isInstanceOf[TileTank])
    {
      val fluidStack = tile.asInstanceOf[TileTank].fluidNode.getTank.getFluid

      if (fluidStack != null)
        fluidStack.setTemperature(evt.temperature)
    }
  }

  @SubscribeEvent
  def plasmaEvent(evt: PlasmaEvent.SpawnPlasmaEvent)
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
        evt.world.setBlock(evt.x, evt.y, evt.z, QuantumContent.blockPlasma)
      }
    }
  }

  /** Is this ItemStack a cell?
    *
    * @param itemStack
    * @return
    */
  def isItemStackEmptyCell(itemStack: ItemStack): Boolean =
  {
    return isItemStackOreDictionaryCompatible(itemStack, "cellEmpty")
  }

  /** Compare to Ore Dict
    *
    * @param itemStack
    * @return
    */
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

  def fluidStackWater: FluidStack = new FluidStack(FluidRegistry.WATER, 0)

  def fluidStackUraniumHexaflouride: FluidStack = new FluidStack(QuantumContent.fluidUraniumHexaflouride, 0)

  def fluidStackSteam: FluidStack = new FluidStack(fluidSteam, 0)

  def FLUIDSTACK_DEUTERIUM: FluidStack = new FluidStack(FLUID_DEUTERIUM, 0)

  def getFluidStackTritium: FluidStack = new FluidStack(getFluidTritium, 0)

  /** Gets a FluidStack of Toxic Waste */
  def getStackToxicWaste: FluidStack = new FluidStack(getFluidToxicWaste, 0)
}
