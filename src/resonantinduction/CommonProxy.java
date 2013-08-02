/**
 * 
 */
package resonantinduction;

import resonantinduction.base.Vector3;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

/**
 * @author Calclavia
 * 
 */
public class CommonProxy implements IGuiHandler
{
	public void registerRenderers()
	{

	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b)
	{

	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target)
	{
		renderElectricShock(world, start, target, 0.55f, 0.7f, 1f);
	}
}
