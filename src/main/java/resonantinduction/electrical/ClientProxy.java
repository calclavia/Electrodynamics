package resonantinduction.electrical;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import resonantinduction.electrical.battery.RenderBattery;
import resonantinduction.electrical.battery.TileBattery;
import resonantinduction.electrical.fx.FXElectricBolt;
import resonantinduction.electrical.multimeter.GuiMultimeter;
import resonantinduction.electrical.multimeter.PartMultimeter;
import resonantinduction.electrical.tesla.RenderTesla;
import resonantinduction.electrical.tesla.TileTesla;
import resonantinduction.old.core.render.BlockRenderingHandler;
import resonantinduction.old.core.render.RenderRIItem;
import resonantinduction.old.transport.levitator.RenderLevitator;
import resonantinduction.old.transport.levitator.TileEMLevitator;
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
	public void preInit()
	{
		RenderingRegistry.registerBlockHandler(BlockRenderingHandler.INSTANCE);
		MinecraftForgeClient.registerItemRenderer(Electrical.itemMultimeter.itemID, RenderRIItem.INSTANCE);
		MinecraftForgeClient.registerItemRenderer(Electrical.itemTransformer.itemID, RenderRIItem.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileTesla.class, new RenderTesla());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEMLevitator.class, new RenderLevitator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBattery.class, new RenderBattery());
	}

	@Override
	public void postInit()
	{
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
	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b, boolean split)
	{
		if (world.isRemote)
		{
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXElectricBolt(world, start, target, split).setColor(r, g, b));
		}
	}
}
