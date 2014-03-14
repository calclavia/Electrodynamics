package resonantinduction.mechanical.energy.turbine;

import java.lang.reflect.Method;

import calclavia.lib.grid.INodeProvider;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.turbine.TileMechanicalTurbine.TurbineNode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * The vertical wind turbine collects airflow.
 * The horizontal wind turbine collects steam from steam power plants.
 * 
 * @author Calclavia
 * 
 */
public class TileWaterTurbine extends TileMechanicalTurbine
{
	public int powerTicks = 0;

	public TileWaterTurbine()
	{
		torque = defaultTorque;
		mechanicalNode = new TurbineNode(this)
		{
			@Override
			public boolean canConnect(ForgeDirection from, Object source)
			{
				if (source instanceof MechanicalNode && !(source instanceof TileMechanicalTurbine))
				{
					/**
					 * Face to face stick connection.
					 */
					TileEntity sourceTile = position().translate(from).getTileEntity(getWorld());

					if (sourceTile instanceof INodeProvider)
					{
						MechanicalNode sourceInstance = ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from.getOpposite());
						return sourceInstance == source && (from == getDirection().getOpposite() || from == getDirection());
					}
				}

				return false;
			}
		};
	}

	@Override
	public void updateEntity()
	{
		if (getMultiBlock().isConstructed())
			torque = (long) (defaultTorque / (1d / multiBlockRadius));
		else
			torque = defaultTorque / 12;

		/**
		 * If this is a horizontal turbine.
		 */
		if (getDirection().offsetY != 0)
		{
			maxPower = 10000;

			if (powerTicks > 0)
			{
				getMultiBlock().get().power += getWaterPower();
				powerTicks--;
			}

			if (ticks % 20 == 0)
			{
				int blockIDAbove = worldObj.getBlockId(xCoord, yCoord + 1, zCoord);
				int metadata = worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord);
				boolean isWater = (blockIDAbove == Block.waterStill.blockID || blockIDAbove == Block.waterMoving.blockID);

				if (isWater && worldObj.isAirBlock(xCoord, yCoord - 1, zCoord) && metadata == 0)
				{
					powerTicks = 20;
					worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord);
					worldObj.setBlock(xCoord, yCoord - 1, zCoord, Block.waterMoving.blockID);
				}
			}
		}
		else
		{
			maxPower = 2500;

			int checkX = xCoord;
			int checkY = yCoord - 1;
			int checkZ = zCoord;
			int blockID = worldObj.getBlockId(xCoord, checkY, checkZ);
			int metadata = worldObj.getBlockMetadata(xCoord, checkY, checkZ);

			if (blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID)
			{
				try
				{
					Method m = ReflectionHelper.findMethod(BlockFluid.class, null, new String[] { "getFlowVector", "func_72202_i" }, IBlockAccess.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
					Vector3 vector = new Vector3((Vec3) m.invoke(Block.waterMoving, worldObj, xCoord, checkY, checkZ));
					ForgeDirection dir = getDirection();

					if ((dir.offsetZ > 0 && vector.x < 0) || (dir.offsetZ < 0 && vector.x > 0) || (dir.offsetX > 0 && vector.z > 0) || (dir.offsetX < 0 && vector.z < 0))
						torque = -torque;

					if (getDirection().offsetX != 0)
						getMultiBlock().get().power += Math.abs(getWaterPower() * vector.z * (7 - metadata) / 7f);
					if (getDirection().offsetZ != 0)
						getMultiBlock().get().power += Math.abs(getWaterPower() * vector.x * (7 - metadata) / 7f);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		super.updateEntity();
	}

	/**
	 * Gravitation Potential Energy:
	 * PE = mgh
	 */
	private long getWaterPower()
	{
		return maxPower / (2 - tier + 1);
	}
}
