package resonantinduction.assemblyline.client;

import resonantinduction.assemblyline.ALRecipeLoader;
import resonantinduction.assemblyline.CommonProxy;
import resonantinduction.assemblyline.client.gui.GuiBatteryBox;
import resonantinduction.assemblyline.client.gui.GuiEncoderCoder;
import resonantinduction.assemblyline.client.gui.GuiEncoderHelp;
import resonantinduction.assemblyline.client.gui.GuiEncoderInventory;
import resonantinduction.assemblyline.client.gui.GuiImprinter;
import resonantinduction.assemblyline.client.gui.GuiProcessor;
import resonantinduction.assemblyline.client.render.BlockRenderHelper;
import resonantinduction.assemblyline.client.render.BlockRenderingHandler;
import resonantinduction.assemblyline.client.render.ItemPipeRenderer;
import resonantinduction.assemblyline.client.render.ItemRenderFluidCan;
import resonantinduction.assemblyline.client.render.ItemTankRenderer;
import resonantinduction.assemblyline.client.render.RenderConstructionPump;
import resonantinduction.assemblyline.client.render.RenderPipe;
import resonantinduction.assemblyline.client.render.RenderPump;
import resonantinduction.assemblyline.client.render.RenderReleaseValve;
import resonantinduction.assemblyline.client.render.RenderSink;
import resonantinduction.assemblyline.client.render.RenderTank;
import resonantinduction.assemblyline.client.render.RenderTestCar;
import resonantinduction.assemblyline.client.render.RenderTurkey;
import resonantinduction.assemblyline.entities.EntityFarmEgg;
import resonantinduction.assemblyline.entities.EntityTurkey;
import resonantinduction.assemblyline.entities.prefab.EntityTestCar;
import resonantinduction.assemblyline.fluid.pipes.TileEntityPipe;
import resonantinduction.assemblyline.fluid.pump.TileEntityConstructionPump;
import resonantinduction.assemblyline.fluid.pump.TileEntityStarterPump;
import resonantinduction.assemblyline.imprinter.TileEntityImprinter;
import resonantinduction.assemblyline.machine.TileEntityBatteryBox;
import resonantinduction.assemblyline.machine.TileEntityReleaseValve;
import resonantinduction.assemblyline.machine.TileEntitySink;
import resonantinduction.assemblyline.machine.TileEntityTank;
import resonantinduction.assemblyline.machine.encoder.TileEntityEncoder;
import resonantinduction.assemblyline.machine.processor.TileEntityProcessor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
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
