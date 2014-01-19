package resonantinduction.core.prefab.part;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.TMultiPart;

public abstract class PartAdvanced extends TMultiPart
{
	protected long ticks = 0;

	@Override
	public void update()
	{
		if (ticks == 0)
		{
			initiate();
		}

		if (ticks >= Long.MAX_VALUE)
		{
			ticks = 1;
		}

		ticks++;
	}

	/**
	 * Called on the TileEntity's first tick.
	 */
	public void initiate()
	{
	}

	public World getWorld()
	{
		return world();
	}

	protected abstract ItemStack getItem();

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());
		return drops;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return getItem();
	}

	protected boolean checkRedstone(int side)
	{
		if (this.world().isBlockIndirectlyGettingPowered(x(), y(), z()))
		{
			return true;
		}
		else
		{
			for (TMultiPart tp : tile().jPartList())
			{
				if (tp instanceof IRedstonePart)
				{
					IRedstonePart rp = (IRedstonePart) tp;
					if ((Math.max(rp.strongPowerLevel(side), rp.weakPowerLevel(side)) << 4) > 0)
					{
						return true;
					}
				}
			}
		}

		return false;
	}
}
