/**
 * 
 */
package resonantinduction.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * @author Calclavia
 * 
 */
public class CommonProxy implements IGuiHandler
{
	public void preInit()
	{

	}

	public void postInit()
	{

	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	public boolean isPaused()
	{
		return false;
	}

	public boolean isFancy()
	{
		return false;
	}

}
