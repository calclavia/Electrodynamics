package com.builtbroken.assemblyline.transmit;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.energy.EnergyNetworkLoader;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;
import calclavia.lib.network.PacketHandler;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.network.ISimplePacketReceiver;
import com.builtbroken.minecraft.helpers.ColorCode;
import com.builtbroken.minecraft.helpers.ColorCode.IColorCoded;
import com.builtbroken.minecraft.prefab.TileEntityAdvanced;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@UniversalClass
public class TileEntityWire extends TileEntityAdvanced implements IConductor, ISimplePacketReceiver, IColorCoded
{
    protected int updateTick = 1;
    protected ColorCode color = ColorCode.UNKOWN;

    private IEnergyNetwork network;

    public TileEntity[] connections = new TileEntity[6];

    public byte currentAcceptorConnections = 0x00;

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!worldObj.isRemote)
        {
            if (ticks % this.updateTick == 0)
            {
                this.updateTick = this.worldObj.rand.nextInt(5) * 40 + 20;
                this.refresh();
            }
        }
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        this.getNetwork().split(this);
    }

    public void refresh()
    {
        if (!this.worldObj.isRemote)
        {
            byte possibleAcceptorConnections = 0x00;
            this.connections = new TileEntity[6];

            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), side);
                if (CompatibilityModule.canConnect(tileEntity, side.getOpposite()) && this.canConnect(side))
                {
                    if (tileEntity instanceof IConductor)
                    {
                        this.getNetwork().merge(((IConductor) tileEntity).getNetwork());
                    }
                    this.connections[side.ordinal()] = tileEntity;
                    possibleAcceptorConnections |= 1 << side.ordinal();
                }
            }
            if (this.currentAcceptorConnections != possibleAcceptorConnections)
            {
                this.currentAcceptorConnections = possibleAcceptorConnections;
                PacketHandler.instance().sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 64);
                this.getNetwork().reconstruct();
            }
        }
    }

    public boolean hasConnectionSide(ForgeDirection side)
    {
        return connectionMapContainsSide(this.currentAcceptorConnections, side);
    }

    public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
    {
        byte tester = (byte) (1 << side.ordinal());
        return ((connections & tester) > 0);
    }

    @Override
    public boolean simplePacket(String id, ByteArrayDataInput data, Player player)
    {
        if (id.equalsIgnoreCase("Wire"))
        {
            this.currentAcceptorConnections = data.readByte();
            this.setColor(ColorCode.get(data.readInt()));
            return true;
        }
        return false;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return PacketHandler.instance().getTilePacket(AssemblyLine.CHANNEL, "Wire", this, this.currentAcceptorConnections, this.getColor().ordinal());
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    @Override
    public TileEntity[] getConnections()
    {
        if (this.connections == null)
        {
            this.refresh();
        }
        return this.connections;
    }

    @Override
    public IEnergyNetwork getNetwork()
    {
        if (!(this.network instanceof IEnergyNetwork))
        {
            this.network = EnergyNetworkLoader.getNewNetwork(this);
        }
        return this.network;
    }

    @Override
    public void setNetwork(IEnergyNetwork network)
    {
        this.network = network;
    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return true;
    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        if (this.canConnect(from) && this.getNetwork() != null)
        {
            return this.getNetwork().produce(this, from.getOpposite(), receive, doReceive);
        }
        return 0;
    }

    @Override
    public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
    {
        return 0;
    }

    @Override
    public ColorCode getColor()
    {
        return this.color;
    }

    @Override
    public boolean setColor(Object obj)
    {
        if (ColorCode.get(obj) != null)
        {
            this.color = ColorCode.get(obj);
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    @Override
    public float getResistance()
    {
        return 0.0000000168f;
    }

    @Override
    public long getCurrentCapacity()
    {
        return 1000000;
    }

    /** NBT Data */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("saveBuffer"))
            this.getNetwork().setBufferFor(this, nbt.getLong("saveBuffer"));
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (this.getNetwork().getBufferOf(this) > 0)
            nbt.setLong("saveBuffer", this.getNetwork().getBufferOf(this));
    }
}
