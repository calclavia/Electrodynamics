package resonantinduction.mechanical.energy.grid;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.content.module.TileBase;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

/** Prefab for mechanical tiles
 * 
 * @author Calclavia */
public abstract class TileMechanical extends TileBase implements INodeProvider, IPacketReceiver
{
    protected static final int PACKET_VELOCITY = 1;

    /** Node that handles most mechanical actions */
    public MechanicalNode mechanicalNode;

    @Deprecated
    public TileMechanical()
    {
        this(null);
    }

    public TileMechanical(Material material)
    {
        super(material);
        mechanicalNode = new MechanicalNode(this).setLoad(0.5f);
    }

    @Override
    public void initiate()
    {
        mechanicalNode.reconstruct();
        super.initiate();
    }

    @Override
    public void invalidate()
    {
        mechanicalNode.deconstruct();
        super.invalidate();
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        mechanicalNode.update();
        if (!this.getWorldObj().isRemote)
        {
            if (mechanicalNode.markRotationUpdate && ticks % 10 == 0)
            {
                sendRotationPacket();
                mechanicalNode.markRotationUpdate = false;
            }
        }
    }

    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        if (nodeType.isAssignableFrom(mechanicalNode.getClass()))
            return mechanicalNode;
        return null;
    }

    private void sendRotationPacket()
    {
        PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, PACKET_VELOCITY, mechanicalNode.angularVelocity), worldObj, new Vector3(this), 20);
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        onReceivePacket(data.readInt(), data, player, extra);
    }

    public void onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        if (id == PACKET_VELOCITY)
            mechanicalNode.angularVelocity = data.readDouble();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        mechanicalNode.load(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        mechanicalNode.save(nbt);
    }
}
