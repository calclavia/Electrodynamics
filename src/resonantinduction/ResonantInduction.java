package resonantinduction;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonantinduction.battery.BatteryManager;
import resonantinduction.battery.BlockBattery;
import resonantinduction.battery.ItemCapacitor;
import resonantinduction.battery.TileEntityBattery;
import resonantinduction.contractor.BlockEMContractor;
import resonantinduction.contractor.ItemBlockContractor;
import resonantinduction.contractor.TileEntityEMContractor;
import resonantinduction.entangler.ItemLinker;
import resonantinduction.entangler.ItemQuantumEntangler;
import resonantinduction.multimeter.BlockMultimeter;
import resonantinduction.multimeter.ItemBlockMultimeter;
import resonantinduction.multimeter.TileEntityMultimeter;
import resonantinduction.tesla.BlockTesla;
import resonantinduction.tesla.TileEntityTesla;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
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
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Calclavia
 * 
 */
@Mod(modid = ResonantInduction.ID, name = ResonantInduction.NAME, version = ResonantInduction.VERSION)
@NetworkMod(channels = ResonantInduction.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class ResonantInduction
{
	/**
	 * Mod Information
	 */
	public static final String ID = "resonantinduction";
	public static final String NAME = "Resonant Induction";
	public static final String CHANNEL = "R_INDUC";

	public static final String MAJOR_VERSION = "@MAJOR@";
	public static final String MINOR_VERSION = "@MINOR@";
	public static final String REVISION_VERSION = "@REVIS@";
	public static final String BUILD_VERSION = "@BUILD@";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;

	@Instance(ID)
	public static ResonantInduction INSTNACE;

	@SidedProxy(clientSide = ID + ".ClientProxy", serverSide = ID + ".CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final Logger LOGGER = Logger.getLogger(NAME);

	/**
	 * Directory Information
	 */
	public static final String DOMAIN = "resonantinduction";
	public static final String PREFIX = DOMAIN + ":";
	public static final String DIRECTORY = "/assets/" + DOMAIN + "/";
	public static final String TEXTURE_DIRECTORY = "textures/";
	public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
	public static final String BLOCK_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	public static final String ITEM_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "items/";
	public static final String MODEL_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "models/";

	public static final String LANGUAGE_DIRECTORY = DIRECTORY + "languages/";
	public static final String[] LANGUAGES = new String[] { "en_US" };

	/**
	 * Settings
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), NAME + ".cfg"));
	public static float POWER_PER_COAL = 8;
	public static boolean SOUND_FXS = true;

	/** Block ID by Jyzarc */
	private static final int BLOCK_ID_PREFIX = 3200;
	/** Item ID by Horfius */
	private static final int ITEM_ID_PREFIX = 20150;

	private static int NEXT_BLOCK_ID = BLOCK_ID_PREFIX;
	private static int NEXT_ITEM_ID = ITEM_ID_PREFIX;

	public static int getNextBlockID()
	{
		return NEXT_BLOCK_ID++;
	}

	public static int getNextItemID()
	{
		return NEXT_ITEM_ID++;
	}

	// Items
	public static Item itemQuantumEntangler;
	public static Item itemCapacitor;
	public static Item itemLinker;

	// Blocks
	public static Block blockTesla;
	public static Block blockMultimeter;
	public static Block blockEMContractor;
	public static Block blockBattery;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		LOGGER.setParent(FMLLog.getLogger());
		NetworkRegistry.instance().registerGuiHandler(this, ResonantInduction.proxy);

		CONFIGURATION.load();

		// Config
		POWER_PER_COAL = (float) CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Coal Wattage", POWER_PER_COAL).getDouble(POWER_PER_COAL);
		SOUND_FXS = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Tesla Sound FXs", SOUND_FXS).getBoolean(SOUND_FXS);

		TileEntityEMContractor.ACCELERATION = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Item Acceleration", TileEntityEMContractor.ACCELERATION).getDouble(TileEntityEMContractor.ACCELERATION);
		TileEntityEMContractor.MAX_REACH = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Reach", TileEntityEMContractor.MAX_REACH).getInt(TileEntityEMContractor.MAX_REACH);
		TileEntityEMContractor.MAX_SPEED = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Speed", TileEntityEMContractor.MAX_SPEED).getDouble(TileEntityEMContractor.MAX_SPEED);
		TileEntityEMContractor.PUSH_DELAY = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Item Push Delay", TileEntityEMContractor.PUSH_DELAY).getInt(TileEntityEMContractor.PUSH_DELAY);

		// Items
		itemQuantumEntangler = new ItemQuantumEntangler(getNextItemID());
		itemCapacitor = new ItemCapacitor(getNextItemID());
		itemLinker = new ItemLinker(getNextItemID());

		GameRegistry.registerItem(itemQuantumEntangler, itemQuantumEntangler.getUnlocalizedName());
		GameRegistry.registerItem(itemCapacitor, itemCapacitor.getUnlocalizedName());
		GameRegistry.registerItem(itemLinker, itemLinker.getUnlocalizedName());

		// Blocks
		blockTesla = new BlockTesla(getNextBlockID());
		blockMultimeter = new BlockMultimeter(getNextBlockID());
		blockEMContractor = new BlockEMContractor(getNextBlockID());
		blockBattery = new BlockBattery(getNextBlockID());

		CONFIGURATION.save();

		GameRegistry.registerBlock(blockTesla, blockTesla.getUnlocalizedName());
		GameRegistry.registerBlock(blockMultimeter, ItemBlockMultimeter.class, blockMultimeter.getUnlocalizedName());
		GameRegistry.registerBlock(blockEMContractor, ItemBlockContractor.class, blockEMContractor.getUnlocalizedName());
		GameRegistry.registerBlock(blockBattery, blockBattery.getUnlocalizedName());

		// Tiles
		GameRegistry.registerTileEntity(TileEntityTesla.class, blockTesla.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityMultimeter.class, blockMultimeter.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityEMContractor.class, blockEMContractor.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityBattery.class, blockBattery.getUnlocalizedName());

		TickRegistry.registerTickHandler(new BatteryManager(), Side.SERVER);

		ResonantInduction.proxy.registerRenderers();

		TabRI.ITEMSTACK = new ItemStack(blockTesla);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		LOGGER.fine("Languages Loaded:" + loadLanguages(LANGUAGE_DIRECTORY, LANGUAGES));

		metadata.modId = ID;
		metadata.name = NAME;
		metadata.description = "Resonant Induction is a Minecraft mod focusing on the manipulation of electricity and wireless technology. Ever wanted blazing electrical shocks flying off your evil lairs? You've came to the right place!";
		metadata.url = "http://universalelectricity.com";
		metadata.version = VERSION + BUILD_VERSION;
		metadata.authorList = Arrays.asList(new String[] { "Calclavia", "Aidancbrady" });
		metadata.logoFile = "/";
		metadata.credits = "Thanks to Archadia for the assets.";
		metadata.autogenerated = true;
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		/**
		 * Recipes
		 */
		/** Capacitor **/
		GameRegistry.addRecipe(new ShapedOreRecipe(itemCapacitor, "RRR", "RIR", "RRR", 'R', Item.redstone, 'I', Item.ingotIron));

		/** Linker **/
		GameRegistry.addRecipe(new ShapedOreRecipe(itemLinker, " E ", "GCG", " E ", 'E', Item.eyeOfEnder, 'C', itemCapacitor, 'G', Item.ingotGold));

		/** Quantum Entangler **/
		GameRegistry.addRecipe(new ShapedOreRecipe(itemQuantumEntangler, "EEE", "ILI", "EEE", 'E', Item.eyeOfEnder, 'L', itemLinker, 'I', Item.ingotIron));

		/** Tesla - by Jyzarc */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockTesla, "EEE", " C ", " I ", 'E', Item.eyeOfEnder, 'C', itemCapacitor, 'I', Block.blockIron));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockMultimeter, "RRR", "ICI", "III", 'R', Item.redstone, 'C', itemCapacitor, 'I', Item.ingotIron));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockBattery, "III", "IRI", "III", 'R', Block.blockRedstone, 'I', Item.ingotIron));

		/** EM Contractor */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockEMContractor, " I ", "GCG", "WWW", 'W', Block.wood, 'C', itemCapacitor, 'G', Item.ingotGold, 'I', Item.ingotIron));
	}

	public static int loadLanguages(String languagePath, String[] languageSupported)
	{
		int loaded = 0;

		for (String language : languageSupported)
		{
			LanguageRegistry.instance().loadLocalization(languagePath + language + ".properties", language, false);
			loaded++;
		}

		return loaded;
	}
}
