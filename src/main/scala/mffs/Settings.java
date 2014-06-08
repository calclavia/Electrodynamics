package mffs;

import java.io.File;

import resonant.lib.config.Config;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import calclavia.api.mffs.Blacklist;
import resonant.lib.content.IDManager;
import resonant.lib.utility.LanguageUtility;
import cpw.mods.fml.common.Loader;

/**
 * Settings include all configuration for MFFS.
 * 
 * @author Calclavia
 * 
 */
public class Settings
{
	public static final Configuration configuration = new Configuration(new File(Loader.instance().getConfigDir(), ModularForceFieldSystem.NAME + ".cfg"));

	/**
	 * Auto-incrementing configuration IDs. Use this to make sure no config ID is the same.
	 */
	public static final IDManager idManager = new IDManager(1680, 11130);

	public static int getNextBlockID()
	{
		return idManager.getNextBlockID();
	}

	public static int getNextItemID()
	{
		return idManager.getNextItemID();
	}

	/**
	 * MFFS Configuration Settings
	 */
	public static int MAX_FORCE_FIELDS_PER_TICK = 1000;
	public static int MAX_FORCE_FIELD_SCALE = 200;
	public static double FORTRON_PRODUCTION_MULTIPLIER = 1;
	public static boolean INTERACT_CREATIVE = true;
	public static boolean LOAD_CHUNKS = true;
	public static boolean OP_OVERRIDE = true;
	public static boolean USE_CACHE = true;
	public static boolean ENABLE_ELECTRICITY = true;
	public static boolean CONSERVE_PACKETS = true;
	public static boolean HIGH_GRAPHICS = true;
	public static int INTERDICTION_MURDER_ENERGY = 0;
	public static int INTERDICTION_MAX_RANGE = Integer.MAX_VALUE;
	public static final int MAX_FREQUENCY_DIGITS = 6;
	public static boolean ENABLE_MANIPULATOR = true;
	@Config
	public static boolean allowForceManipulatorTeleport = true;

	public static void load()
	{
		configuration.load();

		ENABLE_MANIPULATOR = configuration.get(Configuration.CATEGORY_GENERAL, "Enable Force Manipulator", ENABLE_MANIPULATOR).getBoolean(ENABLE_MANIPULATOR);

		FORTRON_PRODUCTION_MULTIPLIER = (double) configuration.get(Configuration.CATEGORY_GENERAL, "Fortron Production Multiplier", FORTRON_PRODUCTION_MULTIPLIER).getDouble(FORTRON_PRODUCTION_MULTIPLIER);

		Property propFieldScale = configuration.get(Configuration.CATEGORY_GENERAL, "Max Force Field Scale", MAX_FORCE_FIELD_SCALE);
		MAX_FORCE_FIELD_SCALE = propFieldScale.getInt(MAX_FORCE_FIELD_SCALE);

		Property propInterdiction = configuration.get(Configuration.CATEGORY_GENERAL, "Interdiction Murder Fortron Consumption", INTERDICTION_MURDER_ENERGY);
		INTERDICTION_MURDER_ENERGY = propInterdiction.getInt(INTERDICTION_MURDER_ENERGY);

		Property propCreative = configuration.get(Configuration.CATEGORY_GENERAL, "Effect Creative Players", INTERACT_CREATIVE);
		propCreative.comment = "Should the interdiction matrix interact with creative players?.";
		INTERACT_CREATIVE = propCreative.getBoolean(INTERACT_CREATIVE);

		Property propChunkLoading = configuration.get(Configuration.CATEGORY_GENERAL, "Load Chunks", LOAD_CHUNKS);
		propChunkLoading.comment = "Set this to false to turn off the MFFS Chunkloading capabilities.";
		LOAD_CHUNKS = propChunkLoading.getBoolean(LOAD_CHUNKS);

		Property propOpOverride = configuration.get(Configuration.CATEGORY_GENERAL, "Op Override", OP_OVERRIDE);
		propOpOverride.comment = "Allow the operator(s) to override security measures created by MFFS?";
		OP_OVERRIDE = propOpOverride.getBoolean(OP_OVERRIDE);

		Property propUseCache = configuration.get(Configuration.CATEGORY_GENERAL, "Use Cache", USE_CACHE);
		propUseCache.comment = "Cache allows temporary data saving to decrease calculations required.";
		USE_CACHE = propUseCache.getBoolean(USE_CACHE);

		Property maxFFGenPerTick = configuration.get(Configuration.CATEGORY_GENERAL, "Field Calculation Per Tick", MAX_FORCE_FIELDS_PER_TICK);
		maxFFGenPerTick.comment = "How many force field blocks can be generated per tick? Less reduces lag.";
		MAX_FORCE_FIELDS_PER_TICK = maxFFGenPerTick.getInt(MAX_FORCE_FIELDS_PER_TICK);

		Property interdictionRange = configuration.get(Configuration.CATEGORY_GENERAL, "Field Calculation Per Tick", INTERDICTION_MAX_RANGE);
		interdictionRange.comment = "The maximum range for the interdiction matrix.";
		INTERDICTION_MAX_RANGE = interdictionRange.getInt(INTERDICTION_MAX_RANGE);

		Property useElectricity = configuration.get(Configuration.CATEGORY_GENERAL, "Require Electricity?", ENABLE_ELECTRICITY);
		useElectricity.comment = "Turning this to false will make MFFS run without electricity or energy systems required. Great for vanilla!";
		ENABLE_ELECTRICITY = useElectricity.getBoolean(ENABLE_ELECTRICITY);

		Property conservePackets = configuration.get(Configuration.CATEGORY_GENERAL, "Conserve Packets?", CONSERVE_PACKETS);
		conservePackets.comment = "Turning this to false will enable better client side packet and updates but in the cost of more packets sent.";
		CONSERVE_PACKETS = conservePackets.getBoolean(CONSERVE_PACKETS);

		Property highGraphics = configuration.get(Configuration.CATEGORY_GENERAL, "High Graphics", HIGH_GRAPHICS);
		highGraphics.comment = "Turning this to false will reduce rendering and client side packet graphical packets.";
		CONSERVE_PACKETS = highGraphics.getBoolean(HIGH_GRAPHICS);

		Property forceManipulatorBlacklist = configuration.get(Configuration.CATEGORY_GENERAL, "Force Manipulator Blacklist", "");
		forceManipulatorBlacklist.comment = "Put a list of block IDs to be not-moved by the force manipulator. Separate by commas, no space.";
		String blackListManipulate = forceManipulatorBlacklist.getString();
		Blacklist.forceManipulationBlacklist.addAll(LanguageUtility.decodeIDSplitByComma(blackListManipulate));

		Property blacklist1 = configuration.get(Configuration.CATEGORY_GENERAL, "Stabilization Blacklist", "");
		String blackListStabilize = blacklist1.getString();
		Blacklist.stabilizationBlacklist.addAll(LanguageUtility.decodeIDSplitByComma(blackListStabilize));

		Property blacklist2 = configuration.get(Configuration.CATEGORY_GENERAL, "Disintegration Blacklist", "");
		String blackListDisintegrate = blacklist1.getString();
		Blacklist.disintegrationBlacklist.addAll(LanguageUtility.decodeIDSplitByComma(blackListDisintegrate));

		configuration.save();
	}
}