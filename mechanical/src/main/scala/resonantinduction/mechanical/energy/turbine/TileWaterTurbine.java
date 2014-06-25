package resonantinduction.mechanical.energy.turbine;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INodeProvider;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * The vertical water turbine collects flowing water flowing on X axis.
 * The horizontal water turbine collects flowing water on Z axis.
 * 
 * @author Calclavia
 * 
 */
public class TileWaterTurbine extends TileTurbine
{
	public int powerTicks = 0;

	public TileWaterTurbine()
	{
	    mechanicalNode.torque = defaultTorque;
		mechanicalNode = new TurbineNode(this)
		{
			@Override
			public boolean canConnect(ForgeDirection from, Object source)
			{
				if (source instanceof MechanicalNode && !(source instanceof TileTurbine))
				{
					/**
					 * Face to face stick connection.
					 */
					TileEntity sourceTile = position().translate(from).getTileEntity(getWorld());

					if (sourceTile instanceof INodeProvider)
					{
						MechanicalNode sourceInstance = (MechanicalNode) ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from.getOpposite());
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
		{
		    mechanicalNode.torque = (long) (defaultTorque / (1d / multiBlockRadius));
		}
		else
		{
		    mechanicalNode.torque = defaultTorque / 12;
		}

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
			ForgeDirection currentDir = getDirection();

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != currentDir && dir != currentDir.getOpposite())
				{
					Vector3 check = new Vector3(this).translate(dir);
					int blockID = worldObj.getBlockId(check.intX(), check.intY(), check.intZ());
					int metadata = worldObj.getBlockMetadata(check.intX(), check.intY(), check.intZ());

					if (blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID)
					{
						try
						{
							Method m = ReflectionHelper.findMethod(BlockFluid.class, null, new String[] { "getFlowVector", "func_72202_i" }, IBlockAccess.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
							Vector3 vector = new Vector3((Vec3) m.invoke(Block.waterMoving, worldObj, check.intX(), check.intY(), check.intZ()));

							if ((currentDir.offsetZ > 0 && vector.x < 0) || (currentDir.offsetZ < 0 && vector.x > 0) || (currentDir.offsetX > 0 && vector.z > 0) || (currentDir.offsetX < 0 && vector.z < 0))
							{
							    mechanicalNode.torque = -mechanicalNode.torque;
							}

							if (getDirection().offsetX != 0)
							{
								getMultiBlock().get().power += Math.abs(getWaterPower() * vector.z * (7 - metadata) / 7f);
								powerTicks = 20;
							}
							
							if (getDirection().offsetZ != 0)
							{
								getMultiBlock().get().power += Math.abs(getWaterPower() * vector.x * (7 - metadata) / 7f);
								powerTicks = 20;
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
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
		return (maxPower / (2 - tier + 1)) * Settings.WATER_POWER_RATIO;
	}
}
