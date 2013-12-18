package com.dark;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

import com.builtbroken.common.Pair;
import com.dark.IExtraInfo.IExtraBlockInfo;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientRegistryProxy extends RegistryProxy
{
    @Override
    public void registerBlock(Block block, Class<? extends ItemBlock> itemClass, String name, String modID)
    {
        super.registerBlock(block, itemClass, name, modID);
        if (block instanceof IExtraBlockInfo)
        {
            List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> set = new ArrayList<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>>();
            ((IExtraBlockInfo) block).getClientTileEntityRenderers(set);
            for (Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer> par : set)
            {
                ClientRegistry.bindTileEntitySpecialRenderer(par.left(), par.right());
            }
        }
    }

    @Override
    public void regiserTileEntity(String name, Class<? extends TileEntity> clazz)
    {
        super.regiserTileEntity(name, clazz);

    }
}
