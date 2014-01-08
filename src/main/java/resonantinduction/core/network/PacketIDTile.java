package resonantinduction.core.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketType;

import com.google.common.io.ByteArrayDataInput;

/** Packet handler for blocks and tile entities.
 * 
 * @author Calclavia */
public class PacketIDTile extends PacketType
{
    public PacketIDTile(String channel)
    {
        super(channel);
    }

    public Packet getPacket(TileEntity tileEntity, String id, Object... args)
    {
        return this.getPacket(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, id, args);
    }

    public Packet getPacket(int x, int y, int z, String id, Object... args)
    {
        List newArgs = new ArrayList();

        newArgs.add(x);
        newArgs.add(y);
        newArgs.add(z);
        newArgs.add(id);

        for (Object obj : args)
        {
            newArgs.add(obj);
        }

        return super.getPacket(newArgs.toArray());
    }

    @Override
    public void receivePacket(ByteArrayDataInput data, EntityPlayer player)
    {
        int x = data.readInt();
        int y = data.readInt();
        int z = data.readInt();
        String id = data.readUTF();
        TileEntity tileEntity = player.worldObj.getBlockTileEntity(x, y, z);
        
        if (tileEntity instanceof IPacketReceiver)
        {
            ((IPacketReceiver) tileEntity).onReceivePacket(data, player);
        }
        else
        {
            int blockID = player.worldObj.getBlockId(x, y, z);

            if (Block.blocksList[blockID] instanceof IPacketReceiver)
            {
                ((IPacketReceiver) Block.blocksList[blockID]).onReceivePacket(data, player, x, y, z);
            }
        }
    }
}
