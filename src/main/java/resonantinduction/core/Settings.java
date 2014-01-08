package resonantinduction.core;

import java.io.File;

import net.minecraftforge.common.Configuration;
import resonantinduction.Reference;
import resonantinduction.transport.levitator.TileEMLevitator;
import cpw.mods.fml.common.Loader;

/**
 * @author Calclavia
 * 
 */
public class Settings
{
	/**
	 * Auto ID Management
	 */
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

	/**
	 * Settings
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), Reference.NAME + ".cfg"));
	public static int FURNACE_WATTAGE = 50000;
	public static boolean SOUND_FXS = true;
	public static boolean LO_FI_INSULATION = false;
	public static boolean SHINY_SILVER = true;
	public static boolean REPLACE_FURNACE = true;

	public static void init()
	{
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
		CONFIGURATION.save();
	}
}
