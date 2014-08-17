package resonantinduction.atomic.machine.plasma;

import java.util.HashMap;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.lwjgl.opengl.GL11;
import resonant.api.ITagRender;
import resonant.engine.ResonantEngine;
import resonant.lib.config.Config;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonant.lib.utility.FluidUtility;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.atomic.Atomic;
import resonantinduction.atomic.AtomicContent;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.UnitDisplay;
import universalelectricity.core.transform.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;
import resonant.lib.content.prefab.java.TileElectric;

public class TilePlasmaHeater extends TileElectric implements IPacketReceiver, ITagRender, IFluidHandler
{
    public static long joules = 10000000000L;

    @Config
    public static int plasmaHeatAmount = 100;

    public final FluidTank tankInputDeuterium = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10);
    public final FluidTank tankInputTritium = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10);
    public final FluidTank tankOutput = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10);

    public float rotation = 0;

    public TilePlasmaHeater()
    {
        super(Material.iron);
        energy().setCapacity(joules);
        energy().setMaxTransfer(joules / 20);
        normalRender(false);
        isOpaqueCube(false);
    }

    @Override
    public void update()
    {
        super.update();

        rotation += energy().getEnergy() / 10000f;

        if (!worldObj.isRemote)
        {
            if (energy().checkExtract())
            {
                // Creates plasma if there is enough Deuterium, Tritium AND Plasma output is not full.
                if (tankInputDeuterium.getFluidAmount() >= plasmaHeatAmount &&
                        tankInputTritium.getFluidAmount() >= plasmaHeatAmount &&
                        tankOutput.getFluidAmount() < tankOutput.getCapacity())
                {
                    tankInputDeuterium.drain(plasmaHeatAmount, true);
                    tankInputTritium.drain(plasmaHeatAmount, true);
                    tankOutput.fill(new FluidStack(AtomicContent.FLUID_PLASMA(), tankOutput.getCapacity()), true);
                    energy().extractEnergy();
                }
            }
        }

        if (ticks() % 80 == 0)
        {
            world().markBlockForUpdate(x(), y(), z());
            //PacketHandler.sendPacketToClients(getDescriptionPacket(), worldObj, new Vector3(this), 25);
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(this, nbt));
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType type)
    {
        try
        {
            readFromNBT(ByteBufUtils.readTag(data));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        NBTTagCompound deuterium = nbt.getCompoundTag("tankInputDeuterium");
        tankInputDeuterium.setFluid(FluidStack.loadFluidStackFromNBT(deuterium));
        NBTTagCompound tritium = nbt.getCompoundTag("tankInputTritium");
        tankInputTritium.setFluid(FluidStack.loadFluidStackFromNBT(tritium));
        NBTTagCompound output = nbt.getCompoundTag("tankOutput");
        tankOutput.setFluid(FluidStack.loadFluidStackFromNBT(output));
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (tankInputDeuterium.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            tankInputDeuterium.getFluid().writeToNBT(compound);
            nbt.setTag("tankInputDeuterium", compound);
        }
        if (tankInputTritium.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            tankInputTritium.getFluid().writeToNBT(compound);
            nbt.setTag("tankInputTritium", compound);
        }
        if (tankOutput.getFluid() != null)
        {
            NBTTagCompound compound = new NBTTagCompound();
            tankOutput.getFluid().writeToNBT(compound);
            nbt.setTag("tankOutput", compound);
        }
    }

    @Override
    public float addInformation(HashMap<String, Integer> map, EntityPlayer player)
    {
        if (energy() != null)
        {
            map.put(LanguageUtility.getLocal("tooltip.energy") + ": " + new UnitDisplay(UnitDisplay.Unit.JOULES, energy().getEnergy()), 0xFFFFFF);
        }

        if (tankInputDeuterium.getFluidAmount() > 0)
        {
            map.put(LanguageUtility.getLocal("fluid.deuterium") + ": " + tankInputDeuterium.getFluidAmount() + " L", 0xFFFFFF);
        }

        if (tankInputTritium.getFluidAmount() > 0)
        {
            map.put(LanguageUtility.getLocal("fluid.tritium") + ": " + tankInputTritium.getFluidAmount() + " L", 0xFFFFFF);
        }

        if (tankOutput.getFluidAmount() > 0)
        {
            map.put(LanguageUtility.getLocal("fluid.plasma") + ": " + tankOutput.getFluidAmount() + " L", 0xFFFFFF);
        }

        return 1.5f;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (resource.isFluidEqual(AtomicContent.FLUIDSTACK_DEUTERIUM()))
        {
            return tankInputDeuterium.fill(resource, doFill);
        }

        if (resource.isFluidEqual(AtomicContent.FLUIDSTACK_TRITIUM()))
        {
            return tankInputTritium.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return tankOutput.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return fluid.getID() == AtomicContent.FLUID_DEUTERIUM().getID() || fluid.getID() == AtomicContent.FLUID_TRITIUM().getID();
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return fluid == AtomicContent.FLUID_PLASMA();
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[]
        { tankInputDeuterium.getInfo(), tankInputTritium.getInfo(), tankOutput.getInfo() };
    }

    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        return FluidUtility.playerActivatedFluidItem(world(), x(), y(), z(), player, side);
    }
}
