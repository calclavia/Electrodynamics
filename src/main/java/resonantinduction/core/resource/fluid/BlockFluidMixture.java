package resonantinduction.core.resource.fluid;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.ResonantInduction;

/**
 * @author Calclavia
 * 
 */
public class BlockFluidMixture extends BlockFluidFinite implements ITileEntityProvider
{
	public BlockFluidMixture(int id, Fluid fluid)
	{
		super(id, fluid, Material.water);
		this.setTextureName("water_flow");
	}

	/* IFluidBlock */
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain)
	{
		TileLiquidMixture tileFluid = (TileLiquidMixture) world.getBlockTileEntity(x, y, z);
		FluidStack stack = new FluidStack(ResonantInduction.MIXTURE, (int) (FluidContainerRegistry.BUCKET_VOLUME * this.getFilledPercentage(world, x, y, z)));
		tileFluid.writeFluidToNBT(stack.tag);
		return stack;
	}

	@Override
	public boolean canDrain(World world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileLiquidMixture();
	}
}
