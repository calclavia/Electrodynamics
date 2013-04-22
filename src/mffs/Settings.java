package mffs;

import java.io.File;

import universalelectricity.core.UniversalElectricity;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.Loader;

/**
 * Settings include all configuration for MFFS.
 * 
 * @author Calclavia
 * 
 */
public class Settings
{
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), ModularForceFieldSystem.NAME + ".cfg"));

	/**
	 * Auto-incrementing configuration IDs. Use this to make sure no config ID is the same.
	 */
	public static final int BLOCK_ID_PREFIX = 1680;
	public static final int ITEM_ID_PREFIX = 11130;

	private static int NEXT_BLOCK_ID = BLOCK_ID_PREFIX;
	private static int NEXT_ITEM_ID = ITEM_ID_PREFIX;

	public static int getNextBlockID()
	{
		NEXT_BLOCK_ID++;
		return NEXT_BLOCK_ID;
	}

	public static int getNextItemID()
	{
		NEXT_ITEM_ID++;
		return NEXT_ITEM_ID;
	}

	/**
	 * MFFS Configuration Settings
	 */
	public static int MAX_FORCE_FIELDS_PER_TICK = 1000000;
	public static int MAX_FORCE_FIELD_SCALE = 150;
	public static boolean LOAD_CHUNKS = true;
	public static boolean OP_OVERRIDE = true;
	public static boolean USE_CACHE = true;
	public static boolean ENABLE_ELECTRICITY = true;

	public static void load()
	{
		CONFIGURATION.load();
		Property propFieldScale = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Max Force Field Scale", MAX_FORCE_FIELD_SCALE);
		MAX_FORCE_FIELD_SCALE = propFieldScale.getInt(MAX_FORCE_FIELD_SCALE);

		Property propChunkLoading = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Load Chunks", LOAD_CHUNKS);
		propChunkLoading.comment = "Set this to false to turn off the MFFS Chunkloading capabilities.";
		LOAD_CHUNKS = propChunkLoading.getBoolean(LOAD_CHUNKS);

		Property propOpOverride = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Op Override", OP_OVERRIDE);
		propOpOverride.comment = "Allow the operator(s) to override security measures created by MFFS?";
		OP_OVERRIDE = propOpOverride.getBoolean(OP_OVERRIDE);

		Property propUseCache = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Use Cache", USE_CACHE);
		propUseCache.comment = "Cache allows temporary data saving to decrease calculations required.";
		USE_CACHE = propUseCache.getBoolean(USE_CACHE);

		Property maxFFGenPerTick = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Field Calculation Per Tick", MAX_FORCE_FIELDS_PER_TICK);
		maxFFGenPerTick.comment = "How many force field blocks can be generated per tick? Less reduces lag.";
		MAX_FORCE_FIELDS_PER_TICK = maxFFGenPerTick.getInt(MAX_FORCE_FIELDS_PER_TICK);

		if (!UniversalElectricity.isNetworkActive && !Loader.isModLoaded("IC2") && !Loader.isModLoaded("BuildCraft|Core"))
		{
			ENABLE_ELECTRICITY = false;
		}

		Property useElectricity = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Use Electricity?", ENABLE_ELECTRICITY);
		useElectricity.comment = "Turning this to false will make MFFS run without electricity or energy systems required. Great for vanilla!";
		ENABLE_ELECTRICITY = useElectricity.getBoolean(ENABLE_ELECTRICITY);
		CONFIGURATION.save();
	}
}