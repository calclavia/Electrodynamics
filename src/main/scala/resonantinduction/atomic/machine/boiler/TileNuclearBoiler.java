package resonantinduction.atomic.machine.boiler;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonantinduction.atomic.Atomic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.IRotatable;
import resonant.lib.network.Synced;
import resonantinduction.atomic.Atomic;
import resonantinduction.atomic.AtomicContent;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;

import com.google.common.io.ByteArrayDataInput;
import resonant.lib.content.prefab.java.TileElectricInventory;
import universalelectricity.core.transform.vector.Vector3;

/** Nuclear boiler TileEntity */

public class TileNuclearBoiler extends TileElectricInventory implements IPacketReceiver, IFluidHandler, IRotatable
{
    public final static long DIAN = 50000;
    public final int SHI_JIAN = 20 * 15;
    @Synced
    public final FluidTank waterTank = new FluidTank(AtomicContent.FLUIDSTACK_WATER().copy(), FluidContainerRegistry.BUCKET_VOLUME * 5);
    @Synced
    public final FluidTank gasTank = new FluidTank(AtomicContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE().copy(), FluidContainerRegistry.BUCKET_VOLUME * 5);
    // How many ticks has this item been extracting for?
    @Synced
    public int timer = 0;
    public float rotation = 0;

    public TileNuclearBoiler()
    {
        super(Material.iron);
        energy().setCapacity(DIAN * 2);
        this.setSizeInventory(4);
    }

    @Override
    public void update()
    {
        super.update();

        if (timer > 0)
        {
            rotation += 0.1f;
        }

        if (!this.worldObj.isRemote)
        {
            // Put water as liquid
            if (getStackInSlot(1) != null)
            {
                if (FluidContainerRegistry.isFilledContainer(getStackInSlot(1)))
                {
                    FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(getStackInSlot(1));

                    if (liquid.isFluidEqual(AtomicContent.FLUIDSTACK_WATER()))
                    {
                        if (this.fill(ForgeDirection.UNKNOWN, liquid, false) > 0)
                        {
                            ItemStack resultingContainer = getStackInSlot(1).getItem().getContainerItem(getStackInSlot(1));

                            if (resultingContainer == null && getStackInSlot(1).stackSize > 1)
                            {
                                getStackInSlot(1).stackSize--;
                            }
                            else
                            {
                                setInventorySlotContents(1, resultingContainer);
                            }

                            this.waterTank.fill(liquid, true);
                        }
                    }
                }
            }

            if (this.nengYong())
            {
                this.discharge(getStackInSlot(0));

                if (energy().extractEnergy(DIAN, false) >= TileNuclearBoiler.DIAN)
                {
                    if (this.timer == 0)
                    {
                        this.timer = SHI_JIAN;
                    }

                    if (this.timer > 0)
                    {
                        this.timer--;

                        if (this.timer < 1)
                        {
                            this.yong();
                            this.timer = 0;
                        }
                    }
                    else
                    {
                        this.timer = 0;
                    }

                    energy().extractEnergy(DIAN, true);
                }
            }
            else
            {
                this.timer = 0;
            }

            if (this.ticks() % 10 == 0)
            {
                this.sendDescPack();
            }
        }
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType type)
    {
        try
        {
            this.timer = data.readInt();
            this.waterTank.setFluid(new FluidStack(AtomicContent.FLUIDSTACK_WATER().fluidID, data.readInt()));
            this.gasTank.setFluid(new FluidStack(AtomicContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE().fluidID, data.readInt()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return ResonantEngine.instance.packetHandler.toMCPacket(getDescPacket());
    }

    public PacketTile getDescPacket()
    {
        return new PacketTile(this, this.timer, Atomic.getFluidAmount(this.waterTank.getFluid()), Atomic.getFluidAmount(this.gasTank.getFluid()));
    }


    public void sendDescPack()
    {
        if (!this.worldObj.isRemote)
        {
            //for (EntityPlayerMP player : this.getPlayersUsing())
            //{
            //    ResonantEngine.instance.packetHandler.sendToPlayer(getDescPacket(), player);
            //}
        }
    }

    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        openGui(player, Atomic.INSTANCE());
        return true;
    }

    // Check all conditions and see if we can start smelting
    public boolean nengYong()
    {
        if (this.waterTank.getFluid() != null)
        {
            if (this.waterTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
            {
                if (getStackInSlot(3) != null)
                {
                    if (AtomicContent.itemYellowCake() == getStackInSlot(3).getItem() || Atomic.isItemStackUraniumOre(getStackInSlot(3)))
                    {
                        if (Atomic.getFluidAmount(this.gasTank.getFluid()) < this.gasTank.getCapacity())
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack. */
    public void yong()
    {
        if (this.nengYong())
        {
            this.waterTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
            FluidStack liquid = AtomicContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE().copy();
            liquid.amount = Settings.uraniumHexaflourideRatio() * 2;
            this.gasTank.fill(liquid, true);
            this.decrStackSize(3, 1);
        }
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.timer = nbt.getInteger("shiJian");

        NBTTagCompound waterCompound = nbt.getCompoundTag("water");
        this.waterTank.setFluid(FluidStack.loadFluidStackFromNBT(waterCompound));

        NBTTagCompound gasCompound = nbt.getCompoundTag("gas");
        this.gasTank.setFluid(FluidStack.loadFluidStackFromNBT(gasCompound));
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("shiJian", this.timer);

        if (this.waterTank.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            this.waterTank.getFluid().writeToNBT(compound);
            nbt.setTag("water", compound);
        }

        if (this.gasTank.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            this.gasTank.getFluid().writeToNBT(compound);
            nbt.setTag("gas", compound);
        }
    }

    /** Tank Methods */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (AtomicContent.FLUIDSTACK_WATER().isFluidEqual(resource))
        {
            return this.waterTank.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (AtomicContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE().isFluidEqual(resource))
        {
            return this.gasTank.drain(resource.amount, doDrain);
        }

        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return this.gasTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return AtomicContent.FLUIDSTACK_WATER().fluidID == fluid.getID();
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return AtomicContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE().fluidID == fluid.getID();
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[]
        { this.waterTank.getInfo(), this.gasTank.getInfo() };
    }

    /** Inventory */
    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        if (slotID == 1)
        {
            return Atomic.isItemStackWaterCell(itemStack);
        }
        else if (slotID == 3)
        {
            return itemStack.getItem() == AtomicContent.itemYellowCake();
        }

        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return side == 0 ? new int[]
        { 2 } : new int[]
        { 1, 3 };
    }

    @Override
    public boolean canInsertItem(int slotID, ItemStack itemStack, int side)
    {
        return this.isItemValidForSlot(slotID, itemStack);
    }

    @Override
    public boolean canExtractItem(int slotID, ItemStack itemstack, int j)
    {
        return slotID == 2;
    }

    @Override
    public ForgeDirection getDirection() {
        return null;
    }

    @Override
    public void setDirection(ForgeDirection direction) {

    }
}
