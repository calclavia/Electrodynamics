package resonantinduction.core.prefab.node;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketIDReceiver;
import resonantinduction.core.prefab.LimitedTank;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.api.core.grid.ISave;
import universalelectricity.core.grid.node.NodeConnector;


/**
 * Simple tank node designed to be implemented by any machine that can connect to other fluid based machines.
 * @author Darkguardsman
 */
public class NodeTank extends NodeConnector implements IFluidTank, IFluidHandler, ISave, IPacketIDReceiver
{
    LimitedTank tank;
    static final int PACKET_DESCRIPTION = 100, PACKET_TANK = 101;

    public NodeTank(INodeProvider parent)
    {
        this(parent, 1);
    }

    public NodeTank(INodeProvider parent, int buckets)
    {
        super(parent);
        tank = new LimitedTank(buckets * FluidContainerRegistry.BUCKET_VOLUME);
    }

    @Override
    public boolean isValidConnection(Object object)
    {
        return super.isValidConnection(object);
    }

    @Override
    public FluidStack getFluid()
    {
        return tank.getFluid();
    }

    @Override
    public int getFluidAmount()
    {
        return tank.getFluidAmount();
    }

    @Override
    public int getCapacity()
    {
        return tank.getCapacity();
    }

    @Override
    public FluidTankInfo getInfo()
    {
        return tank.getInfo();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        return tank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        return tank.drain(maxDrain, doDrain);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if(canConnect(from))
        {
            return fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if(canConnect(from) && resource != null && resource.isFluidEqual(getFluid()))
        {
            return drain(resource.amount, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        if(canConnect(from))
        {
            return drain(maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return canConnect(from);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return canConnect(from);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[]{getInfo()};
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        tank.readFromNBT(nbt.getCompoundTag("tank"));
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        switch(id)
        {
            case PACKET_DESCRIPTION :
                this.load(ByteBufUtils.readTag(buf));
                break;
            case PACKET_TANK:
                this.tank.readFromNBT(ByteBufUtils.readTag(buf));
                break;
        }
        return false;
    }

    public void sendTank()
    {
        ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile((int)x(), (int)y(), (int)z(), tank.writeToNBT(new NBTTagCompound())), this, 64);
    }

    public void sendUpdate()
    {
        NBTTagCompound tag = new NBTTagCompound();
        save(tag);
        ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile((int)x(), (int)y(), (int)z(), tag), this, 64);
    }

    public void setCapacity(int capacity)
    {
        tank.setCapacity(capacity);
    }

    public int maxInput()
    {
        return tank.maxInput;
    }

    public int maxOutput()
    {
        return tank.maxOutput;
    }
}
