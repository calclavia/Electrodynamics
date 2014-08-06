package resonantinduction.electrical.battery;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;

import com.google.common.io.ByteArrayDataInput;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;

/** A modular battery box that allows shared connections with boxes next to it.
 * 
 * @author Calclavia */
public class TileBattery extends TileEnergyDistribution implements IPacketReceiver
{
    /** Tiers: 0, 1, 2 */
    public static final int MAX_TIER = 2;

    /** The transfer rate **/
    public static final long DEFAULT_WATTAGE = getEnergyForTier(0);

    public TileBattery()
    {
        this.ioMap_$eq((short) 0);
        this.saveIOMap_$eq(true);
    }

    /** @param tier - 0, 1, 2
     * @return */
    public static long getEnergyForTier(int tier)
    {
        return Math.round(Math.pow(500000000, (tier / (MAX_TIER + 0.7f)) + 1) / (500000000)) * (500000000);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, renderEnergyAmount, ioMap()));
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType type)
    {
        this.electricNode().energy().setEnergy(data.readLong());
        this.ioMap_$eq(data.readShort());
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
