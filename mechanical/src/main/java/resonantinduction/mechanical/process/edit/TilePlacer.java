package resonantinduction.mechanical.process.edit;

import calclavia.lib.content.module.TileRender;
import calclavia.lib.content.module.prefab.TileInventory;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.render.RenderItemOverlayUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import org.lwjgl.opengl.GL11;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;

/**
 * @author tgame14
 * @since 18/03/14
 */
public class TilePlacer extends TileInventory implements IRotatable, IPacketReceiver
{
    private boolean doWork = false;

    public TilePlacer ()
    {
        super(Material.iron);
        normalRender = false;
    }

    @Override
    public void onAdded ()
    {
        work();
    }

    @Override
    public void onNeighborChanged ()
    {
        work();
    }

    @Override
    public void updateEntity ()
    {
        if (doWork)
        {
            doWork();
            doWork = false;
        }
    }

    public void work ()
    {
        if (isIndirectlyPowered())
        {
            doWork = true;
        }
    }

    public void doWork ()
    {
        ForgeDirection dir = getDirection();
        Vector3 placePos = position().translate(dir);

        if (world().isAirBlock(placePos.intX(), placePos.intY(), placePos.intZ()))
        {

            if (getStackInSlot(0) == null)
            {
                ForgeDirection op = dir.getOpposite();
                TileEntity tile = getWorldObj().getBlockTileEntity(x() + op.offsetX, y() + op.offsetY, z() + op.offsetZ);

                if (tile instanceof IInventory)
                {
                    ItemStack candidate = InventoryUtility.takeTopBlockFromInventory((IInventory) tile, dir.ordinal());
                    if (candidate != null)
                    {
                        incrStackSize(0, candidate);
                    }
                }
            }

            ItemStack placeStack = getStackInSlot(0);

            if (placeStack != null && placeStack.getItem() instanceof ItemBlock)
            {
                ItemBlock itemBlock = ((ItemBlock) placeStack.getItem());

                try
                {
                    itemBlock.placeBlockAt(placeStack, null, world(), placePos.intX(), placePos.intY(), placePos.intZ(), 0, 0, 0, 0, 0);
                }
                catch (Exception e)
                {
                    //	e.printStackTrace();
                }

                decrStackSize(0, 1);
                markUpdate();
            }
        }
    }

    @Override
    protected boolean use (EntityPlayer player, int hitSide, Vector3 hit)
    {
        interactCurrentItem(this, 0, player);
        return true;
    }

    @Override
    public Packet getDescriptionPacket ()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
    }

    @Override
    public void onReceivePacket (ByteArrayDataInput data, EntityPlayer player, Object... extra)
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

    @Override
    @SideOnly(Side.CLIENT)
    protected TileRender newRenderer ()
    {
        return new TileRender()
        {
            @Override
            public boolean renderDynamic (Vector3 position, boolean isItem, float frame)
            {
                if (!isItem)
                {
                    GL11.glPushMatrix();
                    RenderItemOverlayUtility.renderItemOnSides(TilePlacer.this, getStackInSlot(0), position.x, position.y, position.z);
                    GL11.glPopMatrix();
                }

                return false;
            }
        };
    }
}

