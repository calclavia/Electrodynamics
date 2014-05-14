package resonantinduction.core;

import java.io.File;
import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import resonant.api.recipe.QuantumAssemblerRecipes;
import resonant.lib.config.Config;
import resonant.lib.content.IDManager;
import resonant.lib.prefab.poison.PotionRadiation;
import resonant.lib.utility.LanguageUtility;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModMetadata;

/** @author Calclavia */
public class Settings
{
    public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), Reference.NAME + ".cfg"));
    public static final String DOMAIN = "resonantinduction";
	
    /** IDs suggested by Jyzarc and Horfius */
    public static final IDManager idManager;

    static
    {
        CONFIGURATION.load();
        
        idManager = new IDManager(CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "BlockIDPrefix", 1200).getInt(1200), CONFIGURATION.get(Configuration.CATEGORY_GENERAL, "ItemIDPrefix", 20150).getInt(20150));
        
        CONFIGURATION.save();
    }
    
    /** Config Options */
    public static void load()
    {
        for (int recipeID : quantumAssemblerRecipes)
        {
            try
            {
                QuantumAssemblerRecipes.addRecipe(new ItemStack(recipeID, 1, 0));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // Calling this once to prevent the static class from not initiating.
        PotionRadiation.INSTANCE.getId();
    }

	public static int getNextBlockID()
    {
        return idManager.getNextBlockID();
    }

    public static int getNextBlockID(String key)
    {
        int id = idManager.getNextBlockID();
        return Settings.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, key, id).getInt(id);
    }

    public static int getNextItemID()
    {
        return idManager.getNextItemID();
    }

    public static int getNextItemID(String key)
    {
        int id = idManager.getNextItemID();
        return Settings.CONFIGURATION.get(Configuration.CATEGORY_ITEM, key, id).getInt(id);
    }

    @Config(key = "Engineering Table Autocraft")
    public static boolean ALLOW_ENGINEERING_AUTOCRAFT = true;
    
    @Config(key = "Tesla Sound FXs")
    public static boolean SOUND_FXS = true;
    
    @Config(key = "Shiny silver Wires")
    public static boolean SHINY_SILVER = true;
    
    @Config(key = "Max EM Contractor Path")
    public static int MAX_LEVITATOR_DISTANCE = 200;
    
    @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Max Reach")
    public static int LEVITATOR_MAX_REACH = 40;
    
    @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Push Delay")
    public static int LEVITATOR_PUSH_DELAY = 5;
    
    @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Max Speed")
    public static double LEVITATOR_MAX_SPEED = .2;
    
    @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Acceleration")
    public static double LEVITATOR_ACCELERATION = .02;   
    
    @Config(category = "Power", key = "Wind_tubine_Ratio")
    public static int WIND_POWER_RATIO = 1;
    
    @Config(category = "Power", key = "Water_tubine_Ratio")
    public static int WATER_POWER_RATIO = 1;
    
    @Config(category = "Power", key = "Solor_Panel")
    public static int SOLAR_ENERGY = 50;
    
    @Config
    public static double fulminationOutputMultiplier = 1;
    
    @Config
    public static double turbineOutputMultiplier = 1;
    
    @Config
    public static double fissionBoilVolumeMultiplier = 1;
    
    @Config
    public static boolean allowTurbineStacking = true;
    
    @Config
    public static boolean allowToxicWaste = true;
    
    @Config
    public static boolean allowRadioactiveOres = true;
    
    @Config
    public static boolean allowOreDictionaryCompatibility = true;
    
    @Config
    public static boolean allowAlternateRecipes = true;
    
    @Config
    public static boolean allowIC2UraniumCompression = true;
    
    @Config(comment = "0 = Do not generate, 1 = Generate items only, 2 = Generate all")
    public static int quantumAssemblerGenerateMode = 1;
    
    @Config
    public static int uraniumHexaflourideRatio = 200;
    
    @Config
    public static int waterPerDeutermium = 4;
    
    @Config
    public static int deutermiumPerTritium = 4;
    
    @Config(comment = "Put a list of block/item IDs to be used by the Quantum Assembler. Separate by commas, no space.")
    public static int[] quantumAssemblerRecipes = new int[0];
    
    @Config
    public static double darkMatterSpawnChance = 0.2;

    public static void setModMetadata(ModMetadata metadata, String id, String name)
    {
        setModMetadata(metadata, id, name, "");
    }

    public static void setModMetadata(ModMetadata metadata, String id, String name, String parent)
    {
        metadata.modId = id;
        metadata.name = name;
        metadata.description = LanguageUtility.getLocal("meta.resonantinduction.description");
        metadata.url = "http://calclavia.com/resonant-induction";
        metadata.logoFile = "ri_logo.png";
        metadata.version = Reference.VERSION + "." + Reference.BUILD_VERSION;
        metadata.authorList = Arrays.asList(new String[] { "Calclavia", "DarkCow", "Maxwolf Goodliffe" });
        metadata.credits = LanguageUtility.getLocal("meta.resonantinduction.credits");
        metadata.parent = parent;
        metadata.autogenerated = false;
    }

    public static boolean isOp(String username)
    {
        MinecraftServer theServer = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (theServer != null)
        {
            return theServer.getConfigurationManager().getOps().contains(username.trim().toLowerCase());
        }

        return false;
    }
}
