package mffs;

import mffs.container.ContainerBiometricIdentifier;
import mffs.container.ContainerCoercionDeriver;
import mffs.container.ContainerForceFieldProjector;
import mffs.container.ContainerFortronCapacitor;
import mffs.container.ContainerInterdictionMatrix;
import mffs.tileentity.TileEntityBiometricIdentifier;
import mffs.tileentity.TileEntityCoercionDeriver;
import mffs.tileentity.TileEntityForceFieldProjector;
import mffs.tileentity.TileEntityFortronCapacitor;
import mffs.tileentity.TileEntityInterdictionMatrix;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.FMLCommonHandler;
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
			else if (tileEntity.getClass() == TileEntityForceFieldProjector.class)
			{
				return new ContainerForceFieldProjector(player, (TileEntityForceFieldProjector) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityCoercionDeriver.class)
			{
				return new ContainerCoercionDeriver(player, (TileEntityCoercionDeriver) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityBiometricIdentifier.class)
			{
				return new ContainerBiometricIdentifier(player, (TileEntityBiometricIdentifier) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityInterdictionMatrix.class)
			{
				return new ContainerInterdictionMatrix(player, (TileEntityInterdictionMatrix) tileEntity);
			}
		}

		return null;
	}

	public World getClientWorld()
	{
		return null;
	}

	/**
	 * Checks if the player is an operator.
	 * 
	 * @author King_Lemming
	 * @param username
	 */
	public boolean isOp(String username)
	{
		MinecraftServer theServer = FMLCommonHandler.instance().getMinecraftServerInstance();

		if (theServer != null)
		{
			return theServer.getConfigurationManager().getOps().contains(username.trim().toLowerCase());
		}

		return false;
	}

	public void renderBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age)
	{

	}

	public void renderTemporaryHologram(World world, Vector3 position, int age)
	{

	}
}