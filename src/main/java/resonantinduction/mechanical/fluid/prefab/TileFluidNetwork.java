package resonantinduction.mechanical.fluid.prefab;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.fluid.network.FluidNetwork;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.utility.FluidUtility;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileFluidNetwork extends TileEntityFluidDevice implements IFluidPart, IPacketReceiver
{
    public static int refreshRate = 10;
    protected FluidTank tank;
    protected FluidTankInfo[] internalTanksInfo = new FluidTankInfo[1];
    protected Object[] connectedBlocks = new Object[6];
    protected int heat = 0, maxHeat = 20000;
    protected int damage = 0, maxDamage = 1000;
    protected int colorID = 0;
    protected int tankCap;
    protected FluidStack prevStack = null;
    protected IFluidNetwork network;

    public static final int PACKET_DESCRIPTION = Mechanical.contentRegistry.getNextPacketID();
    public static final int PACKET_RENDER = Mechanical.contentRegistry.getNextPacketID();
    public static final int PACKET_TANK = Mechanical.contentRegistry.getNextPacketID();

    /** Bitmask **/
    public byte renderSides = 0b0;

    public boolean updateFluidRender = false;

    public TileFluidNetwork()
    {
        this(1);
    }

    public TileFluidNetwork(int tankCap)
    {
        if (tankCap <= 0)
        {
            tankCap = 1;
        }
        this.tankCap = tankCap;
        this.tank = new FluidTank(this.tankCap * FluidContainerRegistry.BUCKET_VOLUME);
        this.internalTanksInfo[0] = this.tank.getInfo();
    }

    public FluidTank getTank()
    {
        if (tank == null)
        {
            this.tank = new FluidTank(this.tankCap * FluidContainerRegistry.BUCKET_VOLUME);
            this.internalTanksInfo[0] = this.tank.getInfo();
        }
        return tank;
    }

    @Override
    public void initiate()
    {
        super.initiate();
        this.refresh();
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (!worldObj.isRemote)
        {
            if (this.updateFluidRender && ticks % TileFluidNetwork.refreshRate == 0)
            {
                if (!FluidUtility.matchExact(prevStack, this.getTank().getFluid()))
                {
                    this.sendTankUpdate(0);
                }

                this.prevStack = this.tank.getFluid();
                this.updateFluidRender = false;
            }
        }
    }

    @Override
    public void onFluidChanged()
    {
        this.updateFluidRender = true;
    }

    @Override
    public void invalidate()
    {
        this.getNetwork().split(this);
        super.invalidate();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (this.getNetwork() != null && resource != null)
        {
            return this.getNetwork().fill(this, from, resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (this.getNetwork() != null && resource != null)
        {
            return this.getNetwork().drain(this, from, resource, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        if (this.getNetwork() != null)
        {
            return this.getNetwork().drain(this, from, maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return this.getNetwork().getTankInfo();
    }

    @Override
    public Object[] getConnections()
    {
        return this.connectedBlocks;
    }

    public void refresh()
    {
        if (this.worldObj != null && !this.worldObj.isRemote)
        {
            byte previousConnections = renderSides;
            this.connectedBlocks = new Object[6];
            this.renderSides = 0;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                this.validateConnectionSide(new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj), dir);

            }
            /** Only send packet updates if visuallyConnected changed. */
            if (previousConnections != renderSides)
            {
                this.sendRenderUpdate();
            }
        }

    }

    /** Checks to make sure the connection is valid to the tileEntity
     * 
     * @param tileEntity - the tileEntity being checked
     * @param side - side the connection is too */
    public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
    {
        if (!this.worldObj.isRemote)
        {
            if (tileEntity instanceof IFluidPart)
            {
                if (this.canTileConnect(Connection.NETWORK, side.getOpposite()))
                {
                    this.getNetwork().merge(((IFluidPart) tileEntity).getNetwork());
                    this.setRenderSide(side, true);
                    connectedBlocks[side.ordinal()] = tileEntity;
                }
            }
        }
    }

    public void setRenderSide(ForgeDirection direction, boolean doRender)
    {
        if (doRender)
        {
            renderSides = (byte) (renderSides | (1 << direction.ordinal()));
        }
        else
        {
            renderSides = (byte) (renderSides & ~(1 << direction.ordinal()));

        }
    }

    public boolean canRenderSide(ForgeDirection direction)
    {
        return (renderSides & (1 << direction.ordinal())) != 0;
    }

    @Override
    public IFluidNetwork getNetwork()
    {
        if (this.network != null)
        {
            this.network = new FluidNetwork();
            this.network.addConnector(this);
        }
        return this.network;
    }

    @Override
    public void setNetwork(IFluidNetwork fluidNetwork)
    {
        this.network = fluidNetwork;
    }

    @Override
    public boolean canTileConnect(Connection type, ForgeDirection dir)
    {
        if (this.damage >= this.maxDamage)
        {
            return false;
        }
        return type == Connection.FLUIDS || type == Connection.NETWORK;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.damage = nbt.getInteger("damage");
        this.heat = nbt.getInteger("heat");
        this.colorID = nbt.getInteger("subID");
        if (nbt.hasKey("stored"))
        {
            NBTTagCompound tag = nbt.getCompoundTag("stored");
            String name = tag.getString("LiquidName");
            int amount = nbt.getInteger("Amount");
            Fluid fluid = FluidRegistry.getFluid(name);
            if (fluid != null)
            {
                FluidStack liquid = new FluidStack(fluid, amount);
                this.getTank().setFluid(liquid);
                internalTanksInfo[0] = this.getTank().getInfo();
            }
        }
        else
        {
            this.getTank().readFromNBT(nbt.getCompoundTag("FluidTank"));
            internalTanksInfo[0] = this.getTank().getInfo();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("damage", this.damage);
        nbt.setInteger("heat", this.heat);
        nbt.setInteger("subID", this.colorID);
        nbt.setCompoundTag("FluidTank", this.getTank().writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        try
        {
            if (this.worldObj.isRemote)
            {
                int readInt = data.readInt();

                if (readInt == PACKET_DESCRIPTION)
                {
                    this.colorID = data.readInt();
                    this.renderSides = data.readByte();
                    this.tank = new FluidTank(data.readInt());
                    this.getTank().readFromNBT(PacketHandler.readNBTTagCompound(data));
                    this.internalTanksInfo[0] = this.getTank().getInfo();
                }
                else if (readInt == PACKET_RENDER)
                {
                    this.colorID = data.readInt();
                    this.renderSides = data.readByte();
                }
                else if (readInt == PACKET_TANK)
                {
                    this.tank = new FluidTank(data.readInt());
                    this.getTank().readFromNBT(PacketHandler.readNBTTagCompound(data));
                    this.internalTanksInfo[0] = this.getTank().getInfo();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return ResonantInduction.PACKET_TILE.getPacket(this, PACKET_DESCRIPTION, this.colorID, this.renderSides, this.getTank().getCapacity(), this.getTank().writeToNBT(new NBTTagCompound()));
    }

    public void sendRenderUpdate()
    {
        PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, PACKET_RENDER, this.colorID, this.renderSides));
    }

    public void sendTankUpdate(int index)
    {
        if (this.getTank() != null && index == 0)
        {
            PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, PACKET_TANK, this.getTank().getCapacity(), this.getTank().writeToNBT(new NBTTagCompound())), this.worldObj, new Vector3(this), 60);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getAABBPool().getAABB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
    }

    public int getSubID()
    {
        return this.colorID;
    }

    public void setSubID(int id)
    {
        this.colorID = id;
    }

    public static boolean canRenderSide(byte renderSides, ForgeDirection direction)
    {
        return (renderSides & (1 << direction.ordinal())) != 0;
    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return true;
    }

    @Override
    public FluidTank getInternalTank()
    {
        return this.tank;
    }

}
