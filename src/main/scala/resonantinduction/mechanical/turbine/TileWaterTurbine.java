package resonantinduction.mechanical.turbine;

import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.core.Settings;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.core.transform.vector.Vector3;
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
					TileEntity sourceTile = position().add(from).getTileEntity(getWorld());

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
	public void update()
	{
        super.update();
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

			if (ticks() % 20 == 0)
			{
				Block blockIDAbove = worldObj.getBlock(xCoord, yCoord + 1, zCoord);
				int metadata = worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord);
				boolean isWater = (blockIDAbove == Blocks.water || blockIDAbove == Blocks.flowing_water);

				if (isWater && worldObj.isAirBlock(xCoord, yCoord - 1, zCoord) && metadata == 0)
				{
					powerTicks = 20;
					worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord);
					worldObj.setBlock(xCoord, yCoord - 1, zCoord, Blocks.flowing_water);
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
					Vector3 check = new Vector3(this).add(dir);
					Block blockID = worldObj.getBlock(check.xi(), check.yi(), check.zi());
					int metadata = worldObj.getBlockMetadata(check.xi(), check.yi(), check.zi());

					if (blockID == Blocks.water || blockID == Blocks.flowing_water)
					{
						try
						{
							Method m = ReflectionHelper.findMethod(BlockDynamicLiquid.class, null, new String[] { "getFlowVector", "func_72202_i" }, IBlockAccess.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
							Vector3 vector = new Vector3((Vec3) m.invoke(Blocks.water, worldObj, check.xi(), check.yi(), check.zi()));

							if ((currentDir.offsetZ > 0 && vector.x() < 0) || (currentDir.offsetZ < 0 && vector.x() > 0) || (currentDir.offsetX > 0 && vector.z() > 0) || (currentDir.offsetX < 0 && vector.z() < 0))
							{
							    mechanicalNode.torque = -mechanicalNode.torque;
							}

							if (getDirection().offsetX != 0)
							{
								getMultiBlock().get().power += Math.abs(getWaterPower() * vector.z() * (7 - metadata) / 7f);
								powerTicks = 20;
							}

							if (getDirection().offsetZ != 0)
							{
								getMultiBlock().get().power += Math.abs(getWaterPower() * vector.x() * (7 - metadata) / 7f);
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
	}

	/**
	 * Gravitation Potential Energy:
	 * PE = mgh
	 */
	private long getWaterPower()
	{
		return (maxPower / (2 - tier + 1)) * Settings.WATER_POWER_RATIO();
	}

    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int i = 0; i < 3; i++)
            par3List.add(new ItemStack(par1, 1, i));
    }
}
