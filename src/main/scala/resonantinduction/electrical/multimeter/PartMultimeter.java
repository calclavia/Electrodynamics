package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.IRemovable;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonant.lib.utility.WrenchUtility;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.core.prefab.part.PartFace;
import resonantinduction.electrical.Electrical;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import resonantinduction.electrical.ElectricalContent;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.compatibility.Compatibility;

/** Block that detects power.
 *
 * @author Calclavia */
public class PartMultimeter extends PartFace implements IRedstonePart, IPacketReceiver, IRemovable.ISneakWrenchable
{

    public enum DetectMode
    {
        NONE("none"),
        LESS_THAN("lessThan"),
        LESS_THAN_EQUAL("lessThanOrEqual"),
        EQUAL("equal"),
        GREATER_THAN_EQUAL("greaterThanOrEqual"),
        GREATER_THAN("greaterThan");

        public String display;

        private DetectMode(String s)
        {
            display = s;
        }
    }

    public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

    /** Detection */
    public double redstoneTriggerLimit;
    public byte detectType = 0;
    public byte graphType = 0;
    private DetectMode detectMode = DetectMode.NONE;
    public boolean redstoneOn;
    private boolean doDetect = true;

    public boolean isPrimary;
    private MultimeterNetwork network;

    public boolean hasMultimeter(int x, int y, int z)
    {
        return getMultimeter(x, y, z) != null;
    }

    @Override
    public void preRemove()
    {
        if (!world().isRemote)
            getNetwork().remove(this);
    }

    public void updateDesc()
    {
        writeDesc(getWriteStream());
    }

    public void updateGraph()
    {
        writeGraph(getWriteStream());
    }

    /** Gets the multimeter on the same plane. */
    public PartMultimeter getMultimeter(int x, int y, int z)
    {
        TileEntity tileEntity = world().getTileEntity(x, y, z);

        if (tileEntity instanceof TileMultipart)
        {
            TMultiPart part = ((TileMultipart) tileEntity).partMap(placementSide.ordinal());

            if (part instanceof PartMultimeter)
            {
                return (PartMultimeter) part;
            }
        }

        return null;
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
    {
        if (WrenchUtility.isUsableWrench(player, player.inventory.getCurrentItem(), x(), y(), z()))
        {
            if (!this.world().isRemote)
            {
                doDetect = !doDetect;
                player.addChatMessage(new ChatComponentText("Multimeter detection set to: " + doDetect));
                WrenchUtility.damageWrench(player, player.inventory.getCurrentItem(), x(), y(), z());
            }
            return true;
        }

        player.openGui(Electrical.INSTANCE(), placementSide.ordinal(), world(), x(), y(), z());
        return true;
    }

    @Override
    public void update()
    {
        super.update();

        this.ticks++;

        if (!world().isRemote)
        {
            if (doDetect)
                updateDetections();

            double detectedValue = getNetwork().graphs.get(detectType).getDouble();

            boolean outputRedstone = false;

            switch (detectMode)
            {
                default:
                    break;
                case EQUAL:
                    outputRedstone = detectedValue == redstoneTriggerLimit;
                    break;
                case GREATER_THAN:
                    outputRedstone = detectedValue > redstoneTriggerLimit;
                    break;
                case GREATER_THAN_EQUAL:
                    outputRedstone = detectedValue >= redstoneTriggerLimit;
                    break;
                case LESS_THAN:
                    outputRedstone = detectedValue < redstoneTriggerLimit;
                    break;
                case LESS_THAN_EQUAL:
                    outputRedstone = detectedValue <= redstoneTriggerLimit;
                    break;
            }

            getNetwork().markUpdate();

            if (ticks % 20 == 0)
            {
                if (outputRedstone != redstoneOn)
                {
                    redstoneOn = outputRedstone;
                    tile().notifyPartChange(this);
                }

                updateGraph();
            }
        }

        if (!world().isRemote)
        {
            for (EntityPlayer player : playersUsing)
            {
                updateGraph();
            }
        }
    }

    public void updateDetections()
    {
        ForgeDirection receivingSide = getDirection().getOpposite();
        TileEntity tileEntity = getDetectedTile();
        //TODO add energy detection

        if (tileEntity instanceof INodeProvider)
        {
            IMechanicalNode instance = (IMechanicalNode) ((INodeProvider) tileEntity).getNode(IMechanicalNode.class, receivingSide);

            for (ForgeDirection dir : ForgeDirection.values())
            {
                if (instance != null)
                {
                    break;
                }

                instance = (IMechanicalNode) ((INodeProvider) tileEntity).getNode(IMechanicalNode.class, dir);
            }

            if (instance != null)
            {
                getNetwork().torqueGraph.queue(instance.getForce(receivingSide));
                getNetwork().angularVelocityGraph.queue(instance.getAngularSpeed(receivingSide));
                getNetwork().powerGraph.queue(instance.getForce(receivingSide) * instance.getAngularSpeed(receivingSide));
            }
        }

        if (tileEntity instanceof IFluidHandler)
        {
            FluidTankInfo[] fluidInfo = ((IFluidHandler) tileEntity).getTankInfo(receivingSide);

            if (fluidInfo != null)
            {
                for (FluidTankInfo info : fluidInfo)
                {
                    if (info != null)
                        if (info.fluid != null)
                            getNetwork().fluidGraph.queue(info.fluid.amount);
                }
            }
        }

        getNetwork().energyGraph.queue(Compatibility.getEnergy(tileEntity, receivingSide));

        /** Update Energy Capacity Graph */
        getNetwork().energyCapacityGraph.queue(Compatibility.getMaxEnergy(tileEntity, receivingSide));
    }

    @Override
    public void readDesc(MCDataInput packet)
    {
        packet.readByte();
        placementSide = ForgeDirection.getOrientation(packet.readByte());
        facing = packet.readByte();
        detectMode = DetectMode.values()[packet.readByte()];
        detectType = packet.readByte();
        graphType = packet.readByte();
        getNetwork().center = new universalelectricity.core.transform.vector.Vector3(packet.readNBTTagCompound());
        getNetwork().size = new universalelectricity.core.transform.vector.Vector3(packet.readNBTTagCompound());
        getNetwork().isEnabled = packet.readBoolean();
    }

    @Override
    public void writeDesc(MCDataOutput packet)
    {
        packet.writeByte(0);
        packet.writeByte(placementSide.ordinal());
        packet.writeByte(facing);
        packet.writeByte(detectMode.ordinal());
        packet.writeByte(detectType);
        packet.writeByte(graphType);
        packet.writeNBTTagCompound(getNetwork().center.writeNBT(new NBTTagCompound()));
        packet.writeNBTTagCompound(getNetwork().size.writeNBT(new NBTTagCompound()));
        packet.writeBoolean(getNetwork().isEnabled);
    }

    public void writeGraph(MCDataOutput packet)
    {
        packet.writeByte(2);
        isPrimary = getNetwork().isPrimary(this);
        packet.writeBoolean(isPrimary);

        if (isPrimary)
            packet.writeNBTTagCompound(getNetwork().save());
    }

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
            {
                placementSide = ForgeDirection.getOrientation(packet.readByte());
                facing = packet.readByte();
                detectMode = DetectMode.values()[packet.readByte()];
                detectType = packet.readByte();
                graphType = packet.readByte();
                getNetwork().center = new universalelectricity.core.transform.vector.Vector3(packet.readNBTTagCompound());
                getNetwork().size = new universalelectricity.core.transform.vector.Vector3(packet.readNBTTagCompound());
                getNetwork().isEnabled = packet.readBoolean();
                break;
            }
            case 1:
            {
                redstoneTriggerLimit = packet.readLong();
                break;
            }
            case 2:
            {
                isPrimary = packet.readBoolean();

                if (isPrimary)
                    getNetwork().load(packet.readNBTTagCompound());
                break;
            }
        }
    }

    @Override
    public List<ItemStack> getRemovedItems(EntityPlayer entity) {
        List<ItemStack> list = new ArrayList<ItemStack>();
        list.add(new ItemStack(ElectricalContent.itemMultimeter()));
        return list;
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType type)
    {
        detectMode = DetectMode.values()[data.readByte()];
        detectType = data.readByte();
        graphType = data.readByte();
        redstoneTriggerLimit = data.readDouble();
    }

    public TileEntity getDetectedTile()
    {
        ForgeDirection direction = getDirection();
        return world().getTileEntity(x() + direction.offsetX, y() + direction.offsetY, z() + direction.offsetZ);
    }

    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(this.placementSide.ordinal());
    }

    public void toggleGraphType()
    {
        graphType = (byte) ((graphType + 1) % getNetwork().graphs.size());
        updateServer();
    }

    public void toggleMode()
    {
        detectMode = DetectMode.values()[(detectMode.ordinal() + 1) % DetectMode.values().length];
        updateServer();
    }

    public void toggleDetectionValue()
    {
        detectType = (byte) ((detectType + 1) % getNetwork().graphs.size());
        updateServer();
    }

    public void updateServer()
    {
        //ResonantEngine.instance.packetHandler.sendToServer(new PACKET_MULTIPART.getPacket(new universalelectricity.core.transform.vector.Vector3(x(), y(), z()), placementSide.ordinal(), (byte) detectMode.ordinal(), detectType, graphType, redstoneTriggerLimit));
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        placementSide = ForgeDirection.getOrientation(nbt.getByte("side"));
        detectMode = DetectMode.values()[nbt.getByte("detectMode")];
        detectType = nbt.getByte("detectionType");
        graphType = nbt.getByte("graphType");
        doDetect = nbt.getBoolean("doDetect");
        redstoneTriggerLimit = nbt.getDouble("triggerLimit");
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setByte("side", (byte) placementSide.ordinal());
        nbt.setByte("detectMode", (byte) detectMode.ordinal());
        nbt.setByte("detectionType", detectType);
        nbt.setByte("graphType", graphType);
        nbt.setBoolean("doDetect", doDetect);
        nbt.setDouble("triggerLimit", redstoneTriggerLimit);
    }

    public DetectMode getMode()
    {
        return detectMode;
    }

    @Override
    public String getType()
    {
        return "resonant_induction_multimeter";
    }

    @Override
    public int redstoneConductionMap()
    {
        return 0x1F;
    }

    @Override
    public boolean solid(int arg0)
    {
        return true;
    }

    @Override
    protected ItemStack getItem()
    {
        return new ItemStack(ElectricalContent.itemMultimeter());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Vector3 pos, float frame, int pass)
    {
        if (pass == 0)
        {
            RenderMultimeter.render(this, pos.x, pos.y, pos.z);
        }
    }

    @Override
    public boolean canConnectRedstone(int arg0)
    {
        return true;
    }

    @Override
    public int strongPowerLevel(int arg0)
    {
        return redstoneOn ? 14 : 0;
    }

    @Override
    public int weakPowerLevel(int arg0)
    {
        return redstoneOn ? 14 : 0;
    }


    public MultimeterNetwork getNetwork()
    {
        if (network == null)
        {
            network = new MultimeterNetwork();
            network.add(this);
        }

        return network;
    }


    public void setNetwork(MultimeterNetwork network)
    {
        this.network = network;
    }


    public boolean canConnect(ForgeDirection direction, Object obj)
    {
        return obj instanceof PartMultimeter;
    }


    public Object[] getConnections()
    {
        Object[] connections = new Object[6];

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            if (dir != getDirection() && dir != getDirection().getOpposite())
            {
                universalelectricity.core.transform.vector.Vector3 vector = getPosition().add(dir);

                if (hasMultimeter(vector.xi(), vector.yi(), vector.zi()))
                {
                    connections[dir.ordinal()] = getMultimeter(vector.xi(), vector.yi(), vector.zi());
                }
            }
        }

        return connections;
    }

    public universalelectricity.core.transform.vector.Vector3 getPosition()
    {
        return new universalelectricity.core.transform.vector.Vector3(x(), y(), z());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Cuboid6 getRenderBounds()
    {
        if (isPrimary)
            return Cuboid6.full.copy().expand(new Vector3(getNetwork().size.x(), getNetwork().size.y(), getNetwork().size.z()));
        return Cuboid6.full;
    }

    @Override
    public String toString()
    {
        return "[PartMultimeter]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
    }

}
