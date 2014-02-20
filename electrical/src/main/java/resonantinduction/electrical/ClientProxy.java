package resonantinduction.electrical;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.electrical.battery.RenderBattery;
import resonantinduction.electrical.battery.TileBattery;
import resonantinduction.electrical.charger.RenderCharger;
import resonantinduction.electrical.encoder.TileEncoder;
import resonantinduction.electrical.encoder.gui.GuiEncoderInventory;
import resonantinduction.electrical.generator.solar.RenderSolarPanel;
import resonantinduction.electrical.generator.solar.TileSolarPanel;
import resonantinduction.electrical.levitator.RenderLevitator;
import resonantinduction.electrical.multimeter.GuiMultimeter;
import resonantinduction.electrical.multimeter.PartMultimeter;
import resonantinduction.electrical.multimeter.RenderMultimeter;
import resonantinduction.electrical.render.FXElectricBolt;
import resonantinduction.electrical.tesla.RenderTesla;
import resonantinduction.electrical.tesla.TileTesla;
import resonantinduction.electrical.transformer.RenderTransformer;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.item.GlobalItemRenderer;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** @author Calclavia */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		GlobalItemRenderer.register(Electrical.blockBattery.blockID, RenderBattery.INSTANCE);
		GlobalItemRenderer.register(Electrical.itemMultimeter.itemID, RenderMultimeter.INSTANCE);
		GlobalItemRenderer.register(Electrical.itemTransformer.itemID, RenderTransformer.INSTANCE);
		GlobalItemRenderer.register(Electrical.itemCharger.itemID, RenderCharger.INSTANCE);
		GlobalItemRenderer.register(Electrical.itemLevitator.itemID, RenderLevitator.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileTesla.class, new RenderTesla());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBattery.class, new RenderBattery());
		ClientRegistry.bindTileEntitySpecialRenderer(TileSolarPanel.class, new RenderSolarPanel());
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
		else if (tileEntity instanceof TileEncoder)
		{
			return new GuiEncoderInventory(player.inventory, (TileEncoder) tileEntity);
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
