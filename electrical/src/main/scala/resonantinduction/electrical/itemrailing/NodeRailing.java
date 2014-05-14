package resonantinduction.electrical.itemrailing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.config.Config;
import resonant.lib.grid.Node;
import resonant.lib.render.EnumColor;
import resonant.lib.type.Pair;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingProvider;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;
import universalelectricity.api.vector.IVectorWorld;
import universalelectricity.api.vector.VectorWorld;

import com.google.common.collect.Lists;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class NodeRailing extends Node<IItemRailingProvider, GridRailing, Object> implements IVectorWorld, IItemRailing
{
	private int maxItemSpeed;
	private byte connectionMap;
	private EnumColor color;

	@Config(category = GridRailing.CATEGORY_RAILING)
	private static int MAX_TICKS_IN_RAILING = 5;

	/** hold a timer here per item */
	private Set<Pair<IItemRailingTransfer, Integer>> itemNodeSet;


	public NodeRailing(IItemRailingProvider parent)
	{
		super(parent);
		this.itemNodeSet = new HashSet<Pair<IItemRailingTransfer, Integer>>();
		this.color = null;
		this.connectionMap = Byte.parseByte("111111", 2);
		this.maxItemSpeed = 20;
	}

	public NodeRailing setConnection(byte connectionMap)
	{
		this.connectionMap = connectionMap;
		return this;
	}

	@Override
	public void update(float deltaTime)
	{
		if (!world().isRemote)
		{
			Iterator<Map.Entry<Object, ForgeDirection>> iterator = new HashMap(getConnections()).entrySet().iterator();

			for (Pair<IItemRailingTransfer, Integer> pair : this.itemNodeSet)
			{
				if (pair.right() <= 0)
				{
					//TODO move to next item railing

				}
				else
				{
					pair.setRight(pair.right() - 1);
				}


			}

			while (iterator.hasNext())
			{
				Map.Entry<Object, ForgeDirection> entry = iterator.next();
				Object obj = entry.getKey();

				if (obj instanceof NodeRailing)
				{

				}
			}

		}
	}

	@Override
	protected GridRailing newGrid()
	{
		return new GridRailing(this, getClass());
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
		return (VectorWorld) parent.getWorldPos();
	}

	@Override
	public Map<Object, ForgeDirection> getConnectionMap()
	{
		return new HashMap<Object, ForgeDirection>(this.getConnections());
	}

	@Override
	public IInventory[] getInventoriesNearby()
	{
		ArrayList<IInventory> invList = Lists.<IInventory>newArrayList();
		for (Object tile : this.getConnections().entrySet())
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
		return connectionMap < 2;
	}


}
