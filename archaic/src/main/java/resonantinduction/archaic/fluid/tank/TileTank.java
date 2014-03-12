package resonantinduction.archaic.fluid.tank;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import resonantinduction.core.fluid.FluidDistributionetwork;
import resonantinduction.core.fluid.IFluidDistribution;
import resonantinduction.core.fluid.TileFluidDistribution;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.content.module.TileBlock.IComparatorInputOverride;
import calclavia.lib.utility.FluidUtility;
import calclavia.lib.utility.WorldUtility;
import calclavia.lib.utility.inventory.InventoryUtility;

public class TileTank extends TileFluidDistribution implements IComparatorInputOverride
{
	public static final int VOLUME = 16;

	public TileTank()
	{
		super(UniversalElectricity.machine);
		this.getInternalTank().setCapacity(VOLUME * FluidContainerRegistry.BUCKET_VOLUME);
		isOpaqueCube = false;
		normalRender = false;
		itemBlock = ItemBlockFluidContainer.class;
	}

	protected boolean use(EntityPlayer player, int side, Vector3 vector3)
	{
		if (!world().isRemote)
		{
			if (player.isSneaking())
			{
				ItemStack dropStack = ItemBlockFluidContainer.getWrenchedItem(world(), position());
				if (dropStack != null)
				{
					if (player.getHeldItem() == null)
					{
						player.inventory.setInventorySlotContents(player.inventory.currentItem, dropStack);
					}
					else
					{
						InventoryUtility.dropItemStack(world(), position(), dropStack);
					}

					position().setBlock(world(), 0);
				}
			}

			return FluidUtility.playerActivatedFluidItem(world(), x(), y(), z(), player, side);
		}

		return true;
	}

	@Override
	public int getComparatorInputOverride(int side)
	{
		if (getNetwork().getTank().getFluid() != null)
			return (int) (15 * ((double) getNetwork().getTank().getFluidAmount() / (double) getNetwork().getTank().getCapacity()));
		return 0;
	}

	@Override
	public int getLightValue(IBlockAccess access)
	{
		if (getInternalTank().getFluid() != null)
			return getInternalTank().getFluid().getFluid().getLuminosity();
		return super.getLightValue(access);
	}

	@Override
	public FluidDistributionetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new TankNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public void setNetwork(FluidDistributionetwork network)
	{
		if (network instanceof TankNetwork)
		{
			this.network = network;
		}
	}

	@Override
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof TileTank)
			{
				getNetwork().merge(((IFluidDistribution) tileEntity).getNetwork());
				renderSides = WorldUtility.setEnableSide(renderSides, side, true);
				connectedBlocks[side.ordinal()] = tileEntity;
			}
		}
	}
}
