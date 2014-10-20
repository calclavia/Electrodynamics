package resonantinduction.mechanical

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import resonant.content.loader.ContentHolder
import resonant.lib.network.discriminator.PacketAnnotationManager
import resonant.lib.recipe.UniversalRecipe
import resonant.lib.schematic.{SchematicPlate, SchematicRegistry}
import resonantinduction.core.interfaces.IMechanicalNode
import resonantinduction.core.{Reference, ResonantPartFactory, ResonantTab}
import resonantinduction.mechanical.fluid.pipe.{ItemPipe, PartPipe, PipeMaterials}
import resonantinduction.mechanical.fluid.transport.TilePump
import resonantinduction.mechanical.machine.TileDetector
import resonantinduction.mechanical.machine.edit.{TileBreaker, TilePlacer}
import resonantinduction.mechanical.mech.MechanicalNode
import resonantinduction.mechanical.mech.gear.{ItemGear, PartGear}
import resonantinduction.mechanical.mech.gearshaft.{ItemGearShaft, PartGearShaft}
import resonantinduction.mechanical.mech.process.crusher.TileMechanicalPiston
import resonantinduction.mechanical.mech.process.grinder.TileGrindingWheel
import resonantinduction.mechanical.mech.process.mixer.TileMixer
import resonantinduction.mechanical.mech.turbine.{TileElectricTurbine, TileWaterTurbine, TileWindTurbine}
import universalelectricity.api.core.grid.NodeRegistry

/**
 * The core contents of Resonant Induction
 * @author Calclavia
 */
object MechanicalContent extends ContentHolder
{
    //Constructor
    manager.setTab(ResonantTab)
    manager.setPrefix(Reference.prefix)

    //Content
    var itemGear: Item = null
    var itemGearShaft: Item = null
    var itemPipe: Item = null

    var blockWindTurbine: Block = null
    var blockWaterTurbine: Block = null
    var blockElectricTurbine: Block = null
    var blockDetector: Block = null
    var blockPump: Block = null
    var blockGrinderWheel: Block = null
    var blockMixer: Block = null
    var blockMechanicalPiston: Block = null
    var blockTileBreaker: Block = null
    var blockTilePlacer: Block = null

    override def preInit()
    {
        super.preInit()

        itemGear = manager.newItem(classOf[ItemGear])
        itemGearShaft = manager.newItem(classOf[ItemGearShaft])
        itemPipe = manager.newItem(classOf[ItemPipe])

        blockWindTurbine = manager.newBlock(classOf[TileWindTurbine])
        blockWaterTurbine= manager.newBlock(classOf[TileWaterTurbine])
        blockElectricTurbine= manager.newBlock(classOf[TileElectricTurbine])
        blockDetector = manager.newBlock(classOf[TileDetector])
        blockPump = manager.newBlock(classOf[TilePump])
        blockGrinderWheel= manager.newBlock(classOf[TileGrindingWheel])
        blockMixer = manager.newBlock(classOf[TileMixer])
        blockMechanicalPiston = manager.newBlock(classOf[TileMechanicalPiston])
        blockTileBreaker = manager.newBlock(classOf[TileBreaker])
        blockTilePlacer = manager.newBlock(classOf[TilePlacer])

        SchematicRegistry.register("resonantinduction.mechanical.waterTurbine",new SchematicPlate("schematic.waterTurbine.name", MechanicalContent.blockWaterTurbine))
        SchematicRegistry.register("resonantinduction.mechanical.windTurbine",new SchematicPlate("schematic.windTurbine.name", MechanicalContent.blockWindTurbine))
        SchematicRegistry.register("resonantinduction.mechanical.electricalTurbine",new SchematicPlate("schematic.electricTurbine.name", MechanicalContent.blockElectricTurbine))

        NodeRegistry.register(classOf[IMechanicalNode], classOf[MechanicalNode])

        ResonantTab.itemStack(new ItemStack(MechanicalContent.blockGrinderWheel))

        PacketAnnotationManager.INSTANCE.register(classOf[TileWindTurbine])
        PacketAnnotationManager.INSTANCE.register(classOf[TileWaterTurbine])

        ResonantPartFactory.register(classOf[PartGear])
        ResonantPartFactory.register(classOf[PartGearShaft])
        ResonantPartFactory.register(classOf[PartPipe])
    }

    override def init()
    {
        super.init()
    }

    /**
     * Recipe registration
     */
    override def postInit()
    {
        //recipes += shaped(new ItemStack(decoration, 8, 3), "XXX", "XCX", "XXX", 'X', Blocks.cobblestone, 'C', new ItemStack(Items.coal, 1, 1))
        recipes += shaped(new ItemStack(itemGear, 1, 0), "SWS", "W W", "SWS", 'W', "plankWood", 'S', Items.stick)
        recipes += shaped(new ItemStack(itemGear, 1, 1), " W ", "WGW", " W ", 'G', new ItemStack(itemGear, 1, 0), 'W', Blocks.cobblestone)
        recipes += shaped(new ItemStack(itemGear, 1, 2), " W ", "WGW", " W ", 'G', new ItemStack(itemGear, 1, 1), 'W', Items.iron_ingot)

        recipes += shaped(new ItemStack(itemGearShaft, 1, 0), "S", "S", "S", 'S', Items.stick)
        recipes += shaped(new ItemStack(itemGearShaft, 1, 1), "S", "G", "S", 'G', new ItemStack(itemGearShaft, 1, 0), 'S', Blocks.cobblestone)
        recipes += shaped(new ItemStack(itemGearShaft, 1, 2), "S", "G", "S", 'G', new ItemStack(itemGearShaft, 1, 1), 'S', Items.iron_ingot)

        recipes += shaped(blockDetector, "SWS", "SRS", "SWS", 'S', Items.iron_ingot, 'W', UniversalRecipe.WIRE.get)

        recipes += shaped(new ItemStack(blockWindTurbine, 1, 0), "CWC", "WGW", "CWC", 'G', itemGear, 'C', Blocks.wool, 'W', Items.stick)
        recipes += shaped(new ItemStack(blockWindTurbine, 1, 1), "CWC", "WGW", "CWC", 'G', new ItemStack(blockWindTurbine, 1, 0), 'C', Blocks.stone, 'W', Items.stick)
        recipes += shaped(new ItemStack(blockWindTurbine, 1, 2), "CWC", "WGW", "CWC", 'G', new ItemStack(blockWindTurbine, 1, 1), 'C', Items.iron_ingot, 'W', Items.stick)

        recipes += shaped(new ItemStack(blockWaterTurbine, 1, 0), "SWS", "WGW", "SWS", 'G', itemGear, 'W', "plankWood", 'S', Items.stick)
        recipes += shaped(new ItemStack(blockWaterTurbine, 1, 1), "SWS", "WGW", "SWS", 'G', new ItemStack(blockWaterTurbine, 1, 0), 'W', Blocks.stone, 'S', Items.stick)
        recipes += shaped(new ItemStack(blockWaterTurbine, 1, 2), "SWS", "WGW", "SWS", 'G', new ItemStack(blockWaterTurbine, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get, 'S', Items.stick)

        recipes += shaped(blockElectricTurbine, " B ", "BMB", " B ", 'B', UniversalRecipe.SECONDARY_PLATE.get, 'M', UniversalRecipe.MOTOR.get)
        recipes += shaped(blockPump, "PPP", "GGG", "PPP", 'P', itemPipe, 'G', new ItemStack(itemGear, 1, 2))

        recipes += shaped(new ItemStack(itemPipe, 3, PipeMaterials.ceramic.id), "BBB", "   ", "BBB", 'B', Items.brick)
        recipes += shaped(new ItemStack(itemPipe, 3, PipeMaterials.bronze.id), "BBB", "   ", "BBB", 'B', "ingotBronze")
        recipes += shaped(new ItemStack(itemPipe, 3, PipeMaterials.plastic.id), "BBB", "   ", "BBB", 'B', UniversalRecipe.RUBBER.get)
        recipes += shaped(new ItemStack(itemPipe, 3, PipeMaterials.iron.id), "BBB", "   ", "BBB", 'B', Items.iron_ingot)
        recipes += shaped(new ItemStack(itemPipe, 3, PipeMaterials.steel.id), "BBB", "   ", "BBB", 'B', "ingotSteel")
        recipes += shaped(new ItemStack(itemPipe, 3, PipeMaterials.fiberglass.id), "BBB", "   ", "BBB", 'B', Items.diamond)

        recipes += shaped(blockMechanicalPiston, "SGS", "SPS", "SRS", 'P', Blocks.piston, 'S', Items.iron_ingot, 'R', Items.redstone, 'G', new ItemStack(itemGear, 1, 2))
        recipes += shaped(blockGrinderWheel, "III", "LGL", "III", 'I', UniversalRecipe.PRIMARY_METAL.get, 'L', "logWood", 'G', itemGear)
        recipes += shaped(blockMixer, "IGI", "IGI", "IGI", 'I', UniversalRecipe.PRIMARY_METAL.get, 'G', itemGear)
        recipes += shaped(blockTileBreaker, "CGC", "CPC", "CDC", 'C', Blocks.cobblestone, 'G', itemGear, 'P', Blocks.piston, 'D', Items.diamond_pickaxe)
        recipes += shaped(blockTilePlacer, "CGC", "CSC", "CRC", 'C', Blocks.cobblestone, 'G', itemGear, 'S', Items.iron_ingot, 'R', Blocks.redstone_block)
    }
}
