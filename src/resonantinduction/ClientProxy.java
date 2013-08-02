/**
 * 
 */
package resonantinduction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import resonantinduction.render.BlockRenderingHandler;
import resonantinduction.render.RenderTesla;
import resonantinduction.tesla.TileEntityTesla;
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
		RenderingRegistry.registerBlockHandler(BlockRenderingHandler.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTesla.class, new RenderTesla());
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

}
