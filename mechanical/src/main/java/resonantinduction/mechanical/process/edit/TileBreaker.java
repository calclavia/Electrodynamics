package resonantinduction.mechanical.process.edit;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.content.module.TileBase;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.utility.inventory.InternalInventoryHandler;

import com.google.common.io.ByteArrayDataInput;

/** @author tgame14
 * @since 18/03/14 */
public class TileBreaker extends TileBase implements IRotatable, IPacketReceiver
{
    private boolean doWork = false;
    private InternalInventoryHandler invHandler;

    public TileBreaker()
    {
        super(Material.iron);
    }

    public InternalInventoryHandler getInvHandler()
    {
        if (invHandler == null)
        {
            invHandler = new InternalInventoryHandler(this);
        }
        return invHandler;
    }

    @Override
    public void onAdded()
    {
        work();
    }

    @Override
    public void onNeighborChanged()
    {
        work();
    }

    @Override
    public void updateEntity()
    {
        if (doWork)
        {
            doWork();
            doWork = false;
        }
    }

    public void work()
    {
        if (isIndirectlyPowered())
        {
            doWork = true;
        }
    }

    public void doWork()
    {
        if (isIndirectlyPowered())
        {
            ForgeDirection dir = getDirection();
            Vector3 check = position().translate(dir);
            VectorWorld put = (VectorWorld) position().translate(dir.getOpposite());

            Block block = Block.blocksList[check.getBlockID(world())];

            if (block != null)
            {
                int candidateMeta = world().getBlockMetadata(check.intX(), check.intY(), check.intZ());
                boolean flag = true;

                ArrayList<ItemStack> drops = block.getBlockDropped(getWorldObj(), check.intX(), check.intY(), check.intZ(), candidateMeta, 0);

                for (ItemStack stack : drops)
                {
                    ItemStack insert = stack.copy();
                    insert = getInvHandler().storeItem(insert, this.getDirection().getOpposite());
                    if (insert != null)
                    {
                        getInvHandler().throwItem(this.getDirection().getOpposite(), insert);
                    }
                }
                getWorldObj().destroyBlock(check.intX(), check.intY(), check.intZ(), false);

            }
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        try
        {
            readFromNBT(PacketHandler.readNBTTagCompound(data));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
