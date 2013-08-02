/**
 * 
 */
package resonantinduction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import resonantinduction.render.RenderTesla;
import resonantinduction.tesla.TileEntityTesla;
import cpw.mods.fml.client.registry.ClientRegistry;

/**
 * @author Calclavia
 * 
 */
public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTesla.class, new RenderTesla());
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

}
