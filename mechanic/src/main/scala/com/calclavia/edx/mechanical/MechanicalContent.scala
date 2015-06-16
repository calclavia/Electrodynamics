package com.calclavia.edx.mechanical

import com.calclavia.edx.core.Reference
import com.calclavia.edx.mechanical.fluid.pipe.{ItemPipe, PipeMaterials, PartPipe}
import com.calclavia.edx.mechanical.fluid.transport.TilePump
import com.calclavia.edx.mechanical.machine.TileDetector
import com.calclavia.edx.mechanical.machine.edit.{TilePlacer, TileBreaker}
import com.calclavia.edx.mechanical.mech.gear.{ItemGear, PartGear}
import com.calclavia.edx.mechanical.mech.gearshaft.{ItemGearShaft, PartGearShaft}
import com.calclavia.edx.mechanical.mech.grid.NodeMechanical
import com.calclavia.edx.mechanical.mech.process.crusher.TileMechanicalPiston
import com.calclavia.edx.mechanical.mech.process.grinder.TileGrindingWheel
import com.calclavia.edx.mechanical.mech.process.mixer.TileMixer
import com.calclavia.edx.mechanical.mech.turbine.{TileWindTurbine, TileWaterTurbine}
import edx.core.interfaces.TNodeMechanical
import edx.core.{EDXCreativeTab, Reference, ResonantPartFactory}
import edx.mechanical.fluid.pipe.ItemPipe
import edx.mechanical.machine.edit.TilePlacer
import edx.mechanical.mech.gear.ItemGear
import edx.mechanical.mech.gearshaft.ItemGearShaft
import edx.mechanical.mech.turbine.TileWindTurbine
import net.minecraft.block.Block
import nova.core.block.BlockFactory
import nova.core.item.{ItemFactory, Item}
import resonantengine.api.graph.node.NodeRegistry
import resonantengine.lib.schematic.{SchematicPlate, SchematicRegistry}
import resonantengine.lib.utility.recipe.UniversalRecipe
import resonantengine.prefab.modcontent.ContentHolder

/**
 * The core contents of Resonant Induction
 * @author Calclavia
 */
object MechanicalContent extends ContentLoader
{
	//Items
	var itemGear: ItemFactory = new ItemGear
	var itemGearShaft: ItemFactory = new ItemGearShaft
	var itemPipe: ItemFactory = new ItemPipe

	//Blocks
	var blockWindTurbine: BlockFactory = new TileWindTurbine
	var blockWaterTurbine: BlockFactory = new TileWaterTurbine
	var blockDetector: BlockFactory = new TileDetector
	var blockPump: BlockFactory = new TilePump
	var blockGrinderWheel: BlockFactory = new TileGrindingWheel
	var blockMixer: BlockFactory = new TileMixer
	var blockMechanicalPiston: BlockFactory = new TileMechanicalPiston
	var blockTileBreaker: BlockFactory = new TileBreaker
	var blockTilePlacer: BlockFactory = new TilePlacer
	/*
  override def preInit()
  {
    super.preInit()

    EDXCreativeTab.itemStack = new ItemStack(blockPump)

    SchematicRegistry.register("resonantinduction.mechanical.waterTurbine", new SchematicPlate("schematic.waterTurbine.name", MechanicalContent.blockWaterTurbine))
    SchematicRegistry.register("resonantinduction.mechanical.windTurbine", new SchematicPlate("schematic.windTurbine.name", MechanicalContent.blockWindTurbine))

    NodeRegistry.register(classOf[TNodeMechanical], classOf[NodeMechanical])

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
  }*/

	/**
	 * @SubscribeEvent
	@SideOnly(Side.CLIENT)
  def drawBlockHighlight(event: DrawBlockHighlightEvent)
  {
    if (event.currentItem != null && event.currentItem.getItem.isInstanceOf[IHighlight] && event.target != null && event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
    {
      GL11.glPushMatrix()
      RenderUtils.translateToWorldCoords(event.player, event.partialTicks)
      val hit: Vector3 = new Vector3(event.target.hitVec)
      val t = event.currentItem.getItem.asInstanceOf[IHighlight].getHighlightType
      if (t == 0)
      {
        FacePlacementGrid.render(hit, event.target.sideHit)
      }
      if (t == 1)
      {
        CornerPlacementGrid.render(hit, event.target.sideHit)
      }
      event.setCanceled(true)
      GL11.glPopMatrix()
    }
	 */
}
