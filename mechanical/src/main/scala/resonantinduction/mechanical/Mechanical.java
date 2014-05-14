package resonantinduction.mechanical;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.api.IMechanicalNode;
import resonant.core.content.debug.BlockCreativeBuilder;
import resonant.lib.content.ContentRegistry;
import resonant.lib.grid.NodeRegistry;
import resonant.lib.network.PacketAnnotation;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.item.ItemBlockMetadata;
import resonant.lib.recipe.UniversalRecipe;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.mechanical.belt.BlockConveyorBelt;
import resonantinduction.mechanical.belt.TileConveyorBelt;
import resonantinduction.mechanical.energy.gear.ItemGear;
import resonantinduction.mechanical.energy.gearshaft.ItemGearShaft;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.turbine.BlockWaterTurbine;
import resonantinduction.mechanical.energy.turbine.BlockWindTurbine;
import resonantinduction.mechanical.energy.turbine.SchematicWaterTurbine;
import resonantinduction.mechanical.energy.turbine.SchematicWindTurbine;
import resonantinduction.mechanical.energy.turbine.TileWaterTurbine;
import resonantinduction.mechanical.energy.turbine.TileWindTurbine;
import resonantinduction.mechanical.fluid.pipe.EnumPipeMaterial;
import resonantinduction.mechanical.fluid.pipe.ItemPipe;
import resonantinduction.mechanical.fluid.transport.TilePump;
import resonantinduction.mechanical.logistic.belt.BlockDetector;
import resonantinduction.mechanical.logistic.belt.BlockManipulator;
import resonantinduction.mechanical.logistic.belt.TileDetector;
import resonantinduction.mechanical.logistic.belt.TileManipulator;
import resonantinduction.mechanical.logistic.belt.TileSorter;
import resonantinduction.mechanical.process.crusher.TileMechanicalPiston;
import resonantinduction.mechanical.process.edit.TileBreaker;
import resonantinduction.mechanical.process.edit.TilePlacer;
import resonantinduction.mechanical.process.grinder.TileGrindingWheel;
import resonantinduction.mechanical.process.purifier.TileMixer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Resonant Induction Mechanical Module
 * 
 * @author DarkCow, Calclavia
 */
@Mod(modid = Mechanical.ID, name = Mechanical.NAME, version = Reference.VERSION, dependencies = "before:ThermalExpansion;required-after:" + ResonantInduction.ID + ";after:ResonantInduction|Archaic")
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Mechanical
{
	/** Mod Information */
	public static final String ID = "ResonantInduction|Mechanical";
	public static final String NAME = Reference.NAME + " Mechanical";

	@Instance(ID)
	public static Mechanical INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.mechanical.ClientProxy", serverSide = "resonantinduction.mechanical.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID).setPrefix(Reference.PREFIX).setTab(TabRI.DEFAULT);

	// Energy
	public static Item itemGear;
	public static Item itemGearShaft;
	public static Block blockWindTurbine;
	public static Block blockWaterTurbine;

	// Transport
	public static Block blockConveyorBelt;
	public static Block blockManipulator;
	public static Block blockDetector;
	// public static Block blockRejector;
	public static Block blockSorter;

	// Fluids
	public static Block blockReleaseValve;
	public static Block blockPump;
	public static Item itemPipe;

	// Machines/Processes
	public static Block blockGrinderWheel;
	public static Block blockMixer;
	public static Block blockMechanicalPiston;

    // Block Breaker and Placer
    public static Block blockTileBreaker;
    public static Block blockTilePlacer;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		MinecraftForge.EVENT_BUS.register(new MicroblockHighlightHandler());
		BlockCreativeBuilder.register(new SchematicWindTurbine());
		BlockCreativeBuilder.register(new SchematicWaterTurbine());
		NodeRegistry.register(IMechanicalNode.class, MechanicalNode.class);

		Settings.CONFIGURATION.load();

		itemGear = contentRegistry.createItem(ItemGear.class);
		itemGearShaft = contentRegistry.createItem(ItemGearShaft.class);
		blockWindTurbine = contentRegistry.createBlock(BlockWindTurbine.class, ItemBlockMetadata.class, TileWindTurbine.class);
		blockWaterTurbine = contentRegistry.createBlock(BlockWaterTurbine.class, ItemBlockMetadata.class, TileWaterTurbine.class);

		blockConveyorBelt = contentRegistry.createTile(BlockConveyorBelt.class, TileConveyorBelt.class);
		blockManipulator = contentRegistry.createTile(BlockManipulator.class, TileManipulator.class);
		blockDetector = contentRegistry.createTile(BlockDetector.class, TileDetector.class);
		// blockRejector = contentRegistry.createTile(BlockRejector.class, TileRejector.class);
		blockSorter = contentRegistry.newBlock(TileSorter.class);

		blockPump = contentRegistry.newBlock(TilePump.class);

		itemPipe = contentRegistry.createItem(ItemPipe.class);

		// Machines
		blockGrinderWheel = contentRegistry.newBlock(TileGrindingWheel.class);
		blockMixer = contentRegistry.newBlock(TileMixer.class);
		blockMechanicalPiston = contentRegistry.newBlock(TileMechanicalPiston.class);
		OreDictionary.registerOre("gear", itemGear);

        blockTileBreaker = contentRegistry.newBlock(TileBreaker.class);
        blockTilePlacer = contentRegistry.newBlock(TilePlacer.class);

		proxy.preInit();
		Settings.CONFIGURATION.save();

		TabRI.ITEMSTACK = new ItemStack(blockGrinderWheel);

		PacketAnnotation.register(TileWindTurbine.class);
		PacketAnnotation.register(TileWaterTurbine.class);
		PacketAnnotation.register(TileSorter.class);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		MultipartMechanical.INSTANCE = new MultipartMechanical();
		Settings.setModMetadata(metadata, ID, NAME, ResonantInduction.ID);
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		// Add recipes
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGear, 1, 0), "SWS", "W W", "SWS", 'W', "plankWood", 'S', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGear, 1, 1), " W ", "WGW", " W ", 'G', new ItemStack(itemGear, 1, 0), 'W', Block.cobblestone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGear, 1, 2), " W ", "WGW", " W ", 'G', new ItemStack(itemGear, 1, 1), 'W', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGearShaft, 1, 0), "S", "S", "S", 'S', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGearShaft, 1, 1), "S", "G", "S", 'G', new ItemStack(itemGearShaft, 1, 0), 'S', Block.cobblestone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemGearShaft, 1, 2), "S", "G", "S", 'G', new ItemStack(itemGearShaft, 1, 1), 'S', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConveyorBelt, 4), "III", "GGG", 'I', Item.ingotIron, 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockManipulator, "SSS", "SRS", "SCS", 'S', Item.ingotIron, 'C', blockConveyorBelt, 'R', Block.blockRedstone));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockDetector, "SWS", "SRS", "SWS", 'S', Item.ingotIron, 'W', UniversalRecipe.WIRE.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockSorter, "SSS", "SPS", "SRS", 'P', Block.pistonStickyBase, 'S', Item.ingotIron, 'R', Block.blockRedstone));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWindTurbine, 1, 0), "CWC", "WGW", "CWC", 'G', itemGear, 'C', Block.cloth, 'W', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWindTurbine, 1, 1), "CWC", "WGW", "CWC", 'G', new ItemStack(blockWindTurbine, 1, 0), 'C', Block.stone, 'W', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWindTurbine, 1, 2), "CWC", "WGW", "CWC", 'G', new ItemStack(blockWindTurbine, 1, 1), 'C', Item.ingotIron, 'W', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWaterTurbine, 1, 0), "SWS", "WGW", "SWS", 'G', itemGear, 'W', "plankWood", 'S', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWaterTurbine, 1, 1), "SWS", "WGW", "SWS", 'G', new ItemStack(blockWaterTurbine, 1, 0), 'W', Block.stone, 'S', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWaterTurbine, 1, 2), "SWS", "WGW", "SWS", 'G', new ItemStack(blockWaterTurbine, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get(), 'S', Item.stick));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockPump, "PPP", "GGG", "PPP", 'P', itemPipe, 'G', new ItemStack(itemGear, 1, 2)));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, EnumPipeMaterial.CERAMIC.ordinal()), "BBB", "   ", "BBB", 'B', Item.brick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, EnumPipeMaterial.BRONZE.ordinal()), "BBB", "   ", "BBB", 'B', "ingotBronze"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, EnumPipeMaterial.PLASTIC.ordinal()), "BBB", "   ", "BBB", 'B', UniversalRecipe.RUBBER.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, EnumPipeMaterial.IRON.ordinal()), "BBB", "   ", "BBB", 'B', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, EnumPipeMaterial.STEEL.ordinal()), "BBB", "   ", "BBB", 'B', "ingotSteel"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 3, EnumPipeMaterial.FIBERGLASS.ordinal()), "BBB", "   ", "BBB", 'B', Item.diamond));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockMechanicalPiston, "SGS", "SPS", "SRS", 'P', Block.pistonBase, 'S', Item.ingotIron, 'R', Item.redstone, 'G', new ItemStack(itemGear, 1, 2)));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockGrinderWheel, "III", "LGL", "III", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'L', "logWood", 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockMixer, "IGI", "IGI", "IGI", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'G', itemGear));

        // block break and placer recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(blockTileBreaker, "CGC", "CPC", "CDC", 'C', Block.cobblestone, 'G', itemGear, 'P', Block.pistonBase, 'D', Item.pickaxeDiamond));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockTilePlacer, "CGC", "CSC", "CRC", 'C', Block.cobblestone, 'G', itemGear, 'S', Item.ingotIron, 'R', Block.blockRedstone));
	}
}
