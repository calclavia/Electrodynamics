/**
 * 
 */
package resonantinduction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.base.Vector3;
import resonantinduction.battery.ContainerBattery;
import resonantinduction.battery.TileEntityBattery;
import resonantinduction.multimeter.ContainerMultimeter;
import resonantinduction.multimeter.TileEntityMultimeter;
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
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityMultimeter)
		{
			return new ContainerMultimeter(player.inventory, ((TileEntityMultimeter) tileEntity));
		}
		else if (tileEntity instanceof TileEntityBattery)
		{
			return new ContainerBattery(player.inventory, ((TileEntityBattery) tileEntity));
		}
		
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

	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b)
	{

	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, Vector3 color)
	{
		this.renderElectricShock(world, start, target, (float) color.x, (float) color.y, (float) color.z);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target)
	{
		renderElectricShock(world, start, target, 0.55f, 0.7f, 1f);
	}
}
