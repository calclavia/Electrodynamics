/**
 * 
 */
package resonantinduction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.multimeter.ContainerMultimeter;
import resonantinduction.multimeter.TileEntityMultimeter;
import universalelectricity.api.vector.Vector3;
import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;
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
		/*
		 * else if (tileEntity instanceof TileEntityBattery)
		 * {
		 * return new ContainerBattery(player.inventory, ((TileEntityBattery) tileEntity));
		 * }
		 */

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

	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b, boolean split)
	{

	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b)
	{
		this.renderElectricShock(world, start, target, r, g, b, true);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, ColourRGBA color)
	{
		this.renderElectricShock(world, start, target, color.r / 255, color.g / 255, color.b / 255);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, ColourRGBA color, boolean split)
	{
		this.renderElectricShock(world, start, target, color.r / 255, color.g / 255, color.b / 255, split);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target)
	{
		this.renderElectricShock(world, start, target, true);
	}

	public void renderElectricShock(World world, Vector3 start, Vector3 target, boolean b)
	{
		this.renderElectricShock(world, start, target, 0.55f, 0.7f, 1f, b);

	}

	public boolean isFancy()
	{
		return false;
	}

}
