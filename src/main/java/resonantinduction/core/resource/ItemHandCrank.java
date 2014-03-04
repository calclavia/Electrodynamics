package resonantinduction.core.resource;

import net.minecraft.item.Item;
import net.minecraft.world.World;

public class ItemHandCrank extends Item
{
	public ItemHandCrank(int id)
	{
		super(id);
		setMaxStackSize(1);
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z)
	{
		return true;
	}
}
