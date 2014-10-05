package resonantinduction.atomic

import java.util.List

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.{Event, SubscribeEvent}
import cpw.mods.fml.common.registry.{EntityRegistry, GameRegistry}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemBucket, ItemStack}
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
import resonant.content.loader.ContentHolder
import resonant.engine.content.debug.TileCreativeBuilder
import resonant.engine.grid.thermal.EventThermal
import resonant.lib.ore.{OreGenReplaceStone, OreGenerator}
import resonant.lib.recipe.UniversalRecipe
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
import resonantinduction.core.{ResonantInduction, Reference, ResonantTab, Settings}
import universalelectricity.core.transform.vector.VectorWorld

import scala.collection.JavaConversions._

/**
 * Created by robert on 8/10/2014.
 */
object AtomicContent extends ContentHolder
{
    //Constructor
    manager.setTab(ResonantTab).setPrefix(Reference.prefix)

    val ENTITY_ID_PREFIX: Int = 49
    val SECOND_IN_TICKS: Int = 20

    // Blocks
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
    var blockToxicWaste: Block = null

    //Cells
    var itemCell: Item = null
    var itemFissileFuel: Item = null
    var itemBreedingRod: Item = null
    var itemDarkMatter: Item = null
    var itemAntimatter: Item = null
    var itemDeuteriumCell: Item = null
    var itemTritiumCell: Item = null
    var itemWaterCell: Item = null
    var itemYellowCake: Item = null
    var itemUranium: Item = null

    //Buckets
    var itemBucketToxic: Item = null

    //Hazmat suit
    var itemHazmatTop: Item = null
    var itemHazmatBody: Item = null
    var itemHazmatLeggings: Item = null
    var itemHazmatBoots: Item = null


    var uraniumOreGeneration: OreGenerator = null

    override def preInit()
    {
        super.preInit()

        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(FulminationHandler.INSTANCE)

        Settings.config.load

        // Blocks
        blockRadioactive = manager.newBlock("radioactive", new BlockRadioactive(Material.rock)).setBlockTextureName(Reference.prefix + "radioactive").setCreativeTab(CreativeTabs.tabBlock)
        blockCentrifuge = manager.newBlock(classOf[TileCentrifuge])
        blockNuclearBoiler = manager.newBlock(classOf[TileNuclearBoiler])
        blockControlRod = manager.newBlock(classOf[TileControlRod])
        blockThermometer = manager.newBlock(classOf[TileThermometer])
        blockFusionCore = manager.newBlock(classOf[TilePlasmaHeater])
        blockPlasma = manager.newBlock(classOf[TilePlasma])
        blockElectromagnet = manager.newBlock(classOf[TileElectromagnet])
        blockChemicalExtractor = manager.newBlock(classOf[TileChemicalExtractor])
        blockSiren = manager.newBlock(classOf[TileSiren])
        blockSteamFunnel = manager.newBlock(classOf[TileFunnel])
        blockAccelerator = manager.newBlock(classOf[TileAccelerator])
        blockFulmination = manager.newBlock(classOf[TileFulmination])
        blockQuantumAssembler = manager.newBlock(classOf[TileQuantumAssembler])
        blockReactorCell = manager.newBlock(classOf[TileReactorCell])
        blockUraniumOre = manager.newBlock(classOf[BlockUraniumOre])
        blockToxicWaste = manager.newBlock(classOf[BlockToxicWaste]).setCreativeTab(null)

        //Cells
        itemCell = manager.newItem("cellEmpty", new ItemCell("cellEmpty"))
        itemFissileFuel = manager.newItem(classOf[ItemFissileFuel])
        itemBreedingRod = manager.newItem(classOf[ItemBreederFuel])
        itemDarkMatter = manager.newItem("darkMatter", new ItemCell("darkMatter"))
        itemAntimatter = manager.newItem(classOf[ItemAntimatter])
        MinecraftForge.EVENT_BUS.register(AtomicContent.itemAntimatter)
        itemDeuteriumCell = manager.newItem("cellDeuterium", new ItemCell("cellDeuterium"))
        itemTritiumCell = manager.newItem("cellTritium", new ItemCell("cellTritium"))
        itemWaterCell = manager.newItem("cellWater", new ItemCell("cellWater"))
        itemYellowCake = manager.newItem("yellowcake", new ItemRadioactive()).setTextureName(Reference.prefix + "yellowcake").setCreativeTab(ResonantTab)
        itemUranium = manager.newItem(classOf[ItemUranium]).setCreativeTab(ResonantTab)

        //Buckets
        itemBucketToxic = manager.newItem("bucketToxicWaste", new ItemBucket(AtomicContent.blockPlasma)).setCreativeTab(ResonantTab.tab).setContainerItem(Items.bucket).setTextureName(Reference.prefix + "bucketToxicWaste")

        //Hazmat suit
        itemHazmatTop = manager.newItem("hazmatMask", new ItemHazmat("hazmatMask", 0))
        itemHazmatBody = manager.newItem("hazmatBody", new ItemHazmat("hazmatBody", 1))
        itemHazmatLeggings = manager.newItem("hazmatLeggings", new ItemHazmat("hazmatLeggings", 2))
        itemHazmatBoots = manager.newItem("hazmatBoots", new ItemHazmat("hazmatBoots", 3))

        FluidRegistry.registerFluid(AtomicContent.FLUID_URANIUM_HEXAFLOURIDE)
        FluidRegistry.registerFluid(AtomicContent.FLUID_STEAM)
        FluidRegistry.registerFluid(AtomicContent.getFluidTritium)
        FluidRegistry.registerFluid(AtomicContent.FLUID_DEUTERIUM)
        FluidRegistry.registerFluid(AtomicContent.getFluidToxicWaste)
        FluidRegistry.registerFluid(AtomicContent.FLUID_PLASMA)

        TileCreativeBuilder.register(new SchematicAccelerator)
        TileCreativeBuilder.register(new SchematicBreedingReactor)
        TileCreativeBuilder.register(new SchematicFissionReactor)
        TileCreativeBuilder.register(new SchematicFusionReactor)

        AtomicContent.FLUID_PLASMA.setBlock(AtomicContent.blockPlasma)
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
    }

    override def init()
    {
        super.init()
    }

    override def postInit()
    {
        super.postInit()
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
        EntityRegistry.registerModEntity(classOf[EntityParticle], "ASParticle", ENTITY_ID_PREFIX, ResonantInduction, 80, 3, true)
        Settings.config.load
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
        Settings.config.save

        recipes += shapeless(new ItemStack(itemAntimatter, 1, 1), itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, AtomicContent.itemAntimatter, itemAntimatter, itemAntimatter)
        recipes += shapeless(new ItemStack(itemAntimatter, 8, 0), new ItemStack(AtomicContent.itemAntimatter, 1, 1))


        recipes += shaped(new ItemStack(blockSteamFunnel, 2), " B ", "B B", "B B", 'B', UniversalRecipe.SECONDARY_METAL.get)
        recipes += shaped(new ItemStack(blockSteamFunnel, 2), " B ", "B B", "B B", 'B', "ingotIron")

        recipes += shaped(blockQuantumAssembler, "CCC", "SXS", "SSS", 'X', blockCentrifuge, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
        recipes += shaped(blockFulmination, "OSO", "SCS", "OSO", 'O', Blocks.obsidian, 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
        recipes += shaped(blockAccelerator, "SCS", "CMC", "SCS", 'M', UniversalRecipe.MOTOR.get, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
        recipes += shaped(blockCentrifuge, "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)
        recipes += shaped(blockNuclearBoiler, "S S", "FBF", "SMS", 'F', Blocks.furnace, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', Items.bucket, 'M', UniversalRecipe.MOTOR.get)
        recipes += shaped(blockChemicalExtractor, "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)

        recipes += shaped(new ItemStack(blockSiren, 2), "NPN", 'N', Blocks.noteblock, 'P', UniversalRecipe.SECONDARY_PLATE.get)
        recipes += shaped(blockReactorCell, "SCS", "MEM", "SCS", 'E', "cellEmpty", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'M', UniversalRecipe.MOTOR.get)
        recipes += shaped(blockFusionCore, "CPC", "PFP", "CPC", 'P', UniversalRecipe.PRIMARY_PLATE.get, 'F', AtomicContent.blockReactorCell, 'C', UniversalRecipe.CIRCUIT_T3.get)
        recipes += shaped(new ItemStack(itemCell, 16), " T ", "TGT", " T ", 'T', "ingotTin", 'G', Blocks.glass)
        recipes += shaped(blockThermometer, "SSS", "GCG", "GSG", 'S', UniversalRecipe.PRIMARY_METAL.get, 'G', Blocks.glass, 'C', UniversalRecipe.CIRCUIT_T1.get)
        recipes += shaped(blockControlRod, "I", "I", "I", 'I', Items.iron_ingot)
        recipes += shaped(itemFissileFuel, "CUC", "CUC", "CUC", 'U', "ingotUranium", 'C', "cellEmpty")
        recipes += shaped(itemBreedingRod, "CUC", "CUC", "CUC", 'U', "breederUranium", 'C', "cellEmpty")

        //Hazmat recipes
        recipes += shaped(itemHazmatTop, "SSS", "BAB", "SCS", 'A', Items.leather_helmet, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
        recipes += shaped(itemHazmatBody, "SSS", "BAB", "SCS", 'A', Items.leather_chestplate, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
        recipes += shaped(itemHazmatLeggings, "SSS", "BAB", "SCS", 'A', Items.leather_leggings, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
        recipes += shaped(itemHazmatBoots, "SSS", "BAB", "SCS", 'A', Items.leather_boots, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)

        //Electro magnets
        recipes += shaped(new ItemStack(blockElectromagnet, 2, 0), "BBB", "BMB", "BBB", 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)
        recipes += shapeless(new ItemStack(AtomicContent.blockElectromagnet, 1, 1), AtomicContent.blockElectromagnet, Blocks.glass)

        recipes += shapeless(new ItemStack(AtomicContent.itemWaterCell), "cellEmpty", Items.water_bucket)

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
        AtomicContent.getFluidTritium.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "tritium"))
        AtomicContent.getFluidToxicWaste.setIcons(AtomicContent.blockToxicWaste.getIcon(0, 0))
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


    def FLUID_PLASMA: Fluid = new Fluid("plasma").setGaseous(true)


    def FLUIDSTACK_WATER: FluidStack = new FluidStack(FluidRegistry.WATER, 0)

    def FLUIDSTACK_URANIUM_HEXAFLOURIDE: FluidStack = new FluidStack(AtomicContent.FLUID_URANIUM_HEXAFLOURIDE, 0)

    def FLUID_URANIUM_HEXAFLOURIDE: Fluid =
    {
        var fluid: Fluid = FluidRegistry.getFluid("uraniumhexafluoride");
        if (fluid == null)
        {
            fluid = new Fluid("uraniumhexafluoride").setGaseous(true)
            FluidRegistry.registerFluid(fluid)
        }
        return fluid
    }

    def FLUIDSTACK_STEAM: FluidStack = new FluidStack(FLUID_STEAM, 0)

    def FLUID_STEAM: Fluid =
    {
        var fluid: Fluid = FluidRegistry.getFluid("steam");
        if (fluid == null)
        {
            fluid = new Fluid("steam").setGaseous(true)
            FluidRegistry.registerFluid(fluid)
        }
        return fluid
    }

    def FLUIDSTACK_DEUTERIUM: FluidStack = new FluidStack(FLUID_DEUTERIUM, 0)

    def FLUID_DEUTERIUM: Fluid =
    {
        var fluid: Fluid = FluidRegistry.getFluid("deuterium");
        if (fluid == null)
        {
            fluid = new Fluid("deuterium").setGaseous(true)
            FluidRegistry.registerFluid(fluid)
        }
        return fluid
    }

    def getFluidStackTritium: FluidStack = new FluidStack(getFluidTritium, 0)

    /** Gets the Fluid instance of Tritium */
    def getFluidTritium: Fluid =
    {
        var fluid: Fluid = FluidRegistry.getFluid("tritium");
        if (fluid == null)
        {
            fluid = new Fluid("tritium").setGaseous(true)
            FluidRegistry.registerFluid(fluid)
        }
        return fluid
    }

    /** Gets a FluidStack of Toxic Waste */
    def getStackToxicWaste: FluidStack = new FluidStack(getFluidToxicWaste, 0)

    /** Gets the Fluid instance of Toxic Waste */
    def getFluidToxicWaste: Fluid =
    {
        var fluid: Fluid = FluidRegistry.getFluid("toxicwaste");
        if (fluid == null)
        {
            fluid = new Fluid("toxicwaste").setGaseous(true)
            FluidRegistry.registerFluid(fluid)
        }
        return fluid
    }
}
