package mffs;

import java.io.File;

import mffs.api.ForceManipulatorBlacklist;
import net.minecraft.block.Block;
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
	public static int MAX_FORCE_FIELD_SCALE = 200;
	public static boolean INTERACT_CREATIVE = true;
	public static boolean LOAD_CHUNKS = true;
	public static boolean OP_OVERRIDE = true;
	public static boolean USE_CACHE = true;
	public static boolean ENABLE_ELECTRICITY = true;
	public static boolean CONSERVE_PACKETS = true;
	public static boolean HIGH_GRAPHICS = true;
	public static int INTERDICTION_MURDER_ENERGY = 0;
	public static final int MAX_FREQUENCY_DIGITS = 6;

	public static void load()
	{
		CONFIGURATION.load();
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
				if (blockIDString != null)
				{
					try
					{
						int blockID = Integer.parseInt(blockIDString);
						ForceManipulatorBlacklist.blackList.add(Block.blocksList[blockID]);
					}
					catch (Exception e)
					{
						ModularForceFieldSystem.LOGGER.severe("Invalid block ID!");
						e.printStackTrace();
					}
				}
			}
		}

		CONFIGURATION.save();
	}
}