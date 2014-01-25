package resonantinduction.core.resource.fluid;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
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

	public void setQuanta(World world, int x, int y, int z, int quanta)
	{
		world.setBlockMetadataWithNotify(x, y, z, quanta - 1, 3);
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

	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess access, int x, int y, int z)
	{
		TileLiquidMixture tileFluid = (TileLiquidMixture) access.getBlockTileEntity(x, y, z);
		return tileFluid.getColor();
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
