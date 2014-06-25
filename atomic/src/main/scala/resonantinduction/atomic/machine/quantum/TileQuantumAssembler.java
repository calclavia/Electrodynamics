package resonantinduction.atomic.machine.quantum;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.recipe.QuantumAssemblerRecipes;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.prefab.tile.TileElectricalInventory;
import resonantinduction.atomic.Atomic;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/** Atomic assembler of items *
 * 
 * @author Calclavia, Darkguardsman */
public class TileQuantumAssembler extends TileElectricalInventory implements IPacketReceiver, IVoltageInput
{
    long ENERGY = 1000000000L;
    int MAX_TIME = 20 * 120;
    int time = 0;

    /** Used for rendering arm motion, X Y Z are not used as location data */
    Vector3 rotation = new Vector3();

    /** Used for rendering. */
    EntityItem entityItem = null;

    public TileQuantumAssembler()
    {
        super(Material.iron);
        energy = new EnergyStorageHandler(ENERGY, ENERGY / 10);
        maxSlots = 7;
        isOpaqueCube = false;
        normalRender = false;
        customItemRender = true;
        textureName = "machine";
    }

    /** Called when the block is right clicked by the player */
    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        if (!world().isRemote)
            player.openGui(Atomic.INSTANCE, 0, world(), x(), y(), z());

        return true;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        
        //Server side processing of items
        if (!this.worldObj.isRemote)
        {
            if (this.canProcess())
            {
                if (energy.checkExtract())
                {
                    if (this.time == 0)
                    {
                        this.time = this.MAX_TIME;
                    }
                    if (this.time > 0)
                    {
                        this.time -= 1;
                        if (this.time < 1)
                        {
                            this.process();
                            this.time = 0;
                        }
                    }
                    else
                    {
                        this.time = 0;
                    }
                    this.energy.extractEnergy(ENERGY, true);
                }
            }
            else
            {
                this.time = 0;
            }
            if (this.ticks % 10 == 0)
            {
                for (EntityPlayer player : this.getPlayersUsing())
                {
                    PacketDispatcher.sendPacketToPlayer(getDescriptionPacket(), (Player) player);
                }
            }
        } //Client side animation
        else if (this.time > 0)
        {           
            ItemStack middleStack = this.getStackInSlot(6);
            if (middleStack != null)
            {
                middleStack = middleStack.copy();
                middleStack.stackSize = 1;
                if (this.entityItem == null)
                {
                    this.entityItem = new EntityItem(this.worldObj, 0, 0, 0, middleStack);
                }
                else if (!middleStack.isItemEqual(this.entityItem.getEntityItem()))
                {
                    this.entityItem = new EntityItem(this.worldObj, 0, 0, 0, middleStack);
                }
                this.entityItem.age += 1;
            }
            else
            {
                this.entityItem = null;
            }
            
            //Audio update
            if (this.ticks % 600 == 0)
            {
                this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, Reference.PREFIX + "assembler", 0.7f, 1f);
            }
            
            //Animation frame update
            this.rotation.translate(3, 2, 1);
        }
    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        if (this.canProcess())
        {
            return super.onReceiveEnergy(from, receive, doReceive);
        }
        return 0;
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... obj)
    {
        try
        {
            this.time = data.readInt();
            int itemID = data.readInt();
            int itemAmount = data.readInt();
            int itemMeta = data.readInt();
            if (itemID != -1 && itemAmount != -1 && itemMeta != -1)
            {
                this.setInventorySlotContents(6, new ItemStack(Item.itemsList[itemID], itemAmount, itemMeta));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        if (this.getStackInSlot(6) != null)
        {
            return ResonantInduction.PACKET_TILE.getPacket(this, time, getStackInSlot(6).itemID, getStackInSlot(6).stackSize, getStackInSlot(6).getItemDamage());
        }
        return ResonantInduction.PACKET_TILE.getPacket(this, time, -1, -1, -1);
    }

    @Override
    public void openChest()
    {
        if (!this.worldObj.isRemote)
        {
            for (EntityPlayer player : this.getPlayersUsing())
            {
                PacketDispatcher.sendPacketToPlayer(getDescriptionPacket(), (Player) player);
            }
        }
    }

    /** Checks to see if the assembler can run */
    public boolean canProcess()
    {
        if (getStackInSlot(6) != null)
        {
            if (QuantumAssemblerRecipes.hasItemStack(getStackInSlot(6)))
            {
                for (int i = 0; i < 6; i++)
                {
                    if (getStackInSlot(i) == null)
                        return false;
                    if (getStackInSlot(i).itemID != Atomic.itemDarkMatter.itemID)
                        return false;
                }
                return getStackInSlot(6).stackSize < 64;
            }
        }
        return false;
    }

    /** Turn one item from the furnace source stack into the appropriate smelted item in the furnace
     * result stack */
    public void process()
    {
        if (this.canProcess())
        {
            for (int i = 0; i < 6; i++)
            {
                if (getStackInSlot(i) != null)
                {
                    decrStackSize(i, 1);
                }
            }
            if (getStackInSlot(6) != null)
            {
                getStackInSlot(6).stackSize += 1;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.time = nbt.getInteger("smeltingTicks");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("smeltingTicks", this.time);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        if (slotID == 6)
        {
            return true;
        }
        return itemStack.itemID == Atomic.itemDarkMatter.itemID;
    }

    @Override
    public long getVoltageInput(ForgeDirection from)
    {
        return 1000;
    }

    @Override
    public void onWrongVoltage(ForgeDirection d, long voltage)
    {
    }
}
