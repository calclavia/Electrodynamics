package resonantinduction.electrical.battery;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;

import com.google.common.io.ByteArrayDataInput;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;

import java.util.ArrayList;

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
        super(Material.iron);
        setTextureName("material_metal_side");
        this.ioMap_$eq((short) 0);
        this.saveIOMap_$eq(true);
        this.normalRender(false);
        this.isOpaqueCube(false);
        this.itemBlock(ItemBlockBattery.class);

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
        this.energy().setEnergy(data.readLong());
        this.ioMap_$eq(data.readShort());
    }

    @Override
    public void setIO(ForgeDirection dir, int type)
    {
        super.setIO(dir, type);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void onPlaced(EntityLivingBase entityliving, ItemStack itemStack)
    {
        if (!world().isRemote && itemStack.getItem() instanceof ItemBlockBattery)
        {
            energy().setCapacity(TileBattery.getEnergyForTier(ItemBlockBattery.getTier(itemStack)));
            energy().setEnergy(((ItemBlockBattery)itemStack.getItem()).getEnergy(itemStack));
            world().setBlockMetadataWithNotify(x(), y(), z(), ItemBlockBattery.getTier(itemStack), 3);
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        ItemStack itemStack = new ItemStack(getBlockType(), 1);
        ItemBlockBattery itemBlock = (ItemBlockBattery) itemStack.getItem();
        ItemBlockBattery.setTier(itemStack, (byte) world().getBlockMetadata(x(), y(), z()));
        itemBlock.setEnergy(itemStack, energy().getEnergy());
        ret.add(itemStack);
        return ret;
    }

    @Override
    public String toString()
    {
        return "[TileBattery]" + x() + "x " + y() + "y " + z() + "z ";
    }
}
