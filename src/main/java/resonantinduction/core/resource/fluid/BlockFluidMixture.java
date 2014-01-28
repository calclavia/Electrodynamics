package resonantinduction.core.resource.fluid;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockFluidMixture extends BlockFluidFinite implements ITileEntityProvider
{
	public BlockFluidMixture()
	{
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, "fluidMixture", Settings.getNextBlockID()).getInt(), ResonantInduction.fluidMixture, Material.water);
		setTextureName(Reference.PREFIX + "mixture_flow");
		this.setUnlocalizedName(Reference.PREFIX + "fluidMixture");
	}

	public void setQuanta(World world, int x, int y, int z, int quanta)
	{
		if (quanta > 0)
			world.setBlockMetadataWithNotify(x, y, z, quanta, 3);
		else
			world.setBlockToAir(x, y, z);
	}

	/* IFluidBlock */
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain)
	{
		TileFluidMixture tileFluid = (TileFluidMixture) world.getBlockTileEntity(x, y, z);
		FluidStack stack = new FluidStack(ResonantInduction.fluidMixture, (int) (FluidContainerRegistry.BUCKET_VOLUME * this.getFilledPercentage(world, x, y, z)));
		tileFluid.writeFluidToNBT(stack.tag != null ? stack.tag : new NBTTagCompound());
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess access, int x, int y, int z)
	{
		TileFluidMixture tileFluid = (TileFluidMixture) access.getBlockTileEntity(x, y, z);
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
		return new TileFluidMixture();
	}
}
