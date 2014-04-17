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
 * @author tgame14
 * @since 18/03/14
 */
public class NodeRailing extends Node<IItemRailing, GridRailing, NodeRailing> implements IVectorWorld
{
	public NodeRailing(PartRailing parent)
	{
		super(parent);
	}

	@Override
	protected GridRailing newGrid()
	{
		return new GridRailing(getClass());
	}

	/**
	 * @return possibly null, a IInventory to target
	 */

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
