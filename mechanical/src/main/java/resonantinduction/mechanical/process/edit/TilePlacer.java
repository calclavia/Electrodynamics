package resonantinduction.mechanical.process.edit;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.content.module.TileRender;
import calclavia.lib.content.module.prefab.TileInventory;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.render.RenderItemOverlayUtility;
import calclavia.lib.utility.LanguageUtility;
import calclavia.lib.utility.inventory.InternalInventoryHandler;
import calclavia.lib.utility.inventory.InventoryUtility;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** @author tgame14
 * @since 18/03/14 */
public class TilePlacer extends TileInventory implements IRotatable, IPacketReceiver
{
    private boolean doWork = false;
    private boolean autoPullItems = false;
    private InternalInventoryHandler invHandler;
    private ForgeDirection renderItemSide_a;
    private ForgeDirection renderItemSide_b;

    public TilePlacer()
    {
        super(Material.iron);
        this.normalRender = false;
        this.maxSlots = 1;
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
        //Tries to place the item stack into the world
        if (InventoryUtility.placeItemBlock(world(), x() + this.getDirection().offsetX, y() + this.getDirection().offsetY, z() + this.getDirection().offsetZ, this.getStackInSlot(0)))
        {
            decrStackSize(0, 1);
            markUpdate();
        }
    }

    @Override
    protected boolean use(EntityPlayer player, int hitSide, Vector3 hit)
    {
        interactCurrentItem(this, 0, player);
        return true;
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

    @Override
    @SideOnly(Side.CLIENT)
    protected TileRender newRenderer()
    {
        return new TileRender()
        {
            @Override
            public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
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
