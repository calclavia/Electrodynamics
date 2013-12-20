package com.builtbroken.minecraft;

import java.awt.Color;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

import com.builtbroken.minecraft.network.PacketManagerEffects;

import cpw.mods.fml.common.registry.GameRegistry;

public class RegistryProxy
{
    public void registerBlock(Block block, Class<? extends ItemBlock> itemClass, String name, String modID)
    {
        if (block != null && name != null)
        {
            GameRegistry.registerBlock(block, itemClass == null ? ItemBlock.class : itemClass, name, modID);
        }
    }

    public void regiserTileEntity(String name, Class<? extends TileEntity> clazz)
    {
        GameRegistry.registerTileEntityWithAlternatives(clazz, name, "DM" + name);
    }

    public void renderBeam(World world, Vector3 position, Vector3 target, Color color, int age)
    {
        PacketManagerEffects.sendClientLaserEffect(world, position, target, color, age);
    }
}
