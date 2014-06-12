package resonantinduction.mechanical.energy.grid;

import java.io.IOException;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.core.ResonantEngine;
import resonant.lib.References;
import resonant.lib.content.module.TileBase;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.IPacketReceiverWithID;
import resonant.lib.network.PacketHandler;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.ControlKeyModifer;

import com.google.common.io.ByteArrayDataInput;

/** Prefab for mechanical tiles
 * 
 * @author Calclavia */
public abstract class TileMechanical extends TileBase implements INodeProvider, IPacketReceiverWithID
{
    protected static final int PACKET_NBT = 0;
    protected static final int PACKET_VELOCITY = 1;

    /** Node that handles most mechanical actions */
    public MechanicalNode mechanicalNode;
    
    /** External debug GUI */
    MechanicalNodeFrame frame = null;

    @Deprecated
    public TileMechanical()
    {
        this(null);
    }

    public TileMechanical(Material material)
    {
        super(material);
        this.mechanicalNode = new MechanicalNode(this);
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
        
        if(frame != null)
        {
            frame.update();
            if(!frame.isVisible())
            {
                frame.dispose();
                frame = null;
            }
        }
        
        if (!this.getWorldObj().isRemote)
        {
            if (ticks % 3 == 0 && (mechanicalNode.markTorqueUpdate || mechanicalNode.markRotationUpdate))
            {
                //ResonantInduction.LOGGER.info("[mechanicalNode] Sending Update");
                sendRotationPacket();
                mechanicalNode.markRotationUpdate = false;
                mechanicalNode.markTorqueUpdate = false;
            }
        }
    }
   
    @Override
    protected boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        ItemStack itemStack = player.getHeldItem();
        if (ResonantEngine.runningAsDev)
        {
            if (itemStack != null && !world().isRemote)
            {
                if (itemStack.getItem().itemID == Item.stick.itemID)
                {
                    //Set the nodes debug mode
                    if (ControlKeyModifer.isControlDown(player))
                    {
                        //Opens a debug GUI
                        if (frame == null)
                        {
                            frame = new MechanicalNodeFrame(this);
                            frame.showDebugFrame();
                        } //Closes the debug GUI
                        else
                        {
                            frame.closeDebugFrame();
                            frame = null;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        if (nodeType.isAssignableFrom(mechanicalNode.getClass()))
            return mechanicalNode;
        return null;
    }
    
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return References.PACKET_TILE.getPacketWithID(PACKET_NBT, this, tag);
    }

    private void sendRotationPacket()
    {
        PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_VELOCITY, this, mechanicalNode.angularVelocity, mechanicalNode.torque), worldObj, new Vector3(this), 20);
    }

    @Override
    public boolean onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        try
        {
            if (world().isRemote)
            {
                if (id == PACKET_NBT)
                {
                    readFromNBT(PacketHandler.readNBTTagCompound(data));
                    return true;
                }
                else if (id == PACKET_VELOCITY)
                {
                    mechanicalNode.angularVelocity = data.readDouble();
                    mechanicalNode.torque = data.readDouble();
                    return true;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return true;
        }
        return false;
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
