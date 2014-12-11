package resonantinduction.mechanical

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import resonant.api.tile.node.NodeRegistry
import resonant.lib.network.discriminator.PacketAnnotationManager
import resonant.lib.mod.content.ContentHolder
import resonant.lib.utility.recipe.UniversalRecipe
import resonant.lib.world.schematic.{SchematicPlate, SchematicRegistry}
import resonantinduction.core.interfaces.TNodeMechanical
import resonantinduction.core.{RICreativeTab, Reference, ResonantPartFactory}
import resonantinduction.mechanical.fluid.pipe.{ItemPipe, PartPipe, PipeMaterials}
import resonantinduction.mechanical.fluid.transport.TilePump
import resonantinduction.mechanical.machine.TileDetector
import resonantinduction.mechanical.machine.edit.{TileBreaker, TilePlacer}
import resonantinduction.mechanical.mech.gear.{ItemGear, PartGear}
import resonantinduction.mechanical.mech.gearshaft.{ItemGearShaft, PartGearShaft}
import resonantinduction.mechanical.mech.grid.NodeMechanical
import resonantinduction.mechanical.mech.process.crusher.TileMechanicalPiston
import resonantinduction.mechanical.mech.process.grinder.TileGrindingWheel
import resonantinduction.mechanical.mech.process.mixer.TileMixer
import resonantinduction.mechanical.mech.turbine.{TileWaterTurbine, TileWindTurbine}

/**
 * The core contents of Resonant Induction
 * @author Calclavia
 */
object MechanicalContent extends ContentHolder
{
  //Constructor
  manager.setTab(RICreativeTab)
  manager.setPrefix(Reference.prefix)

  //Content
  var itemGear: Item = new ItemGear
  var itemGearShaft: Item = new  ItemGearShaft
  var itemPipe: Item = new ItemPipe

  var blockWindTurbine: Block = new TileWindTurbine
  var blockWaterTurbine: Block = new TileWaterTurbine
  var blockDetector: Block = new TileDetector
  var blockPump: Block = new TilePump
  var blockGrinderWheel: Block = new TileGrindingWheel
  var blockMixer: Block = new TileMixer
  var blockMechanicalPiston: Block = new TileMechanicalPiston
  var blockTileBreaker: Block = new TileBreaker
  var blockTilePlacer: Block = new TilePlacer

  override def preInit()
  {
    super.preInit()

    RICreativeTab.itemStack = new ItemStack(itemGear)

    SchematicRegistry.register("resonantinduction.mechanical.waterTurbine", new SchematicPlate("schematic.waterTurbine.name", MechanicalContent.blockWaterTurbine))
    SchematicRegistry.register("resonantinduction.mechanical.windTurbine", new SchematicPlate("schematic.windTurbine.name", MechanicalContent.blockWindTurbine))

    NodeRegistry.register(classOf[TNodeMechanical], classOf[NodeMechanical])

    PacketAnnotationManager.INSTANCE.register(classOf[TileWindTurbine])
    PacketAnnotationManager.INSTANCE.register(classOf[TileWaterTurbine])

    ResonantPartFactory.register(classOf[PartGear])
    ResonantPartFactory.register(classOf[PartGearShaft])
    ResonantPartFactory.register(classOf[PartPipe])
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
