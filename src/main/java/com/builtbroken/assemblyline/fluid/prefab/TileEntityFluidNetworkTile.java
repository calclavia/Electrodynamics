package com.builtbroken.assemblyline.fluid.prefab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
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

import org.bouncycastle.util.Arrays;

import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.PacketHandler;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.api.fluid.FluidMasterList;
import com.builtbroken.assemblyline.api.fluid.INetworkFluidPart;
import com.builtbroken.assemblyline.fluid.network.NetworkFluidTiles;
import com.builtbroken.assemblyline.fluid.pipes.FluidPartsMaterial;
import com.builtbroken.assemblyline.network.ISimplePacketReceiver;
import com.builtbroken.minecraft.tilenetwork.INetworkPart;
import com.builtbroken.minecraft.tilenetwork.ITileNetwork;
import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class TileEntityFluidNetworkTile extends TileEntityFluidDevice implements INetworkFluidPart, ISimplePacketReceiver
{
    private int updateTick = 1;
    public static int refreshRate = 10;
    protected FluidTank tank;
    protected FluidTankInfo[] internalTanksInfo = new FluidTankInfo[1];
    protected List<TileEntity> connectedBlocks = new ArrayList<TileEntity>();
    public boolean[] renderConnection = new boolean[6];
    protected int heat = 0, maxHeat = 20000;
    protected int damage = 0, maxDamage = 1000;
    protected int subID = 0;
    protected int tankCap;
    protected FluidStack prevStack = null;

    protected NetworkFluidTiles network;

    public TileEntityFluidNetworkTile()
    {
        this(1);
    }

    public TileEntityFluidNetworkTile(int tankCap)
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
            if (ticks % TileEntityFluidNetworkTile.refreshRate == 0)
            {
                if (this.getTank().getFluid() == null && this.prevStack == null)
                {
                    //Do nothing
                }
                else if ((this.getTank().getFluid() == null && this.prevStack != null) || (this.getTank().getFluid() != null && this.prevStack == null) || (this.getTank().getFluid().amount != this.prevStack.amount))
                {
                    this.sendTankUpdate(0);
                }
                this.prevStack = this.tank.getFluid();
            }
        }
    }

    @Override
    public void invalidate()
    {
        this.getTileNetwork().splitNetwork(this);
        super.invalidate();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (this.getTileNetwork() != null && resource != null)
        {
            return this.getTileNetwork().fillNetworkTank(this, resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (this.getTileNetwork() != null && resource != null)
        {
            if (this.getTileNetwork().getNetworkTank() != null && this.getTileNetwork().getNetworkTank().getFluid() != null && this.getTileNetwork().getNetworkTank().getFluid().isFluidEqual(resource))
            {
                return this.getTileNetwork().drainNetworkTank(this.worldObj, resource.amount, doDrain);
            }

        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        if (this.getTileNetwork() != null)
        {
            return this.getTileNetwork().drainNetworkTank(this.worldObj, maxDrain, doDrain);
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
        return new FluidTankInfo[] { this.getTileNetwork().getNetworkTankInfo() };
    }

    @Override
    public List<TileEntity> getNetworkConnections()
    {
        return this.connectedBlocks;
    }

    @Override
    public void refresh()
    {
        if (this.worldObj != null && !this.worldObj.isRemote)
        {
            boolean[] previousConnections = this.renderConnection.clone();
            this.connectedBlocks.clear();
            this.renderConnection = new boolean[6];

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                this.validateConnectionSide(new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj), dir);

            }
            /** Only send packet updates if visuallyConnected changed. */
            if (!Arrays.areEqual(previousConnections, this.renderConnection))
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
            if (tileEntity instanceof INetworkFluidPart)
            {
                if (this.canTileConnect(Connection.NETWORK, side.getOpposite()))
                {
                    this.getTileNetwork().mergeNetwork(((INetworkFluidPart) tileEntity).getTileNetwork(), (INetworkPart) tileEntity);
                    this.renderConnection[side.ordinal()] = true;
                    connectedBlocks.add(tileEntity);
                }
            }
        }
    }

    @Override
    public NetworkFluidTiles getTileNetwork()
    {
        if (!(this.network instanceof NetworkFluidTiles))
        {
            this.network = new NetworkFluidTiles(this);
        }
        return this.network;
    }

    @Override
    public void setTileNetwork(ITileNetwork fluidNetwork)
    {
        if (fluidNetwork instanceof NetworkFluidTiles)
        {
            this.network = (NetworkFluidTiles) fluidNetwork;
        }

    }

    @Override
    public FluidTankInfo[] getTankInfo()
    {
        if (this.internalTanksInfo == null)
        {
            this.internalTanksInfo = new FluidTankInfo[] { this.getTank().getInfo() };
        }
        return this.internalTanksInfo;
    }

    @Override
    public int fillTankContent(int index, FluidStack stack, boolean doFill)
    {
        if (index == 0)
        {
            int p = this.getTank().getFluid() != null ? this.getTank().getFluid().amount : 0;
            int fill = this.getTank().fill(stack, doFill);
            if (p != fill && doFill)
            {
                this.internalTanksInfo[index] = this.getTank().getInfo();
            }
            return fill;
        }
        return 0;
    }

    @Override
    public FluidStack drainTankContent(int index, int volume, boolean doDrain)
    {
        if (index == 0)
        {
            FluidStack prev = this.getTank().getFluid();
            FluidStack stack = this.getTank().drain(volume, doDrain);
            if (prev != null && (stack == null || prev.amount != stack.amount) && doDrain)
            {
                this.internalTanksInfo[index] = this.getTank().getInfo();
            }
            return stack;
        }
        return null;
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
    public boolean canPassThrew(FluidStack fluid, ForgeDirection from, ForgeDirection to)
    {
        return this.connectedBlocks.get(from.ordinal()) != null && this.connectedBlocks.get(to.ordinal()) != null && this.damage < this.maxDamage;
    }

    @Override
    public boolean onPassThrew(FluidStack fluid, ForgeDirection from, ForgeDirection to)
    {
        FluidPartsMaterial mat = FluidPartsMaterial.get(this.getBlockMetadata());
        if (fluid != null && fluid.getFluid() != null && mat != null)
        {
            if (fluid.getFluid().isGaseous(fluid) && !mat.canSupportGas)
            {
                //TODO lose 25% of the gas, and render the escaping gas as a particle effect
                this.getTileNetwork().drainNetworkTank(this.worldObj, (int) (fluid.amount * .05), true);
            }
            else if (FluidMasterList.isMolten(fluid.getFluid()) && !mat.canSupportMoltenFluids)
            {
                //TODO start to heat up the pipe to melting point. When it hits melting point turn the pipe to its molten metal equal
                //TODO also once it reaches a set heat level start burning up blocks around the pipe such as wood
                // this.heat += FluidMasterList.getHeatPerPass(fluid.getFluid());
                if (heat >= this.maxHeat)
                {
                    this.worldObj.setBlock(xCoord, yCoord, zCoord, Block.fire.blockID);
                    return true;
                }
            }
            else if (!fluid.getFluid().isGaseous(fluid) && !mat.canSupportFluids)
            {
                this.damage += 1;
                if (this.damage >= this.maxDamage)
                {
                    //TODO test this and make sure its right, as well black fluid block in some cases
                    this.getBlockType().dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, 0, 0);
                    this.worldObj.setBlock(xCoord, yCoord, zCoord, 0);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.damage = nbt.getInteger("damage");
        this.heat = nbt.getInteger("heat");
        this.subID = nbt.getInteger("subID");
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
        nbt.setInteger("subID", this.subID);
        nbt.setCompoundTag("FluidTank", this.getTank().writeToNBT(new NBTTagCompound()));
    }

    @Override
    public boolean simplePacket(String id, ByteArrayDataInput data, Player player)
    {
        try
        {
            if (this.worldObj.isRemote)
            {
                if (id.equalsIgnoreCase("DescriptionPacket"))
                {
                    this.subID = data.readInt();
                    this.renderConnection[0] = data.readBoolean();
                    this.renderConnection[1] = data.readBoolean();
                    this.renderConnection[2] = data.readBoolean();
                    this.renderConnection[3] = data.readBoolean();
                    this.renderConnection[4] = data.readBoolean();
                    this.renderConnection[5] = data.readBoolean();
                    this.tank = new FluidTank(data.readInt());
                    this.getTank().readFromNBT(PacketHandler.readNBTTagCompound(data));
                    this.internalTanksInfo[0] = this.getTank().getInfo();
                    return true;
                }
                else if (id.equalsIgnoreCase("RenderPacket"))
                {
                    this.subID = data.readInt();
                    this.renderConnection[0] = data.readBoolean();
                    this.renderConnection[1] = data.readBoolean();
                    this.renderConnection[2] = data.readBoolean();
                    this.renderConnection[3] = data.readBoolean();
                    this.renderConnection[4] = data.readBoolean();
                    this.renderConnection[5] = data.readBoolean();
                    return true;
                }
                else if (id.equalsIgnoreCase("SingleTank"))
                {
                    this.tank = new FluidTank(data.readInt());
                    this.getTank().readFromNBT(PacketHandler.readNBTTagCompound(data));
                    this.internalTanksInfo[0] = this.getTank().getInfo();
                    return true;
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("// Fluid Mechanics Tank packet read error");
            e.printStackTrace();
            return true;
        }
        return false;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        Object[] data = new Object[9];
        data[0] = this.subID;
        data[1] = this.renderConnection[0];
        data[2] = this.renderConnection[1];
        data[3] = this.renderConnection[2];
        data[4] = this.renderConnection[3];
        data[5] = this.renderConnection[4];
        data[6] = this.renderConnection[5];
        data[7] = this.getTank().getCapacity();
        data[8] = this.getTank().writeToNBT(new NBTTagCompound());
        return AssemblyLine.getTilePacket().getPacket(this, "DescriptionPacket", data);
    }

    public void sendRenderUpdate()
    {
        Object[] data = new Object[7];
        data[0] = this.subID;
        data[1] = this.renderConnection[0];
        data[2] = this.renderConnection[1];
        data[3] = this.renderConnection[2];
        data[4] = this.renderConnection[3];
        data[5] = this.renderConnection[4];
        data[6] = this.renderConnection[5];
        PacketHandler.sendPacketToClients(AssemblyLine.getTilePacket().getPacket(this, "RenderPacket", data));
    }

    public void sendTankUpdate(int index)
    {
        if (this.getTank() != null && index == 0)
        {
            PacketHandler.sendPacketToClients(AssemblyLine.getTilePacket().getPacket(this, "SingleTank", this.getTank().getCapacity(), this.getTank().writeToNBT(new NBTTagCompound())), this.worldObj, new Vector3(this), 60);
        }
    }

    @Override
    public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
    {
        if (tool == EnumTools.PIPE_GUAGE)
        {
            String out = "Debug: " + this.getTileNetwork().toString();
            out += "   ";
            for (boolean b : this.renderConnection)
            {
                out += "|" + (b ? "T" : "F");
            }
            return out + "   Vol: " + this.getTileNetwork().getNetworkTank().getFluidAmount();
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getAABBPool().getAABB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
    }

    public int getSubID()
    {
        return this.subID;
    }

    public void setSubID(int id)
    {
        this.subID = id;
    }

}
