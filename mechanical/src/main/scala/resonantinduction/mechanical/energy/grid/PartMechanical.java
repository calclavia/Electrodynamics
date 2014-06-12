package resonantinduction.mechanical.energy.grid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.core.ResonantEngine;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.ControlKeyModifer;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TFacePart;

/** We assume all the force acting on the gear is 90 degrees.
 * 
 * @author Calclavia */
public abstract class PartMechanical extends JCuboidPart implements JNormalOcclusion, TFacePart, INodeProvider
{
    /** Node that handles mechanical action of the machine */
    public MechanicalNode node;
    protected double prevAngularVelocity;    
    int ticks = 0;
    /** Packets */
    boolean markPacketUpdate = false;
    /** Simple debug external GUI */
    MechanicalNodeFrame frame = null;

    /** Side of the block this is placed on */
    public ForgeDirection placementSide = ForgeDirection.UNKNOWN;

    public int tier;

    public void preparePlacement(int side, int itemDamage)
    {
        this.placementSide = ForgeDirection.getOrientation((byte) (side));
        this.tier = itemDamage;
    }

    @Override
    public void update()
    {
        ticks++;
        if (ticks >= Long.MAX_VALUE)
        {
            ticks = 0;
        }
        
        //Make sure to update on both sides
        this.node.update();
        
        if (!world().isRemote)
        {
            checkClientUpdate();          
        }
        if (frame != null)
        {
            frame.update();
        }
        super.update();
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack itemStack)
    {
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
        return super.activate(player, hit, itemStack);
    }

    public void checkClientUpdate()
    {
        if (Math.abs(prevAngularVelocity - node.angularVelocity) >= 0.1)
        {
            prevAngularVelocity = node.angularVelocity;
            sendRotationPacket();
        }
    }

    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        if (nodeType.isAssignableFrom(node.getClass()))
            return node;
        return null;
    }

    @Override
    public void onWorldJoin()
    {
        node.reconstruct();
    }

    @Override
    public void onWorldSeparate()
    {
        node.deconstruct();
        if (frame != null)
        {
            frame.closeDebugFrame();
        }
    }

    /** Packet Code. */
    public void sendRotationPacket()
    {
        if (world() != null && !world().isRemote)
        {
            getWriteStream().writeByte(1).writeDouble(node.angularVelocity);
        }
    }

    /** Packet Code. */
    @Override
    public void read(MCDataInput packet)
    {
        read(packet, packet.readUByte());
    }

    public void read(MCDataInput packet, int packetID)
    {
        switch (packetID)
        {
            case 0:
                load(packet.readNBTTagCompound());
                break;
            case 1:
                node.angularVelocity = packet.readDouble();
                break;
        }
    }

    @Override
    public void readDesc(MCDataInput packet)
    {
        packet.readByte();
        load(packet.readNBTTagCompound());
    }

    @Override
    public void writeDesc(MCDataOutput packet)
    {
        packet.writeByte(0);
        NBTTagCompound nbt = new NBTTagCompound();
        save(nbt);
        packet.writeNBTTagCompound(nbt);
    }

    @Override
    public int redstoneConductionMap()
    {
        return 0;
    }

    @Override
    public boolean solid(int arg0)
    {
        return true;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        placementSide = ForgeDirection.getOrientation(nbt.getByte("side"));
        tier = nbt.getByte("tier");
        node.load(nbt);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        nbt.setByte("side", (byte) placementSide.ordinal());
        nbt.setByte("tier", (byte) tier);
        node.save(nbt);
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

    public universalelectricity.api.vector.Vector3 getPosition()
    {
        return new universalelectricity.api.vector.Vector3(x(), y(), z());
    }

    @Override
    public String toString()
    {
        return "[PartMech]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
    }
}