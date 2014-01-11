package mffs;

import java.io.File;

import mffs.api.Blacklist;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import calclavia.lib.content.IDManager;
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
	public static float FORTRON_PRODUCTION_MULTIPLIER = 1;
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

	public static void load()
	{
		CONFIGURATION.load();

		ENABLE_MANIPULATOR = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Enable Force Manipulator", ENABLE_MANIPULATOR).getBoolean(ENABLE_MANIPULATOR);

		FORTRON_PRODUCTION_MULTIPLIER = (float) CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Fortron Production Multiplier", FORTRON_PRODUCTION_MULTIPLIER).getDouble(FORTRON_PRODUCTION_MULTIPLIER);

		Property propFieldScale = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Max Force Field Scale", MAX_FORCE_FIELD_SCALE);
		MAX_FORCE_FIELD_SCALE = propFieldScale.getInt(MAX_FORCE_FIELD_SCALE);

		Property propInterdiction = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Interdiction Murder Fortron Consumption", INTERDICTION_MURDER_ENERGY);
		INTERDICTION_MURDER_ENERGY = propInterdiction.getInt(INTERDICTION_MURDER_ENERGY);

		Property propCreative = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Effect Creative Players", INTERACT_CREATIVE);
		propCreative.comment = "Should the interdiction matrix interact with creative players?.";
		INTERACT_CREATIVE = propCreative.getBoolean(INTERACT_CREATIVE);

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

		Property interdictionRange = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Field Calculation Per Tick", INTERDICTION_MAX_RANGE);
		interdictionRange.comment = "The maximum range for the interdiction matrix.";
		INTERDICTION_MAX_RANGE = interdictionRange.getInt(INTERDICTION_MAX_RANGE);

		Property useElectricity = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Require Electricity?", ENABLE_ELECTRICITY);
		useElectricity.comment = "Turning this to false will make MFFS run without electricity or energy systems required. Great for vanilla!";
		ENABLE_ELECTRICITY = useElectricity.getBoolean(ENABLE_ELECTRICITY);

		Property conservePackets = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Conserve Packets?", CONSERVE_PACKETS);
		conservePackets.comment = "Turning this to false will enable better client side packet and updates but in the cost of more packets sent.";
		CONSERVE_PACKETS = conservePackets.getBoolean(CONSERVE_PACKETS);

		Property highGraphics = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "High Graphics", HIGH_GRAPHICS);
		highGraphics.comment = "Turning this to false will reduce rendering and client side packet graphical packets.";
		CONSERVE_PACKETS = highGraphics.getBoolean(HIGH_GRAPHICS);

		Property forceManipulatorBlacklist = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Force Manipulator Blacklist", "");
		highGraphics.comment = "Put a list of block IDs to be not-moved by the force manipulator. Separate by commas, no space.";
		String blackListString = forceManipulatorBlacklist.getString();

		if (blackListString != null)
		{
			for (String blockIDString : blackListString.split(","))
			{
				if (blockIDString != null && !blockIDString.isEmpty())
				{
					try
					{
						int blockID = Integer.parseInt(blockIDString);
						Blacklist.forceManipulationBlacklist.add(blockID);
					}
					catch (Exception e)
					{
						ModularForceFieldSystem.LOGGER.severe("Invalid block blacklist ID!");
						e.printStackTrace();
					}
				}
			}
		}

		Property blacklist1 = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Stabilization Blacklist", "");
		String blackListString1 = blacklist1.getString();

		if (blackListString1 != null)
		{
			for (String blockIDString : blackListString1.split(","))
			{
				if (blockIDString != null && !blockIDString.isEmpty())
				{
					try
					{
						int blockID = Integer.parseInt(blockIDString);
						Blacklist.stabilizationBlacklist.add(blockID);
					}
					catch (Exception e)
					{
						ModularForceFieldSystem.LOGGER.severe("Invalid block blacklist ID!");
						e.printStackTrace();
					}
				}
			}
		}

		Property blacklist2 = CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "Disintegration Blacklist", "");
		String blackListString2 = blacklist1.getString();

		if (blackListString2 != null)
		{
			for (String blockIDString : blackListString2.split(","))
			{
				if (blockIDString != null && !blockIDString.isEmpty())
				{
					try
					{
						int blockID = Integer.parseInt(blockIDString);
						Blacklist.disintegrationBlacklist.add(blockID);
					}
					catch (Exception e)
					{
						ModularForceFieldSystem.LOGGER.severe("Invalid block blacklist ID!");
						e.printStackTrace();
					}
				}
			}
		}

		CONFIGURATION.save();
	}
}