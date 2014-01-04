package resonantinduction;

import ic2.api.item.Items;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import resonantinduction.machine.crusher.ItemDust;
import resonantinduction.machine.furnace.BlockAdvancedFurnace;
import resonantinduction.machine.furnace.TileAdvancedFurnace;
import resonantinduction.multimeter.ItemMultimeter;
import resonantinduction.transport.battery.BlockBattery;
import resonantinduction.transport.battery.ItemBlockBattery;
import resonantinduction.transport.battery.TileBattery;
import resonantinduction.transport.levitator.BlockEMContractor;
import resonantinduction.transport.levitator.ItemBlockContractor;
import resonantinduction.transport.levitator.ItemLinker;
import resonantinduction.transport.levitator.TileEMLevitator;
import resonantinduction.transport.tesla.BlockTesla;
import resonantinduction.transport.tesla.TileTesla;
import resonantinduction.transport.transformer.ItemTransformer;
import resonantinduction.transport.wire.EnumWireMaterial;
import resonantinduction.transport.wire.ItemWire;
import calclavia.lib.UniversalRecipe;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.network.PacketTile;
import calclavia.lib.prefab.TranslationHelper;
import codechicken.lib.colour.ColourRGBA;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author Calclavia
 * 
 */
@Mod(modid = ResonantInduction.ID, name = ResonantInduction.NAME, version = ResonantInduction.VERSION, dependencies = "required-after:CalclaviaCore;before:ThermalExpansion;before:IC2")
@NetworkMod(channels = ResonantInduction.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
@ModstatInfo(prefix = "resonantin")
public class ResonantInduction
{
	/**
	 * Mod Information
	 */
	public static final String ID = "resonantinduction";
	public static final String NAME = "Resonant Induction";
	public static final String CHANNEL = "RESIND";

	public static final String MAJOR_VERSION = "@MAJOR@";
	public static final String MINOR_VERSION = "@MINOR@";
	public static final String REVISION_VERSION = "@REVIS@";
	public static final String BUILD_VERSION = "@BUILD@";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;

	@Instance(ID)
	public static ResonantInduction INSTANCE;

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
	public static final String MODEL_DIRECTORY = DIRECTORY + "models/";

	public static final String LANGUAGE_DIRECTORY = DIRECTORY + "languages/";
	public static final String[] LANGUAGES = new String[] { "en_US", "de_DE" };

	/**
	 * Settings
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), NAME + ".cfg"));
	public static int FURNACE_WATTAGE = 50000;
	public static boolean SOUND_FXS = true;
	public static boolean LO_FI_INSULATION = false;
	public static boolean SHINY_SILVER = true;
	public static boolean REPLACE_FURNACE = true;

	/** Block ID by Jyzarc */
	private static final int BLOCK_ID_PREFIX = 3200;
	/** Item ID by Horfius */
	private static final int ITEM_ID_PREFIX = 20150;
	public static int MAX_CONTRACTOR_DISTANCE = 200;

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
	/**
	 * Transport
	 */
	public static Item itemLinker;
	private static Item itemPartWire;
	public static Item itemMultimeter;
	public static Item itemTransformer;

	/**
	 * Machines
	 */
	public static Item itemDust;

	// Blocks
	public static Block blockTesla;
	public static Block blockEMContractor;
	public static Block blockBattery;
	public static Block blockAdvancedFurnace;

	/**
	 * Packets
	 */
	public static final PacketTile PACKET_TILE = new PacketTile(CHANNEL);
	public static final PacketMultiPart PACKET_MULTIPART = new PacketMultiPart(CHANNEL);
	public static final ColourRGBA[] DYE_COLORS = new ColourRGBA[] { new ColourRGBA(255, 255, 255, 255), new ColourRGBA(1, 0, 0, 1d), new ColourRGBA(0, 0.608, 0.232, 1d), new ColourRGBA(0.588, 0.294, 0, 1d), new ColourRGBA(0, 0, 1, 1d), new ColourRGBA(0.5, 0, 05, 1d), new ColourRGBA(0, 1, 1, 1d), new ColourRGBA(0.8, 0.8, 0.8, 1d), new ColourRGBA(0.3, 0.3, 0.3, 1d), new ColourRGBA(1, 0.412, 0.706, 1d), new ColourRGBA(0.616, 1, 0, 1d), new ColourRGBA(1, 1, 0, 1d), new ColourRGBA(0.46f, 0.932, 1, 1d), new ColourRGBA(0.5, 0.2, 0.5, 1d), new ColourRGBA(0.7, 0.5, 0.1, 1d), new ColourRGBA(1, 1, 1, 1d) };

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		LOGGER.setParent(FMLLog.getLogger());
		NetworkRegistry.instance().registerGuiHandler(this, ResonantInduction.proxy);
		Modstats.instance().getReporter().registerMod(this);
		CONFIGURATION.load();

		// Config
		FURNACE_WATTAGE = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Furnace Wattage Per Tick", FURNACE_WATTAGE).getInt(FURNACE_WATTAGE);
		SOUND_FXS = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Tesla Sound FXs", SOUND_FXS).getBoolean(SOUND_FXS);
		LO_FI_INSULATION = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Use lo-fi insulation texture", LO_FI_INSULATION).getBoolean(LO_FI_INSULATION);
		SHINY_SILVER = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Shiny silver wires", SHINY_SILVER).getBoolean(SHINY_SILVER);
		MAX_CONTRACTOR_DISTANCE = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Max EM Contractor Path", MAX_CONTRACTOR_DISTANCE).getInt(MAX_CONTRACTOR_DISTANCE);
		REPLACE_FURNACE = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Replace vanilla furnace", REPLACE_FURNACE).getBoolean(REPLACE_FURNACE);

		TileEMLevitator.ACCELERATION = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Item Acceleration", TileEMLevitator.ACCELERATION).getDouble(TileEMLevitator.ACCELERATION);
		TileEMLevitator.MAX_REACH = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Reach", TileEMLevitator.MAX_REACH).getInt(TileEMLevitator.MAX_REACH);
		TileEMLevitator.MAX_SPEED = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Max Item Speed", TileEMLevitator.MAX_SPEED).getDouble(TileEMLevitator.MAX_SPEED);
		TileEMLevitator.PUSH_DELAY = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Contractor Item Push Delay", TileEMLevitator.PUSH_DELAY).getInt(TileEMLevitator.PUSH_DELAY);

		// Items
		itemLinker = new ItemLinker(getNextItemID());
		itemPartWire = new ItemWire(getNextItemID());
		itemMultimeter = new ItemMultimeter(getNextItemID());
		itemTransformer = new ItemTransformer(getNextItemID());
		itemDust = new ItemDust(getNextItemID());

		// Blocks
		blockTesla = new BlockTesla(getNextBlockID());
		blockEMContractor = new BlockEMContractor(getNextBlockID());
		blockBattery = new BlockBattery(getNextBlockID());

		if (REPLACE_FURNACE)
		{
			blockAdvancedFurnace = BlockAdvancedFurnace.createNew(false);
			GameRegistry.registerBlock(blockAdvancedFurnace, "ri_" + blockAdvancedFurnace.getUnlocalizedName());
			GameRegistry.registerTileEntity(TileAdvancedFurnace.class, "ri_" + blockAdvancedFurnace.getUnlocalizedName());
		}

		CONFIGURATION.save();

		GameRegistry.registerItem(itemLinker, itemLinker.getUnlocalizedName());
		GameRegistry.registerItem(itemMultimeter, itemMultimeter.getUnlocalizedName());
		GameRegistry.registerItem(itemTransformer, itemTransformer.getUnlocalizedName());
		GameRegistry.registerItem(itemDust, itemDust.getUnlocalizedName());

		GameRegistry.registerBlock(blockTesla, blockTesla.getUnlocalizedName());
		GameRegistry.registerBlock(blockEMContractor, ItemBlockContractor.class, blockEMContractor.getUnlocalizedName());
		GameRegistry.registerBlock(blockBattery, ItemBlockBattery.class, blockBattery.getUnlocalizedName());

		// Tiles
		GameRegistry.registerTileEntity(TileTesla.class, blockTesla.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEMLevitator.class, blockEMContractor.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileBattery.class, blockBattery.getUnlocalizedName());

		ResonantInduction.proxy.registerRenderers();

		/**
		 * Set reference itemstacks
		 */
		TabRI.ITEMSTACK = new ItemStack(blockBattery);

		for (EnumWireMaterial material : EnumWireMaterial.values())
		{
			material.setWire(itemPartWire);
		}

		MinecraftForge.EVENT_BUS.register(itemDust);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		LOGGER.fine("Languages Loaded:" + TranslationHelper.loadLanguages(LANGUAGE_DIRECTORY, LANGUAGES));
		// TODO localize this
		metadata.modId = ID;
		metadata.name = NAME;
		metadata.description = TranslationHelper.getLocal("meta.resonantinduction.description");
		metadata.url = "http://universalelectricity.com/resonant-induction";
		metadata.logoFile = "/ri_logo.png";
		metadata.version = VERSION + BUILD_VERSION;
		metadata.authorList = Arrays.asList(new String[] { "Calclavia", "Aidancbrady" });
		metadata.credits = TranslationHelper.getLocal("meta.resonantinduction.credits");
		metadata.autogenerated = false;

		MultipartRI.INSTANCE = new MultipartRI();

		UniversalRecipe.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		/**
		 * Recipes
		 */
		final ItemStack defaultWire = EnumWireMaterial.IRON.getWire();

		/** Linker **/
		GameRegistry.addRecipe(new ShapedOreRecipe(itemLinker, " E ", "GCG", " E ", 'E', Item.eyeOfEnder, 'C', UniversalRecipe.BATTERY.get(), 'G', UniversalRecipe.SECONDARY_METAL.get()));

		/** Tesla - by Jyzarc */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockTesla, "WEW", " C ", " I ", 'W', defaultWire, 'E', Item.eyeOfEnder, 'C', UniversalRecipe.BATTERY.get(), 'I', UniversalRecipe.PRIMARY_PLATE.get()));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(itemMultimeter, "WWW", "ICI", 'W', defaultWire, 'C', UniversalRecipe.BATTERY.get(), 'I', UniversalRecipe.PRIMARY_METAL.get()));

		/** Battery */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockBattery, "III", "IRI", "III", 'R', Block.blockRedstone, 'I', UniversalRecipe.PRIMARY_METAL.get()));

		/** EM Contractor */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockEMContractor, " I ", "GCG", "WWW", 'W', UniversalRecipe.PRIMARY_METAL.get(), 'C', UniversalRecipe.BATTERY.get(), 'G', UniversalRecipe.SECONDARY_METAL.get(), 'I', UniversalRecipe.PRIMARY_METAL.get()));

		/** Wires **/
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SILVER.getWire(), "MMM", 'M', "ingotSilver"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", 'M', "ingotSuperconductor"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", "MEM", "MMM", 'M', Item.ingotGold, 'E', Item.eyeOfEnder));

		/** Wire Compatiblity **/
		if (Loader.isModLoaded("IC2"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), Items.getItem("copperCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.TIN.getWire(), Items.getItem("tinCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.IRON.getWire(), Items.getItem("ironCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(), Items.getItem("glassFiberCableItem")));
		}

		if (Loader.isModLoaded("Mekanism"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), "universalCable"));
		}

		/** Auto-gen dusts */
		ItemDust.postInit();
		ResonantInduction.proxy.postInit();

		/** Inject new furnace tile class */
		replaceTileEntity(TileEntityFurnace.class, TileAdvancedFurnace.class);
	}

	public static void replaceTileEntity(Class<? extends TileEntity> findTile, Class<? extends TileEntity> replaceTile)
	{
		try
		{
			Map<String, Class> nameToClassMap = ObfuscationReflectionHelper.getPrivateValue(TileEntity.class, null, "field_" + "70326_a", "nameToClassMap", "a");
			Map<Class, String> classToNameMap = ObfuscationReflectionHelper.getPrivateValue(TileEntity.class, null, "field_" + "70326_b", "classToNameMap", "b");

			String findTileID = classToNameMap.get(findTile);

			if (findTileID != null)
			{
				nameToClassMap.put(findTileID, replaceTile);
				classToNameMap.put(replaceTile, findTileID);
				classToNameMap.remove(findTile);
				LOGGER.fine("Replaced TileEntity: " + findTile);
			}
			else
			{
				LOGGER.severe("Failed to replace TileEntity: " + findTile);
			}
		}
		catch (Exception e)
		{
			LOGGER.severe("Failed to replace TileEntity: " + findTile);
			e.printStackTrace();
		}
	}
}
