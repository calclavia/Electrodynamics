package resonantinduction.mechanical;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonantinduction.api.mechanical.IMechanical;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.mechanical.belt.BlockConveyorBelt;
import resonantinduction.mechanical.belt.TileConveyorBelt;
import resonantinduction.mechanical.fluid.pipe.ItemBlockFluidContainer;
import resonantinduction.mechanical.fluid.pipe.ItemPipe;
import resonantinduction.mechanical.fluid.tank.BlockTank;
import resonantinduction.mechanical.fluid.tank.TileTank;
import resonantinduction.mechanical.fluid.transport.BlockGrate;
import resonantinduction.mechanical.fluid.transport.BlockPump;
import resonantinduction.mechanical.fluid.transport.TileGrate;
import resonantinduction.mechanical.fluid.transport.TilePump;
import resonantinduction.mechanical.gear.ItemGear;
import resonantinduction.mechanical.gear.ItemGearShaft;
import resonantinduction.mechanical.item.ItemPipeGauge;
import resonantinduction.mechanical.logistic.belt.BlockDetector;
import resonantinduction.mechanical.logistic.belt.BlockManipulator;
import resonantinduction.mechanical.logistic.belt.BlockRejector;
import resonantinduction.mechanical.logistic.belt.TileDetector;
import resonantinduction.mechanical.logistic.belt.TileManipulator;
import resonantinduction.mechanical.logistic.belt.TileRejector;
import resonantinduction.mechanical.network.PacketNetwork;
import resonantinduction.mechanical.process.BlockFilter;
import resonantinduction.mechanical.process.grinder.BlockGrindingWheel;
import resonantinduction.mechanical.process.grinder.TileGrinderWheel;
import resonantinduction.mechanical.process.purifier.BlockMixer;
import resonantinduction.mechanical.process.purifier.TileMixer;
import resonantinduction.mechanical.turbine.BlockWaterTurbine;
import resonantinduction.mechanical.turbine.BlockWindTurbine;
import resonantinduction.mechanical.turbine.TileWaterTurbine;
import resonantinduction.mechanical.turbine.TileWindTurbine;
import calclavia.lib.content.ContentRegistry;
import calclavia.lib.network.PacketAnnotation;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.recipe.UniversalRecipe;
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
 * Resonant Induction Archaic Module
 * 
 * @author DarkCow, Calclavia
 */
@Mod(modid = Mechanical.ID, name = Mechanical.NAME, version = Reference.VERSION, dependencies = "before:ThermalExpansion;required-after:" + ResonantInduction.ID)
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

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID).setPrefix(Reference.PREFIX).setTab(TabRI.CORE);

	// Energy
	public static Item itemGear;
	public static Item itemGearShaft;
	public static Block windTurbine;
	public static Block waterTurbine;

	// Transport
	public static Block blockConveyorBelt;
	public static Block blockManipulator;
	public static Block blockDetector;
	public static Block blockRejector;

	// Fluids
	public static Block blockTank;
	public static Block blockReleaseValve;
	public static Block blockGrate;
	public static Block blockPump;

	public static Item itemPipe;
	public static Item itemPipeGuage;

	// Machines/Processes
	public static Block blockGrinderWheel;
	public static Block blockPurifier;
	public static Block blockFilter;

	public static final PacketNetwork PACKET_NETWORK = new PacketNetwork(IMechanical.class, Reference.CHANNEL);

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		Settings.load();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		MinecraftForge.EVENT_BUS.register(new MicroblockHighlightHandler());

		itemGear = contentRegistry.createItem(ItemGear.class);
		itemGearShaft = contentRegistry.createItem(ItemGearShaft.class);
		windTurbine = contentRegistry.createTile(BlockWindTurbine.class, TileWindTurbine.class);
		waterTurbine = contentRegistry.createTile(BlockWaterTurbine.class, TileWaterTurbine.class);

		blockConveyorBelt = contentRegistry.createTile(BlockConveyorBelt.class, TileConveyorBelt.class);
		blockManipulator = contentRegistry.createTile(BlockManipulator.class, TileManipulator.class);
		blockDetector = contentRegistry.createTile(BlockDetector.class, TileDetector.class);
		blockRejector = contentRegistry.createTile(BlockRejector.class, TileRejector.class);

		blockTank = contentRegistry.createBlock(BlockTank.class, ItemBlockFluidContainer.class, TileTank.class);
		blockGrate = contentRegistry.createTile(BlockGrate.class, TileGrate.class);
		blockPump = contentRegistry.createTile(BlockPump.class, TilePump.class);

		itemPipeGuage = contentRegistry.createItem(ItemPipeGauge.class);
		itemPipe = contentRegistry.createItem(ItemPipe.class);

		// Machines
		blockGrinderWheel = contentRegistry.createTile(BlockGrindingWheel.class, TileGrinderWheel.class);
		blockPurifier = contentRegistry.createTile(BlockMixer.class, TileMixer.class);
		blockFilter = contentRegistry.createBlock(BlockFilter.class);
		OreDictionary.registerOre("gear", itemGear);

		proxy.preInit();
		Settings.save();

		TabRI.ITEMSTACK = new ItemStack(blockGrinderWheel);

		PacketAnnotation.register(TileWindTurbine.class);
		PacketAnnotation.register(TileWaterTurbine.class);
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
		GameRegistry.addRecipe(new ShapedOreRecipe(itemGearShaft, "S", "S", "S", 'S', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConveyorBelt, 4), "III", "GGG", 'I', Item.ingotIron, 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockManipulator, "SSS", "SRS", "SCS", 'S', Item.ingotIron, 'C', blockConveyorBelt, 'R', Block.blockRedstone));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockDetector, "SWS", "SRS", "SWS", 'S', Item.ingotIron, 'W', UniversalRecipe.WIRE.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockRejector, "S S", "SPS", "SRS", 'P', Block.pistonBase, 'S', Item.ingotIron, 'R', Item.redstone));

		GameRegistry.addRecipe(new ShapedOreRecipe(windTurbine, "CWC", "WGW", "CWC", 'G', itemGear, 'C', Block.cloth, 'W', "plankWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(waterTurbine, " W ", "WGW", " W ", 'G', itemGear, 'W', UniversalRecipe.PRIMARY_METAL.get()));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockTank, "GGG", "GSG", "GGG", 'G', Block.glass, 'S', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockGrate, "BBB", "B B", "BBB", 'B', Block.fenceIron));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 4), "BBB", "   ", "BBB", 'B', UniversalRecipe.SECONDARY_METAL.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(itemPipeGuage, "RRR", "GGG", " S ", 'S', Item.stick, 'G', Block.glass, 'R', Item.redstone));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockGrinderWheel, "III", "LGL", "III", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'L', "logWood", 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockPurifier, "IGI", "IGI", "IGI", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockFilter, "BBB", "PPP", "BBB", 'B', Block.fenceIron, 'P', Item.paper));

	}
}
