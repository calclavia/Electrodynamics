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
    private byte place_delay = 0;
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
    public void initiate()
    {
        super.initiate();
        updateDirection();
    }

    @Override
    public void updateEntity()
    {
        if (autoPullItems && this.ticks % 5 == 0)
        {
            if (getStackInSlot(0) == null)
            {
                this.setInventorySlotContents(0, this.getInvHandler().tryGrabFromPosition(this.getDirection().getOpposite(), 1));
            }
        }
        if (doWork)
        {
            if (place_delay < Byte.MAX_VALUE)
                place_delay++;
            
            if (place_delay >= 5)
            {//TODO implement block break speed, and a minor delay
                doWork();
                doWork = false;
            }
        }
       
    }

    public void work()
    {
        if (isIndirectlyPowered())
        {
            doWork = true;
            place_delay = 0;
        }
    }

    public void doWork()
    {
        //Tries to place the item stack into the world
        if (InventoryUtility.placeItemBlock(world(), x() + this.getDirection().offsetX, y() + this.getDirection().offsetY, z() + this.getDirection().offsetZ, this.getStackInSlot(0)))
        {
            decrStackSize(0, 1);
            markUpdate();
            doWork = false;
        }
    }

    @Override
    protected boolean use(EntityPlayer player, int hitSide, Vector3 hit)
    {
        interactCurrentItem(this, 0, player);
        return true;
    }

    protected boolean configure(EntityPlayer player, int side, Vector3 hit)
    {
        if (player.isSneaking())
        {
            this.autoPullItems = !this.autoPullItems;
            player.sendChatToPlayer(ChatMessageComponent.createFromText("AutoExtract: " + this.autoPullItems));
        }
        return super.configure(player, side, hit);
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
    public void setDirection(ForgeDirection direction)
    {
        super.setDirection(direction);
        this.updateDirection();
    }

    @SuppressWarnings("incomplete-switch")
    public void updateDirection()
    {
        switch (this.getDirection())
        {
            case EAST:
            case WEST:
                this.renderItemSide_a = ForgeDirection.NORTH;
                this.renderItemSide_b = ForgeDirection.SOUTH;
                break;
            case NORTH:
            case SOUTH:
                this.renderItemSide_a = ForgeDirection.EAST;
                this.renderItemSide_b = ForgeDirection.WEST;
                break;

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
                    if (TilePlacer.this.worldObj != null && (TilePlacer.this.renderItemSide_a == null || TilePlacer.this.renderItemSide_b == null))
                    {
                        TilePlacer.this.updateDirection();
                    }
                    GL11.glPushMatrix();
                    RenderItemOverlayUtility.renderItemOnSides(TilePlacer.this, getStackInSlot(0), position.x, position.y, position.z, LanguageUtility.getLocal("tooltip.noOutput"), TilePlacer.this.renderItemSide_a, TilePlacer.this.renderItemSide_b);
                    GL11.glPopMatrix();
                }

                return false;
            }
        };
    }
}
