package resonantinduction.atomic.machine.reactor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import resonant.content.prefab.java.TileAdvanced;
import resonant.content.spatial.block.SpatialBlock;
import resonant.lib.path.IPathCallBack;
import resonant.lib.path.Pathfinder;
import universalelectricity.core.transform.vector.Vector3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reactor Drain
 *
 * @author Calclavia
 */
public class TileReactorDrain extends TileAdvanced implements IFluidHandler
{
	private final Set<IFluidTank> tanks = new HashSet<IFluidTank>();
	private long lastFindTime = -1;

	public TileReactorDrain()
	{
		super(Material.iron);
	}

	public void find()
	{
		this.tanks.clear();
		final World world = this.worldObj;
		final Vector3 position = new Vector3(this);

		Pathfinder finder = new Pathfinder(new IPathCallBack()
		{
			@Override
			public Set<Vector3> getConnectedNodes(Pathfinder finder, Vector3 currentNode)
			{
				Set<Vector3> neighbors = new HashSet<Vector3>();

				for (int i = 0; i < 6; i++)
				{
					ForgeDirection direction = ForgeDirection.getOrientation(i);
					Vector3 position = currentNode.clone().add(direction);
					Block block = position.getBlock(world);

					if (block == null || block instanceof IFluidBlock || position.getTileEntity(world) instanceof TileReactorCell)
					{
						neighbors.add(position);
					}
				}

				return neighbors;
			}

			@Override
			public boolean onSearch(Pathfinder finder, Vector3 start, Vector3 node)
			{
				if (node.getTileEntity(world) instanceof TileReactorCell)
				{
					finder.results.add(node);
				}

				if (node.distance(position) > 6)
				{
					return true;
				}

				return false;
			}
		}).init(new Vector3(this).add(ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite()));

		for (Vector3 node : finder.results)
		{
			TileEntity tileEntity = node.getTileEntity(this.worldObj);

			if (tileEntity instanceof TileReactorCell)
			{
				this.tanks.add(((TileReactorCell) tileEntity).tank());
			}
		}

		this.lastFindTime = this.worldObj.getWorldTime();
	}

	public IFluidTank getOptimalTank()
	{
		if (this.lastFindTime == -1 || this.worldObj.getWorldTime() - this.lastFindTime > 20)
		{
			this.find();
		}

		if (this.tanks.size() > 0)
		{
			IFluidTank optimalTank = null;

			for (IFluidTank tank : this.tanks)
			{
				if (tank != null)
				{
					if (optimalTank == null || (optimalTank != null && getFluidSafe(tank.getFluid()) > getFluidSafe(optimalTank.getFluid())))
					{
						optimalTank = tank;
					}
				}
			}

			return optimalTank;
		}

		return null;
	}

	public int getFluidSafe(FluidStack stack)
	{
		if (stack != null)
		{
			return stack.amount;
		}

		return 0;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if (this.getOptimalTank() != null)
		{
			return this.getOptimalTank().drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		List<FluidTankInfo> tankInfoList = new ArrayList<FluidTankInfo>();

		this.getOptimalTank();
		for (IFluidTank tank : this.tanks)
		{
			tankInfoList.add(tank.getInfo());
		}

		return tankInfoList.toArray(new FluidTankInfo[0]);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		SpatialBlock.icon().put("ReactorDrain_front", iconRegister.registerIcon("ReactorDrain_front"));
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if (side == metadata)
		{
			return SpatialBlock.icon().get("ReactorDrain_front");
		}
		return super.getIcon(side, metadata);
	}

	@Override
	public void onPlaced(EntityLivingBase entityLiving, ItemStack itemStack)
	{
		if (MathHelper.abs((float) entityLiving.posX - x()) < 2.0F && MathHelper.abs((float) entityLiving.posZ - z()) < 2.0F)
		{
			double d0 = entityLiving.posY + 1.82D - entityLiving.yOffset;

			if (d0 - y() > 2.0D)
			{
				world().setBlockMetadataWithNotify(x(), y(), z(), 1, 3);
				return;
			}

			if (y() - d0 > 0.0D)
			{
				world().setBlockMetadataWithNotify(x(), y(), z(), 0, 3);
				return;
			}
		}

		super.onPlaced(entityLiving, itemStack);
	}
}
