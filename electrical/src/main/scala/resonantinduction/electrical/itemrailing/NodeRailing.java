package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.Node;
import calclavia.lib.render.EnumColor;
import com.google.common.collect.Lists;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;
import universalelectricity.api.vector.IVectorWorld;
import universalelectricity.api.vector.VectorWorld;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class NodeRailing extends Node<PartRailing, GridRailing, NodeRailing> implements IVectorWorld, IItemRailing
{
	private EnumColor color;
	private Set<IItemRailingTransfer> itemNodeSet;

	public NodeRailing(PartRailing parent)
	{
		super(parent);
		this.itemNodeSet = new HashSet<IItemRailingTransfer>();
	}

	@Override
	protected GridRailing newGrid()
	{
		return new GridRailing(getClass());
	}

	@Override
	public double z()
	{
		return this.getWorldPos().z();
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

	@Override
	public boolean canItemEnter (IItemRailingTransfer item)
	{
		return this.color != null ? this.color == item.getColor() : false;
	}

	@Override
	public boolean canConnectToRailing (IItemRailing railing, ForgeDirection from)
	{
		return this.color != null ? this.color == railing.getRailingColor() : true;
	}

	@Override
	public EnumColor getRailingColor ()
	{
		return this.color;
	}

	@Override
	public IItemRailing setRailingColor (EnumColor color)
	{
		this.color = color;
		return this;
	}

	@Override
	public VectorWorld getWorldPos()
	{
		return parent.getWorldPos();
	}

	@Override
	public IInventory[] getInventoriesNearby()
	{
		ArrayList<IInventory> invList = Lists.<IInventory>newArrayList();
		for (TileEntity tile : parent.getConnections())
		{
			if (tile instanceof IInventory)
			{
				invList.add((IInventory) tile);
			}
		}
		return (IInventory[]) invList.toArray();
	}

	@Override
	public boolean isLeaf()
	{
		return parent.getConnections().length < 2;
	}
}
