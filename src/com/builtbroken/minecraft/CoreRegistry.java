package com.builtbroken.minecraft;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.IExtraInfo.IExtraBlockInfo;
import com.builtbroken.minecraft.IExtraInfo.IExtraItemInfo;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

/** Handler to make registering all parts of a mod's objects that are loaded into the game by forge
 * 
 * @author DarkGuardsman */
public class CoreRegistry
{
    public static HashMap<Block, String> registredBlocks = new HashMap<Block, String>();
    public static HashMap<Item, String> registredItems = new HashMap<Item, String>();

    @SidedProxy(clientSide = "com.builtbroken.minecraft.ClientRegistryProxy", serverSide = "com.builtbroken.minecraft.RegistryProxy")
    public static RegistryProxy prox;

    public static RegistryProxy proxy()
    {
        if (prox == null)
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                prox = new ClientRegistryProxy();
            }
            else
            {
                prox = new RegistryProxy();
            }
        }
        return prox;
    }

    public static Configuration masterBlockConfig = new Configuration(new File(Loader.instance().getConfigDir(), "objects/EnabledBlocks.cfg"));

    /** Generates a block using reflection, and runs it threw config checks
     * 
     * @param name - name to register the block with
     * @param modID - mod id to register the block to
     * @param blockClass - class to generate the instance from */
    public static Block createNewBlock(String name, String modID, Class<? extends Block> blockClass)
    {
        return CoreRegistry.createNewBlock(name, modID, blockClass, true);
    }

    /** Generates a block using reflection, and runs it threw config checks
     * 
     * @param name - name to register the block with
     * @param modID - mod id to register the block to
     * @param blockClass - class to generate the instance from
     * @param canDisable - should we allow the player the option to disable the block */
    public static Block createNewBlock(String name, String modID, Class<? extends Block> blockClass, boolean canDisable)
    {
        return CoreRegistry.createNewBlock(name, modID, blockClass, null, canDisable);
    }

    /** Generates a block using reflection, and runs it threw config checks
     * 
     * @param name - name to register the block with
     * @param modID - mod id to register the block to
     * @param blockClass - class to generate the instance from
     * @param itemClass - item block to register with the block */
    public static Block createNewBlock(String name, String modID, Class<? extends Block> blockClass, Class<? extends ItemBlock> itemClass)
    {
        return createNewBlock(name, modID, blockClass, itemClass, true);
    }

    /** Generates a block using reflection, and runs it threw config checks
     * 
     * @param name - name to register the block with
     * @param modID - mod id to register the block to
     * @param blockClass - class to generate the instance from
     * @param canDisable - should we allow the player the option to disable the block
     * @param itemClass - item block to register with the block */
    public static Block createNewBlock(String name, String modID, Class<? extends Block> blockClass, Class<? extends ItemBlock> itemClass, boolean canDisable)
    {
        Block block = null;
        if (blockClass != null && (!canDisable || canDisable && masterBlockConfig.get("Enabled_List", "Enabled_" + name, true).getBoolean(true)))
        {
            //TODO redesign to catch blockID conflict
            try
            {
                block = blockClass.newInstance();
            }
            catch (IllegalArgumentException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                System.out.println("\n\nWarning: Block [" + name + "] failed to be created\n");
                e.printStackTrace();
                System.out.println("\n\n");
            }
            if (block != null)
            {
                registredBlocks.put(block, name);
                proxy().registerBlock(block, itemClass, name, modID);
                CoreRegistry.finishCreation(block);
            }
        }
        return block;
    }

    /** Finishes the creation of the block loading config files and tile entities */
    public static void finishCreation(Block block)
    {
        if (block instanceof IExtraInfo)
        {
            if (((IExtraInfo) block).hasExtraConfigs())
            {
                Configuration extraBlockConfig = new Configuration(new File(Loader.instance().getConfigDir(), "objects/blocks/" + block.getUnlocalizedName() + ".cfg"));
                extraBlockConfig.load();
                ((IExtraInfo) block).loadExtraConfigs(extraBlockConfig);
                extraBlockConfig.save();
            }
            if (block instanceof IExtraBlockInfo)
            {
                ((IExtraBlockInfo) block).loadOreNames();
                Set<Pair<String, Class<? extends TileEntity>>> tileListNew = new HashSet<Pair<String, Class<? extends TileEntity>>>();
                ((IExtraBlockInfo) block).getTileEntities(block.blockID, tileListNew);
                for (Pair<String, Class<? extends TileEntity>> par : tileListNew)
                {
                    proxy().regiserTileEntity(par.left(), par.right());
                }
            }
        }

    }

    /** Method to get block via name
     * 
     * @param blockName
     * @return Block requested */
    public static Block getBlock(String blockName)
    {
        for (Entry<Block, String> entry : registredBlocks.entrySet())
        {
            String name = entry.getKey().getUnlocalizedName().replace("tile.", "");
            if (name.equalsIgnoreCase(blockName))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Creates a new fluid block using the prefab following a few conditions.
     * 
     * @param modDomainPrefix - prefix of the mod, used for texture refrence and block registry
     * @param config - config file to pull the blockID from
     * @param fluid - fluid to link to this block */
    public static Block createNewFluidBlock(String modDomainPrefix, Configuration config, Fluid fluid)
    {
        Block fluidBlock = null;
        Fluid fluidActual = null;
        if (config != null && fluid != null && config.get("general", "EnableFluid_" + fluid.getName(), true).getBoolean(true) && FluidRegistry.getFluid(fluid.getName()) == null)
        {
            FluidRegistry.registerFluid(fluid);
            fluidActual = FluidRegistry.getFluid(fluid.getName());
            if (fluidActual == null)
            {
                fluidActual = fluid;
            }

            if (fluidActual.getBlockID() == -1 && masterBlockConfig.get("Enabled_List", "Enabled_" + fluid.getName() + "Block", true).getBoolean(true))
            {
                fluidBlock = new BlockFluid(modDomainPrefix, fluidActual, config).setUnlocalizedName("tile.Fluid." + fluid.getName());
                proxy().registerBlock(fluidBlock, null, "DMBlockFluid" + fluid.getName(), modDomainPrefix);
            }
            else
            {
                fluidBlock = Block.blocksList[fluid.getBlockID()];
            }
        }

        return fluidBlock;
    }

    /** Creates a new item using reflection as well runs it threw some check to activate any
     * interface methods
     * 
     * @param name - name to register the item with
     * @param modid - mods that the item comes from
     * @param clazz - item class
     * @param canDisable - can a user disable this item
     * @return the new item */
    public static Item createNewItem(String name, String modid, Class<? extends Item> clazz, boolean canDisable)
    {
        Item item = null;
        if (clazz != null && (!canDisable || canDisable && masterBlockConfig.get("Enabled_List", "Enabled_" + name, true).getBoolean(true)))
        {
            try
            {
                item = clazz.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (item != null)
            {
                registredItems.put(item, name);
                GameRegistry.registerItem(item, name, modid);
                if (item instanceof IExtraInfo)
                {
                    if (((IExtraInfo) item).hasExtraConfigs())
                    {
                        Configuration extraBlockConfig = new Configuration(new File(Loader.instance().getConfigDir(), "objects/items/" + item.getUnlocalizedName() + ".cfg"));
                        extraBlockConfig.load();
                        ((IExtraInfo) item).loadExtraConfigs(extraBlockConfig);
                        extraBlockConfig.save();
                    }
                    if (item instanceof IExtraItemInfo)
                    {
                        ((IExtraItemInfo) item).loadOreNames();
                    }
                }
            }
        }
        return item;
    }
}
