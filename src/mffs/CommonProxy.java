package mffs;

import mffs.container.ContainerFortronCapacitor;
import mffs.tileentity.TileEntityFortronCapacitor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	public void preInit()
	{
	}

	public void init()
	{
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			if (tileEntity.getClass() == TileEntityFortronCapacitor.class)
			{
				return new ContainerFortronCapacitor(player, (TileEntityFortronCapacitor) tileEntity);
			}
		}

		return null;
	}

	public World getClientWorld()
	{
		return null;
	}

	public void renderBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age)
	{

	}
}