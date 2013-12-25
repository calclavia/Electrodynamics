package mffs;

import mffs.container.ContainerBiometricIdentifier;
import mffs.container.ContainerCoercionDeriver;
import mffs.container.ContainerForceFieldProjector;
import mffs.container.ContainerForceManipulator;
import mffs.container.ContainerFortronCapacitor;
import mffs.container.ContainerInterdictionMatrix;
import mffs.tileentity.TileBiometricIdentifier;
import mffs.tileentity.TileCoercionDeriver;
import mffs.tileentity.TileForceFieldProjector;
import mffs.tileentity.TileForceManipulator;
import mffs.tileentity.TileFortronCapacitor;
import mffs.tileentity.TileInterdictionMatrix;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;
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
			if (tileEntity.getClass() == TileFortronCapacitor.class)
			{
				return new ContainerFortronCapacitor(player, (TileFortronCapacitor) tileEntity);
			}
			else if (tileEntity.getClass() == TileForceFieldProjector.class)
			{
				return new ContainerForceFieldProjector(player, (TileForceFieldProjector) tileEntity);
			}
			else if (tileEntity.getClass() == TileCoercionDeriver.class)
			{
				return new ContainerCoercionDeriver(player, (TileCoercionDeriver) tileEntity);
			}
			else if (tileEntity.getClass() == TileBiometricIdentifier.class)
			{
				return new ContainerBiometricIdentifier(player, (TileBiometricIdentifier) tileEntity);
			}
			else if (tileEntity.getClass() == TileInterdictionMatrix.class)
			{
				return new ContainerInterdictionMatrix(player, (TileInterdictionMatrix) tileEntity);
			}
			else if (tileEntity.getClass() == TileForceManipulator.class)
			{
				return new ContainerForceManipulator(player, (TileForceManipulator) tileEntity);
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

	public void renderHologram(World world, Vector3 position, float red, float green, float blue, int age, Vector3 targetPosition)
	{

	}

	public void renderHologramMoving(World world, Vector3 position, float red, float green, float blue, int age)
	{

	}
}