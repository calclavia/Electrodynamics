package com.builtbroken.assemblyline.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import com.builtbroken.assemblyline.ALRecipeLoader;
import com.builtbroken.assemblyline.CommonProxy;
import com.builtbroken.assemblyline.client.gui.GuiBatteryBox;
import com.builtbroken.assemblyline.client.gui.GuiEncoderCoder;
import com.builtbroken.assemblyline.client.gui.GuiEncoderHelp;
import com.builtbroken.assemblyline.client.gui.GuiEncoderInventory;
import com.builtbroken.assemblyline.client.gui.GuiImprinter;
import com.builtbroken.assemblyline.client.gui.GuiProcessor;
import com.builtbroken.assemblyline.client.render.BlockRenderHelper;
import com.builtbroken.assemblyline.client.render.BlockRenderingHandler;
import com.builtbroken.assemblyline.client.render.ItemPipeRenderer;
import com.builtbroken.assemblyline.client.render.ItemRenderFluidCan;
import com.builtbroken.assemblyline.client.render.ItemTankRenderer;
import com.builtbroken.assemblyline.client.render.RenderConstructionPump;
import com.builtbroken.assemblyline.client.render.RenderPipe;
import com.builtbroken.assemblyline.client.render.RenderPump;
import com.builtbroken.assemblyline.client.render.RenderReleaseValve;
import com.builtbroken.assemblyline.client.render.RenderSink;
import com.builtbroken.assemblyline.client.render.RenderTank;
import com.builtbroken.assemblyline.client.render.RenderTestCar;
import com.builtbroken.assemblyline.client.render.RenderTurkey;
import com.builtbroken.assemblyline.entities.EntityFarmEgg;
import com.builtbroken.assemblyline.entities.EntityTurkey;
import com.builtbroken.assemblyline.entities.prefab.EntityTestCar;
import com.builtbroken.assemblyline.fluid.pipes.TileEntityPipe;
import com.builtbroken.assemblyline.fluid.pump.TileEntityConstructionPump;
import com.builtbroken.assemblyline.fluid.pump.TileEntityStarterPump;
import com.builtbroken.assemblyline.imprinter.TileEntityImprinter;
import com.builtbroken.assemblyline.machine.TileEntityBatteryBox;
import com.builtbroken.assemblyline.machine.TileEntityReleaseValve;
import com.builtbroken.assemblyline.machine.TileEntitySink;
import com.builtbroken.assemblyline.machine.TileEntityTank;
import com.builtbroken.assemblyline.machine.encoder.TileEntityEncoder;
import com.builtbroken.assemblyline.machine.processor.TileEntityProcessor;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    @Override
    public void preInit()
    {
        RenderingRegistry.registerBlockHandler(new BlockRenderingHandler());
        MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityTurkey.class, new RenderTurkey());
        RenderingRegistry.registerEntityRenderingHandler(EntityFarmEgg.class, new RenderSnowball(Item.egg));
        RenderingRegistry.registerEntityRenderingHandler(EntityTestCar.class, new RenderTestCar());
    }

    @Override
    public void init()
    {
        super.init();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipe.class, new RenderPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStarterPump.class, new RenderPump());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRod.class, new RenderGearRod());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGenerator.class, new RenderGenerator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReleaseValve.class, new RenderReleaseValve());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySink.class, new RenderSink());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConstructionPump.class, new RenderConstructionPump());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTank.class, new RenderTank());

        MinecraftForgeClient.registerItemRenderer(ALRecipeLoader.blockPipe.blockID, new ItemPipeRenderer());
        MinecraftForgeClient.registerItemRenderer(ALRecipeLoader.blockTank.blockID, new ItemTankRenderer());
        MinecraftForgeClient.registerItemRenderer(ALRecipeLoader.blockReleaseValve.blockID, new ItemPipeRenderer());

        RenderingRegistry.registerBlockHandler(new BlockRenderHelper());
        RenderingRegistry.registerBlockHandler(new BlockRenderingHandler());
        if (ALRecipeLoader.itemFluidCan != null)
            MinecraftForgeClient.registerItemRenderer(ALRecipeLoader.itemFluidCan.itemID, new ItemRenderFluidCan());
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity != null)
        {
            switch (ID)
            {
                case GUI_IMPRINTER:
                {
                    return new GuiImprinter(player.inventory, (TileEntityImprinter) tileEntity);
                }
                case GUI_ENCODER:
                {
                    return new GuiEncoderInventory(player.inventory, (TileEntityEncoder) tileEntity);
                }
                case GUI_ENCODER_CODE:
                {
                    return new GuiEncoderCoder(player.inventory, (TileEntityEncoder) tileEntity);
                }
                case GUI_ENCODER_HELP:
                {
                    return new GuiEncoderHelp(player.inventory, (TileEntityEncoder) tileEntity);
                }
                case GUI_PROCESSOR:
                {
                    return new GuiProcessor(player.inventory, (TileEntityProcessor) tileEntity);
                }
                case GUI_BATTERY_BOX:
                    return new GuiBatteryBox(player.inventory, (TileEntityBatteryBox) tileEntity);
            }
        }

        return null;
    }

    @Override
    public boolean isCtrKeyDown()
    {
        return GuiScreen.isCtrlKeyDown();
    }

}
