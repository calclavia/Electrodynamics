/**
 * 
 */
package resonantinduction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonantinduction.base.Vector3;
import resonantinduction.contractor.TileEntityEMContractor;
import resonantinduction.fx.FXElectricBolt;
import resonantinduction.multimeter.GuiMultimeter;
import resonantinduction.multimeter.TileEntityMultimeter;
import resonantinduction.render.BlockRenderingHandler;
import resonantinduction.render.RenderEMContractor;
import resonantinduction.render.RenderTesla;
import resonantinduction.tesla.TileEntityTesla;
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

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTesla.class, new RenderTesla());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEMContractor.class, new RenderEMContractor());
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityMultimeter)
		{
			return new GuiMultimeter(player.inventory, ((TileEntityMultimeter) tileEntity));
		}
		return null;
	}

	@Override
	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b)
	{
		if (world.isRemote)
		{
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXElectricBolt(world, start, target).setColor(r, g, b));
		}
	}
}
