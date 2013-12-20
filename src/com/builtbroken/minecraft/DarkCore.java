package com.builtbroken.minecraft;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.UniversalElectricity;

import com.builtbroken.minecraft.fluid.FluidHelper;
import com.builtbroken.minecraft.helpers.PlayerKeyHandler;
import com.builtbroken.minecraft.prefab.BlockMulti;
import com.builtbroken.minecraft.save.SaveManager;
import com.builtbroken.minecraft.tilenetwork.prefab.NetworkUpdateHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class DarkCore
{
    private static DarkCore instance;

    private boolean pre, load, post;

    public static final String DOMAIN = "darkcore";
    public static final String PREFIX = DOMAIN + ":";

    public static final String TEXTURE_DIRECTORY = "textures/";
    public static final String BLOCK_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
    public static final String ITEM_DIRECTORY = TEXTURE_DIRECTORY + "items/";
    public static final String MODEL_DIRECTORY = TEXTURE_DIRECTORY + "models/";
    public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
    public static final String CHANNEL = "DARKCORE";

    public static String DIRECTORY_NO_SLASH = "assets/" + DOMAIN + "/";
    public static String DIRECTORY = "/" + DIRECTORY_NO_SLASH;
    public static String LANGUAGE_PATH = DIRECTORY + "languages/";
    public static String SOUND_PATH = DIRECTORY + "audio/";

    public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "Dark/Main.cfg"));

    /* START IDS */
    public static int BLOCK_ID_PRE = 3100;
    public static int ITEM_ID_PREFIX = 13200;

    public static BlockMulti multiBlock = null;

    public static DarkCore instance()
    {
        if (instance == null)
        {
            instance = new DarkCore();
        }
        return instance;
    }

    public static void requestMultiBlock(String modID)
    {
        if (multiBlock == null)
        {
            Block b = CoreRegistry.createNewBlock("DMBlockMulti", modID, BlockMulti.class, false);
            if (b instanceof BlockMulti)
            {
                multiBlock = (BlockMulti) b;
            }
        }
    }

    public void preLoad()
    {
        if (!pre)
        {
            MinecraftForge.EVENT_BUS.register(new FluidHelper());
            MinecraftForge.EVENT_BUS.register(SaveManager.instance());
            TickRegistry.registerTickHandler(NetworkUpdateHandler.instance(), Side.SERVER);
            TickRegistry.registerScheduledTickHandler(new PlayerKeyHandler(), Side.CLIENT);
            MinecraftForge.EVENT_BUS.register(new LaserEntityDamageSource(null));
            UniversalElectricity.initiate();
            Compatibility.initiate();
            pre = true;
        }
    }

    public void Load()
    {
        if (!load)
        {
            CoreRegistry.masterBlockConfig.load();
            load = true;
        }
    }

    public void postLoad()
    {
        if (!post)
        {
            CoreRegistry.masterBlockConfig.save();
            post = true;
        }
    }

    /** Gets the next unused ID in the block list. Does not prevent config file issues after the file
     * has been made */
    public static int getNextID()
    {
        int id = BLOCK_ID_PRE;

        while (id > 255 && id < (Block.blocksList.length - 1))
        {
            Block block = Block.blocksList[id];
            if (block == null)
            {
                break;
            }
            id++;
        }
        BLOCK_ID_PRE = id + 1;
        return id;
    }

    /** Gets the next unused ID in the item list. Does not prevent config file issues after the file
     * has been made */
    public static int getNextItemId()
    {
        int id = ITEM_ID_PREFIX;

        while (id > 255 && id < (Item.itemsList.length - 1))
        {
            Item item = Item.itemsList[id];
            if (item == null)
            {
                break;
            }
            id++;
        }
        ITEM_ID_PREFIX = id + 1;
        return id;
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
