package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.Node;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import universalelectricity.api.vector.IVectorWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 18/03/14
 * @author tgame14
 */
public class NodeRailing extends Node<IItemRailing, GridRailing, NodeRailing> implements IVectorWorld
{
    public NodeRailing (PartRailing parent)
    {
        super(parent);
    }

    @Override
    protected GridRailing newGrid ()
    {
        return new GridRailing(getClass());
    }

	/**
	 *
	 * @return possibly null, a IInventory to target
	 */
	public IInventory[] getInventoriesNearby()
	{
		List<IInventory> invList = new ArrayList<IInventory>();
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity te = parent.getWorldPos().translate(dir).getTileEntity(parent.getWorldPos().world());
			if (te != null && te instanceof IInventory)
			{
				invList.add((IInventory) te);
			}
		}
		return (IInventory[]) invList.toArray();
	}

	public boolean isLeaf()
	{
		int connectionsCount = 0;
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (parent.getWorldPos().translate(dir).getTileEntity(parent.getWorldPos().world()) instanceof IItemRailing)
			{
				connectionsCount++;
				if (connectionsCount >= 2)
				{
					return false;
				}
			}
		}
		return true;
	}


	@Override
	public double z()
	{
		return parent.getWorldPos().z();
	}

	@Override
	public World world()
	{
		return parent.getWorldPos().world();
	}

	@Override
	public double x()
	{
		return parent.getWorldPos().x();
	}

	@Override
	public double y()
	{
		return parent.getWorldPos().y();
	}
}
