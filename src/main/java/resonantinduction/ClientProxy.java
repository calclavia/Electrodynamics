package resonantinduction;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import resonantinduction.core.render.BlockRenderingHandler;
import resonantinduction.core.render.RenderRIItem;
import resonantinduction.machine.crusher.ItemDust;
import resonantinduction.transport.battery.RenderBattery;
import resonantinduction.transport.battery.TileBattery;
import resonantinduction.transport.fx.FXElectricBolt;
import resonantinduction.transport.gui.GuiMultimeter;
import resonantinduction.transport.levitator.RenderLevitator;
import resonantinduction.transport.levitator.TileEMLevitator;
import resonantinduction.transport.tesla.RenderTesla;
import resonantinduction.transport.tesla.TileTesla;
import resonantinduction.utility.multimeter.PartMultimeter;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);

		RenderingRegistry.registerBlockHandler(BlockRenderingHandler.INSTANCE);
		MinecraftForgeClient.registerItemRenderer(ResonantInduction.itemMultimeter.itemID, RenderRIItem.INSTANCE);
		MinecraftForgeClient.registerItemRenderer(ResonantInduction.itemTransformer.itemID, RenderRIItem.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileTesla.class, new RenderTesla());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEMLevitator.class, new RenderLevitator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBattery.class, new RenderBattery());
	}

	@Override
	public void postInit()
	{
		ItemDust.computeColors();
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMultipart)
		{
			TMultiPart part = ((TileMultipart) tileEntity).partMap(id);

			if (part instanceof PartMultimeter)
			{
				return new GuiMultimeter(player.inventory, (PartMultimeter) part);
			}
		}

		return null;
	}

	@Override
	public boolean isPaused()
	{
		if (FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient().getIntegratedServer().getPublic())
		{
			GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

			if (screen != null)
			{
				if (screen.doesGuiPauseGame())
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isFancy()
	{
		return FMLClientHandler.instance().getClient().gameSettings.fancyGraphics;
	}

	@Override
	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b, boolean split)
	{
		if (world.isRemote)
		{
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXElectricBolt(world, start, target, split).setColor(r, g, b));
		}
	}
}
