package mffs.field.module;

import resonant.api.mffs.IProjector;
import mffs.base.ItemModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import universalelectricity.core.transform.vector.Vector3;

import java.util.Set;

public class ItemModuleSponge extends ItemModule
{
	public ItemModuleSponge(int i)
	{
		super(i, "moduleSponge");
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onProject(IProjector projector, Set<Vector3> fields)
	{
		if (projector.getTicks() % 60 == 0)
		{
			World world = ((TileEntity) projector).worldObj;

			if (!world.isRemote)
			{
				for (Vector3 point : projector.getInteriorPoints())
				{
					Block block = Block.blocksList[point.getBlockID(world)];

					if (block instanceof BlockFluid || block instanceof BlockFluidBase)
					{
						point.setBlock(world, 0);
					}
				}
			}
		}

		return super.onProject(projector, fields);
	}

	@Override
	public boolean requireTicks(ItemStack moduleStack)
	{
		return true;
	}
}