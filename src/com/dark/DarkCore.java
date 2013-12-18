package com.dark;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class DarkCore
{
    private static DarkCore instance;

    public static final String TEXTURE_DIRECTORY = "textures/";
    public static final String BLOCK_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
    public static final String ITEM_DIRECTORY = TEXTURE_DIRECTORY + "items/";
    public static final String MODEL_DIRECTORY = TEXTURE_DIRECTORY + "models/";
    public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";

    /* START IDS */
    public static int BLOCK_ID_PRE = 3100;
    public static int ITEM_ID_PREFIX = 13200;

    public static DarkCore instance()
    {
        if (instance == null)
        {
            instance = new DarkCore();
        }
        return instance;
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
}
