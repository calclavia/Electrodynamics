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
import resonantinduction.core.resource.fluid.TileLiquidMixture;
import universalelectricity.api.vector.Vector3;
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

		TileEntity tileAbove = checkAbove.getTileEntity(world);
		TileEntity tileBelow = checkBelow.getTileEntity(world);

		if (tileAbove instanceof TileLiquidMixture && (tileBelow == null || tileBelow instanceof TileLiquidMixture))
		{
			world.spawnParticle("dripWater", x + 0.5, y, z + 0.5, 0, 0, 0);

			if (((TileLiquidMixture) tileAbove).items.size() > 0)
			{
				/**
				 * Leak the fluid down.
				 */
				BlockFluidMixture fluidBlock = (BlockFluidMixture) ResonantInduction.blockFluidMixture;
				int amount = fluidBlock.getQuantaValue(world, x, y, z);

				/**
				 * All fluid is filtered out, spawn all the items.
				 */
				 if (amount <= 1)
				{
					System.out.println("filter dropped");
					for (ItemStack itemStack : ((TileLiquidMixture) tileAbove).items)
					{
						for (Resource resoure : MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER, itemStack))
						{
							InventoryUtility.dropItemStack(world, checkAbove.clone().add(0.5), resoure.getItemStack().copy());
						}
					}
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
				if (checkBelow.getBlockID(world) == ResonantInduction.blockFluidMixture.blockID)
				{
					fluidBlock.setQuanta(world, checkBelow.intX(), checkBelow.intY(), checkBelow.intZ(), fluidBlock.getQuantaValue(world, checkBelow.intX(), checkBelow.intY(), checkBelow.intZ()) + 1);
				}
				else
				{
					checkBelow.setBlock(world, Block.waterStill.blockID, 3);
				}
			}
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
