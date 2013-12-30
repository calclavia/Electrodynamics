package com.builtbroken.minecraft;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.IExtraInfo.IExtraBlockInfo;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


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
    public void renderBeam(World world, Vector3 position, Vector3 target, Color color, int age)
    {
        if (world.isRemote || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXBeam(world, position, target, color, DarkCore.TEXTURE_DIRECTORY + "", age));
        }
    }
}
