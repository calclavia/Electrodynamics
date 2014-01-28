package resonantinduction.mechanical;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.electrical.purifier.BlockMixer;
import resonantinduction.mechanical.belt.BlockConveyorBelt;
import resonantinduction.mechanical.belt.TileConveyorBelt;
import resonantinduction.mechanical.fluid.pipe.ItemBlockFluidContainer;
import resonantinduction.mechanical.fluid.pipe.ItemPipe;
import resonantinduction.mechanical.fluid.pump.BlockGrate;
import resonantinduction.mechanical.fluid.pump.TileGrate;
import resonantinduction.mechanical.fluid.tank.BlockTank;
import resonantinduction.mechanical.fluid.tank.TileTank;
import resonantinduction.mechanical.gear.ItemGear;
import resonantinduction.mechanical.item.ItemPipeGauge;
import resonantinduction.mechanical.logistic.BlockDetector;
import resonantinduction.mechanical.logistic.BlockManipulator;
import resonantinduction.mechanical.logistic.BlockRejector;
import resonantinduction.mechanical.logistic.TileDetector;
import resonantinduction.mechanical.logistic.TileManipulator;
import resonantinduction.mechanical.logistic.TileRejector;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.PacketNetwork;
import resonantinduction.mechanical.process.BlockFilter;
import resonantinduction.mechanical.process.BlockGrinderWheel;
import resonantinduction.mechanical.process.TileGrinderWheel;
import resonantinduction.mechanical.process.TileMixer;
import calclavia.lib.content.ContentRegistry;
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

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, ID);

	// Energy
	public static Item itemGear;
	public static Block itemGearShaft;

	// Transport
	public static Block blockConveyorBelt;
	public static Block blockManipulator;
	public static Block blockDetector;
	public static Block blockRejector;

	// Fluids
	public static Block blockTank;
	public static Block blockReleaseValve;
	public static Block blockGrate;

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
		itemGear = contentRegistry.createItem(ItemGear.class);

		blockConveyorBelt = contentRegistry.createTile(BlockConveyorBelt.class, TileConveyorBelt.class);
		blockManipulator = contentRegistry.createTile(BlockManipulator.class, TileManipulator.class);
		blockDetector = contentRegistry.createTile(BlockDetector.class, TileDetector.class);
		blockRejector = contentRegistry.createTile(BlockRejector.class, TileRejector.class);

		blockTank = contentRegistry.createBlock(BlockTank.class, ItemBlockFluidContainer.class, TileTank.class);
		blockGrate = contentRegistry.createTile(BlockGrate.class, TileGrate.class);

		itemPipeGuage = contentRegistry.createItem(ItemPipeGauge.class);
		itemPipe = contentRegistry.createItem(ItemPipe.class);

		// Machines
		blockGrinderWheel = contentRegistry.createTile(BlockGrinderWheel.class, TileGrinderWheel.class);
		blockPurifier = contentRegistry.createTile(BlockMixer.class, TileMixer.class);
		blockFilter = contentRegistry.createBlock(BlockFilter.class);
		OreDictionary.registerOre("gear", itemGear);

		proxy.preInit();
		Settings.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		MultipartMechanical.INSTANCE = new MultipartMechanical();
		Settings.setModMetadata(metadata, ID, NAME);
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		// Add recipes
		GameRegistry.addRecipe(new ShapedOreRecipe(itemGear, "SWS", "W W", "SWS", 'W', "plankWood", 'S', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConveyorBelt, 4), "III", "GGG", 'I', Item.ingotIron, 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockManipulator, "SSS", "SRS", "SCS", 'S', Item.ingotIron, 'C', blockConveyorBelt, 'R', Block.blockRedstone));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockDetector, "SWS", "SRS", "SWS", 'S', Item.ingotIron, 'W', UniversalRecipe.WIRE.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockRejector, "S S", "SPS", "SRS", 'P', Block.pistonBase, 'S', Item.ingotIron, 'R', Item.redstone));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockTank, "GGG", "GSG", "GGG", 'G', Block.glass, 'S', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockGrate, "BBB", "B B", "BBB", 'B', Block.fenceIron));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemPipe, 4), "BBB", "GGG", "BBB", 'B', UniversalRecipe.SECONDARY_METAL.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(itemPipeGuage, "RRR", "GGG", " S ", 'S', Item.stick, 'G', Block.glass, 'R', Item.redstone));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockGrinderWheel, "III", "LGL", "III", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'L', "logWood", 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockPurifier, "IGI", "IGI", "IGI", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'G', itemGear));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockFilter, "BBB", "PPP", "BBB", 'B', Block.fenceIron, 'P', Item.paper));

	}
}
