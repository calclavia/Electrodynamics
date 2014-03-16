package resonantinduction.core.resource.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction.RecipeType;
import resonantinduction.core.Settings;
import resonantinduction.core.resource.ResourceGenerator;
import calclavia.api.recipe.MachineRecipes;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockFluidMixture extends BlockFluidFinite
{
	public BlockFluidMixture(Fluid fluid)
	{
		super(Settings.getNextBlockID(fluid.getName()), fluid, Material.water);
		setTextureName(Reference.PREFIX + "mixture_flow");
		this.setUnlocalizedName(Reference.PREFIX + "fluidMixture");
	}

	public void setQuanta(World world, int x, int y, int z, int quanta)
	{
		if (quanta > 0 && quanta <= quantaPerBlock)
			world.setBlockMetadataWithNotify(x, y, z, quanta, 3);
		else
			world.setBlockToAir(x, y, z);
	}

	/* IFluidBlock */
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain)
	{
		FluidStack stack = new FluidStack(getFluid(), (int) (FluidContainerRegistry.BUCKET_VOLUME * this.getFilledPercentage(world, x, y, z)));
		if (doDrain)
			world.setBlockToAir(x, y, z);
		return stack;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess access, int x, int y, int z)
	{
		return ResourceGenerator.getColor(ResourceGenerator.mixtureToMaterial(getFluid().getName()));
	}

	public boolean mix(World world, int x, int y, int z, ItemStack stack)
	{
		if (MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER.name(), stack).length > 0 && getQuantaValue(world, x, y, z) < quantaPerBlock)
		{
			if (getQuantaValue(world, x, y, z) < quantaPerBlock)
			{
				world.setBlockMetadataWithNotify(x, y, z, getQuantaValue(world, x, y, z) + 1, 3);
				world.markBlockForUpdate(x, y, z);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canDrain(World world, int x, int y, int z)
	{
		return true;
	}

	public int getQuantaPerBlock()
	{
		return quantaPerBlock;
	}
}
