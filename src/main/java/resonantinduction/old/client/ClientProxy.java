package resonantinduction.old.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import resonantinduction.old.client.gui.GuiEncoderCoder;
import resonantinduction.old.client.gui.GuiEncoderHelp;
import resonantinduction.old.client.gui.GuiEncoderInventory;
import resonantinduction.old.client.gui.GuiImprinter;
import resonantinduction.old.client.gui.GuiProcessor;
import resonantinduction.old.client.render.BlockRenderHelper;
import resonantinduction.old.client.render.BlockRenderingHandler;
import resonantinduction.old.client.render.ItemPipeRenderer;
import resonantinduction.old.client.render.ItemRenderFluidCan;
import resonantinduction.old.client.render.ItemTankRenderer;
import resonantinduction.old.client.render.RenderConstructionPump;
import resonantinduction.old.client.render.RenderPipe;
import resonantinduction.old.client.render.RenderPump;
import resonantinduction.old.client.render.RenderReleaseValve;
import resonantinduction.old.client.render.RenderSink;
import resonantinduction.old.client.render.RenderTank;
import resonantinduction.old.client.render.RenderTestCar;
import resonantinduction.old.client.render.RenderTurkey;
import resonantinduction.old.core.misc.EntityFarmEgg;
import resonantinduction.old.core.misc.EntityTurkey;
import resonantinduction.old.core.recipe.RecipeLoader;
import resonantinduction.old.mechanics.processor.TileEntityProcessor;
import resonantinduction.old.transport.encoder.TileEntityEncoder;
import resonantinduction.old.transport.fluid.TileEntityReleaseValve;
import resonantinduction.old.transport.fluid.TileEntityTank;
import resonantinduction.old.transport.fluid.TileKitchenSink;
import resonantinduction.old.transport.fluid.pipes.TileEntityPipe;
import resonantinduction.old.transport.fluid.pump.TileEntityConstructionPump;
import resonantinduction.old.transport.fluid.pump.TileEntityStarterPump;
import resonantinduction.old.transport.imprinter.TileEntityImprinter;
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
		// ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRod.class, new RenderGearRod());
		// ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGenerator.class, new
		// RenderGenerator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReleaseValve.class, new RenderReleaseValve());
		ClientRegistry.bindTileEntitySpecialRenderer(TileKitchenSink.class, new RenderSink());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConstructionPump.class, new RenderConstructionPump());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTank.class, new RenderTank());

		MinecraftForgeClient.registerItemRenderer(RecipeLoader.blockPipe.blockID, new ItemPipeRenderer());
		MinecraftForgeClient.registerItemRenderer(RecipeLoader.blockTank.blockID, new ItemTankRenderer());
		MinecraftForgeClient.registerItemRenderer(RecipeLoader.blockReleaseValve.blockID, new ItemPipeRenderer());

		RenderingRegistry.registerBlockHandler(new BlockRenderHelper());
		RenderingRegistry.registerBlockHandler(new BlockRenderingHandler());
		if (RecipeLoader.itemFluidCan != null)
			MinecraftForgeClient.registerItemRenderer(RecipeLoader.itemFluidCan.itemID, new ItemRenderFluidCan());
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
					return new GuiBatteryBox(player.inventory, (TileBatteryBox) tileEntity);
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
