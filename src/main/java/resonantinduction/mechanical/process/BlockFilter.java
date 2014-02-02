package resonantinduction.mechanical.process;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.RecipeUtils.Resource;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.prefab.block.BlockRI;
import resonantinduction.core.resource.fluid.BlockFluidMixture;
import resonantinduction.core.resource.fluid.TileFluidMixture;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.inventory.InventoryUtility;

/**
 * Used for filtering liquid mixtures
 * 
 * @author Calclavia
 * 
 */
public class BlockFilter extends BlockRI implements ITileEntityProvider
{
	public BlockFilter()
	{
		super("filter");
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		world.scheduleBlockUpdate(x, y, z, blockID, 20);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborID)
	{
		world.scheduleBlockUpdate(x, y, z, blockID, 20);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random)
	{
		Vector3 position = new Vector3(x, y, z);
		Vector3 checkAbove = position.clone().translate(ForgeDirection.UP);
		Vector3 checkBelow = position.clone().translate(ForgeDirection.DOWN);

		Block bAbove = Block.blocksList[checkAbove.getBlockID(world)];
		Block bBelow = Block.blocksList[checkAbove.getBlockID(world)];

		if (bAbove instanceof BlockFluidMixture && world.isAirBlock(checkBelow.intX(), checkBelow.intY(), checkBelow.intZ()))
		{
			world.spawnParticle("dripWater", x + 0.5, y, z + 0.5, 0, 0, 0);

			/**
			 * Leak the fluid down.
			 */
			BlockFluidMixture fluidBlock = (BlockFluidMixture) bAbove;
			int amount = fluidBlock.getQuantaValue(world, checkAbove.intX(), checkAbove.intY(), checkAbove.intZ());

			/**
			 * Drop item from fluid.
			 */
			for (Resource resoure : MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER, "dust" + LanguageUtility.capitalizeFirst(fluidBlock.getFluid().getName().replace("mixture", ""))))
			{
				InventoryUtility.dropItemStack(world, checkAbove.clone().add(0.5), resoure.getItemStack().copy());
			}

			int remaining = amount - 1;

			/**
			 * Remove liquid from top.
			 */
			if (remaining > 0)
			{
				fluidBlock.setQuanta(world, checkAbove.intX(), checkAbove.intY(), checkAbove.intZ(), remaining);
				world.scheduleBlockUpdate(x, y, z, blockID, 20);
			}
			else
			{
				checkAbove.setBlock(world, 0);
			}

			/**
			 * Add liquid to bottom.
			 */
			checkBelow.setBlock(world, Block.waterStill.blockID, 0, 3);
		}
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

}
