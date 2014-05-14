package resonantinduction.electrical.battery;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.IPacketSender;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyInterface;

import com.google.common.io.ByteArrayDataInput;

/** A modular battery box that allows shared connections with boxes next to it.
 * 
 * @author Calclavia */
public class TileBattery extends TileEnergyDistribution implements IVoltageInput, IVoltageOutput, IPacketSender, IPacketReceiver, IEnergyInterface, IEnergyContainer
{
    /** Tiers: 0, 1, 2 */
    public static final int MAX_TIER = 2;

    /** The transfer rate **/
    public static final long DEFAULT_WATTAGE = getEnergyForTier(0);

    public TileBattery()
    {
        this.setEnergyHandler(new EnergyStorageHandler(0));
        this.getEnergyHandler().setCapacity(Long.MAX_VALUE);
        this.ioMap = 0;
        this.saveIOMap = true;
    }

    /** @param tier - 0, 1, 2
     * @return */
    public static long getEnergyForTier(int tier)
    {
        return Math.round(Math.pow(500000000, (tier / (MAX_TIER + 0.7f)) + 1) / (500000000)) * (500000000);
    }

    @Override
    public void initiate()
    {
        super.initiate();
        getEnergyHandler().setCapacity(getEnergyForTier(getBlockMetadata()));
        getEnergyHandler().setMaxTransfer(getEnergyHandler().getEnergyCapacity());
    }

    @Override
    public void updateEntity()
    {
        if (!this.worldObj.isRemote)
        {
            markDistributionUpdate |= produce() > 0;
        }

        super.updateEntity();
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return ResonantInduction.PACKET_TILE.getPacket(this, getPacketData(0).toArray());
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        getEnergyHandler().setEnergy(data.readLong());
        ioMap = data.readShort();
    }

    @Override
    public ArrayList getPacketData(int type)
    {
        ArrayList data = new ArrayList();
        data.add(renderEnergyAmount);
        data.add(ioMap);
        return data;
    }

    @Override
    public long getVoltageOutput(ForgeDirection side)
    {
        return UniversalElectricity.DEFAULT_VOLTAGE;
    }

    @Override
    public long getVoltageInput(ForgeDirection direction)
    {
        return UniversalElectricity.DEFAULT_VOLTAGE;
    }

    @Override
    public void onWrongVoltage(ForgeDirection direction, long voltage)
    {

    }

    @Override
    public void setIO(ForgeDirection dir, int type)
    {
        super.setIO(dir, type);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public String toString()
    {
        return "[TileBattery]" + x() + "x " + y() + "y " + z() + "z ";
    }
}
