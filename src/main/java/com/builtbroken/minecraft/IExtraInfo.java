package com.builtbroken.minecraft;

import java.util.List;
import java.util.Set;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Used to handle info about the block that would normally be handled by the mod main class. Use the
 * BlockRegistry in order for these methods to be called on load of the mod.
 *
 * @author DarkGuardsman */
public interface IExtraInfo
{

    /** Does this object need to generate a settings config */
    public boolean hasExtraConfigs();

    /** Loads the config file for this block. This is a single config file that is tied to just this
     * block alone. Anything can be stored in the config file but its suggested to use it for
     * advanced settings for the block/tile. Things like power, update rate, optional features,
     * graphics, or crafting cost */
    public void loadExtraConfigs(Configuration config);

    public static interface IExtraBlockInfo extends IExtraInfo, ITileEntityProvider
    {

        /** Loads the names used to reference this item in a recipe */
        public void loadOreNames();

        /** List of all tileEntities this block needs */
        public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list);

        @SideOnly(Side.CLIENT)
        public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list);
    }

    public static interface IExtraTileEntityInfo extends IExtraInfo
    {

    }

    public static interface IExtraItemInfo extends IExtraInfo
    {
        /** Loads the names used to reference this item in a recipe */
        public void loadOreNames();
    }

}
