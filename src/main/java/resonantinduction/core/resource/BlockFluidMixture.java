package resonantinduction.core.resource;

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

	@Override
	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity)
	{
		// If this item can be smelted into another fluid, add it to the mixture.
	}

	/* IFluidBlock */
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain)
	{
		TileFluidMixture tileFluid = (TileFluidMixture) world.getBlockTileEntity(x, y, z);
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
		return new TileFluidMixture();
	}
}
