package resonantinduction.core.grid.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import resonant.lib.content.module.TileBase;
import resonant.lib.network.IPacketReceiverWithID;
import resonant.lib.network.PacketHandler;
import resonant.lib.utility.FluidUtility;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

/** A prefab class for tiles that use the fluid network.
 * 
 * @author DarkGuardsman */
public abstract class TileFluidNode extends TileBase implements IPacketReceiverWithID
{
    protected int pressure;

    protected FluidTank tank;

    protected int colorID = 0;

    /** Copy of the tank's content last time it updated */
    protected FluidStack prevStack = null;

    public static final int PACKET_DESCRIPTION = 0;
    public static final int PACKET_RENDER = 1;
    public static final int PACKET_TANK = 2;

    /** Bitmask that handles connections for the renderer **/
    public byte renderSides = 0;

    protected boolean markTankUpdate;

    protected final int tankSize;

    public TileFluidNode(Material material, int tankSize)
    {
        super(material);
        this.tankSize = tankSize;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (markTankUpdate)
        {
            sendTankUpdate();
            markTankUpdate = false;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        colorID = nbt.getInteger("colorID");
        getInternalTank().readFromNBT(nbt.getCompoundTag("FluidTank"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("colorID", colorID);
        nbt.setCompoundTag("FluidTank", getInternalTank().writeToNBT(new NBTTagCompound()));
    }

    @Override
    public boolean onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        try
        {
            if (this.worldObj.isRemote)
            {
                if (id == PACKET_DESCRIPTION)
                {
                    colorID = data.readInt();
                    renderSides = data.readByte();
                    tank = new FluidTank(data.readInt());
                    getInternalTank().readFromNBT(PacketHandler.readNBTTagCompound(data));
                    return true;
                }
                else if (id == PACKET_RENDER)
                {
                    colorID = data.readInt();
                    renderSides = data.readByte();
                    markRender();
                    return true;
                }
                else if (id == PACKET_TANK)
                {
                    tank = new FluidTank(data.readInt()).readFromNBT(PacketHandler.readNBTTagCompound(data));
                    pressure = data.readInt();
                    updateLight();
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        if (getInternalTank().getFluid() == null || (getInternalTank().getFluid() != null && getInternalTank().getFluid().getFluid().getName() != null))
            return ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_DESCRIPTION, this, this.colorID, this.renderSides, getInternalTank().getCapacity(), getInternalTank().writeToNBT(new NBTTagCompound()));
        return null;
    }

    public void sendRenderUpdate()
    {
        if (!this.worldObj.isRemote)
            PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_RENDER, this, this.colorID, this.renderSides));
    }

    public void sendTankUpdate()
    {
        if (!this.worldObj.isRemote)
            PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_TANK, this, getInternalTank().getCapacity(), getInternalTank().writeToNBT(new NBTTagCompound()), pressure), this.worldObj, new Vector3(this), 60);
    }

    public void onFluidChanged()
    {
        if (!worldObj.isRemote)
        {
            if (!FluidUtility.matchExact(prevStack, getInternalTank().getFluid()))
            {
                markTankUpdate = true;
                prevStack = tank.getFluid() != null ? tank.getFluid().copy() : null;
            }
        }
    }

    public FluidTank getInternalTank()
    {
        if (this.tank == null)
        {
            this.tank = new FluidTank(this.tankSize);
        }
        return this.tank;
    }
}
