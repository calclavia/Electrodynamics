package resonantinduction.electrical.itemrailing;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.grid.Node;
import resonant.lib.grid.TickingGrid;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingProvider;
import universalelectricity.api.vector.IVector3;
import universalelectricity.core.transform.vector.Vector3;

/**
 * @since 25/05/14
 * @author tgame14
 */
public class NodeRailing extends Node<IItemRailingProvider, GridRailing, Object>
{

    protected byte connectionMap = Byte.parseByte("111111", 2);

    public NodeRailing (IItemRailingProvider parent)
    {
        super(parent);
    }

    public NodeRailing setConnection(byte connectionMap)
    {
        this.connectionMap = connectionMap;
        return this;
    }

    @Override
    protected GridRailing newGrid ()
    {
        return new GridRailing(this, NodeRailing.class);
    }

    @Override
    public void update (float deltaTime)
    {
        super.update(deltaTime);
    }

    @Override
    protected void doRecache ()
    {
        connections.clear();

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            TileEntity tile = new Vector3(position()).add(dir).getTileEntity(world());

            if (tile instanceof IItemRailingProvider)
            {
                NodeRailing check = (NodeRailing) ((IItemRailingProvider) tile).getNode(NodeRailing.class, dir.getOpposite());

                if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite(), this))
                {
                    connections.put(check, dir);
                }
            }
        }
    }

    public IVector3 position()
    {
        return parent.getVectorWorld();
    }

    public World world()
    {
        return parent.getVectorWorld().world();
    }

    @Override
    public boolean canConnect (ForgeDirection from, Object source)
    {
        return (source instanceof NodeRailing) && (connectionMap & (1 << from.ordinal())) != 0;
    }

    @Override
    public void load (NBTTagCompound nbt)
    {
        super.load(nbt);
    }

    @Override
    public void save (NBTTagCompound nbt)
    {
        super.save(nbt);
    }
}
