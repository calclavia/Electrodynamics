package resonantinduction.electrical.multimeter;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.grid.INodeProvider;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.utility.WrenchUtility;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.core.prefab.part.PartFace;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.electricity.IElectricalNetwork;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.net.IConnector;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Block that detects power.
 * 
 * @author Calclavia */
public class PartMultimeter extends PartFace implements IConnector<MultimeterNetwork>, IRedstonePart, IPacketReceiver
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
            getNetwork().split(this);
    }

    public void refresh()
    {
        if (world() != null)
        {
            if (!world().isRemote)
            {
                for (Object obj : getConnections())
                {
                    if (obj instanceof PartMultimeter)
                    {
                        getNetwork().merge(((PartMultimeter) obj).getNetwork());
                    }
                }

                getNetwork().reconstruct();
            }
        }
    }

    public void updateDesc()
    {
        writeDesc(getWriteStream());
    }

    public void updateGraph()
    {
        writeGraph(getWriteStream());
    }

    @Override
    public void onWorldJoin()
    {
        refresh();
    }

    @Override
    public void onNeighborChanged()
    {
        refresh();
    }

    @Override
    public void onPartChanged(TMultiPart part)
    {
        refresh();
    }

    /** Gets the multimeter on the same plane. */
    public PartMultimeter getMultimeter(int x, int y, int z)
    {
        TileEntity tileEntity = world().getBlockTileEntity(x, y, z);

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
                player.addChatMessage("Multimeter detection set to: " + doDetect);
                WrenchUtility.damageWrench(player, player.inventory.getCurrentItem(), x(), y(), z());
            }
            return true;
        }

        player.openGui(Electrical.INSTANCE, placementSide.ordinal(), world(), x(), y(), z());
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

        /** Update Energy Graph */
        if (tileEntity instanceof IConductor)
        {
            IConnector<IEnergyNetwork> instance = ((IConductor) tileEntity).getInstance(receivingSide);

            for (ForgeDirection dir : ForgeDirection.values())
            {
                if (instance != null)
                {
                    break;
                }

                instance = ((IConnector) tileEntity).getInstance(dir);
            }

            if (instance != null)
            {
                if (instance.getNetwork() instanceof IEnergyNetwork)
                {
                    IEnergyNetwork network = instance.getNetwork();
                    getNetwork().energyGraph.queue(Math.max(network.getBuffer(), network.getLastBuffer()));
                    getNetwork().powerGraph.queue(getNetwork().energyGraph.getAverage() * 20);

                    if (instance.getNetwork() instanceof IElectricalNetwork)
                        getNetwork().voltageGraph.queue(((IElectricalNetwork) network).getVoltage());
                }
            }
        }

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
                getNetwork().torqueGraph.queue(instance.getTorque());
                getNetwork().angularVelocityGraph.queue(instance.getAngularSpeed());
                getNetwork().powerGraph.queue((long) instance.getPower());
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

        if (tileEntity instanceof IPressureNodeProvider)
        {
            getNetwork().pressureGraph.queue(((FluidPressureNode) ((IPressureNodeProvider) tileEntity).getNode(FluidPressureNode.class, receivingSide)).getPressure(receivingSide));
        }

        getNetwork().energyGraph.queue(CompatibilityModule.getEnergy(tileEntity, receivingSide));

        /** Update Energy Capacity Graph */
        getNetwork().energyCapacityGraph.queue(CompatibilityModule.getMaxEnergy(tileEntity, receivingSide));
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
        getNetwork().center = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
        getNetwork().size = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
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
        packet.writeNBTTagCompound(getNetwork().center.writeToNBT(new NBTTagCompound()));
        packet.writeNBTTagCompound(getNetwork().size.writeToNBT(new NBTTagCompound()));
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
                getNetwork().center = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
                getNetwork().size = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
                getNetwork().isEnabled = packet.readBoolean();
                refresh();
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
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        detectMode = DetectMode.values()[data.readByte()];
        detectType = data.readByte();
        graphType = data.readByte();
        redstoneTriggerLimit = data.readDouble();
    }

    public TileEntity getDetectedTile()
    {
        ForgeDirection direction = getDirection();
        return world().getBlockTileEntity(x() + direction.offsetX, y() + direction.offsetY, z() + direction.offsetZ);
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
        PacketDispatcher.sendPacketToServer(ResonantInduction.PACKET_MULTIPART.getPacket(new universalelectricity.api.vector.Vector3(x(), y(), z()), placementSide.ordinal(), (byte) detectMode.ordinal(), detectType, graphType, redstoneTriggerLimit));
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
        return new ItemStack(Electrical.itemMultimeter);
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

    @Override
    public MultimeterNetwork getNetwork()
    {
        if (network == null)
        {
            network = new MultimeterNetwork();
            network.addConnector(this);
        }

        return network;
    }

    @Override
    public void setNetwork(MultimeterNetwork network)
    {
        this.network = network;
    }

    @Override
    public boolean canConnect(ForgeDirection direction, Object obj)
    {
        return obj instanceof PartMultimeter;
    }

    @Override
    public Object[] getConnections()
    {
        Object[] connections = new Object[6];

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            if (dir != getDirection() && dir != getDirection().getOpposite())
            {
                universalelectricity.api.vector.Vector3 vector = getPosition().translate(dir);

                if (hasMultimeter(vector.intX(), vector.intY(), vector.intZ()))
                {
                    connections[dir.ordinal()] = getMultimeter(vector.intX(), vector.intY(), vector.intZ());
                }
            }
        }

        return connections;
    }

    @Override
    public IConnector<MultimeterNetwork> getInstance(ForgeDirection dir)
    {
        return this;
    }

    public universalelectricity.api.vector.Vector3 getPosition()
    {
        return new universalelectricity.api.vector.Vector3(x(), y(), z());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Cuboid6 getRenderBounds()
    {
        if (isPrimary)
            return Cuboid6.full.copy().expand(new Vector3(getNetwork().size.x, getNetwork().size.y, getNetwork().size.z));
        return Cuboid6.full;
    }

    @Override
    public String toString()
    {
        return "[PartMultimeter]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
    }

}
