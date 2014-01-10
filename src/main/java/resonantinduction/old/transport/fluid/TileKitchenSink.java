package resonantinduction.old.transport.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.network.ISimplePacketReceiver;
import resonantinduction.old.transport.ResonantInductionTransport;
import resonantinduction.old.transport.fluid.prefab.TileEntityFluidStorage;
import calclavia.lib.network.PacketHandler;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;
import dark.lib.helpers.ColorCode;

public class TileKitchenSink extends TileEntityFluidStorage implements ISimplePacketReceiver
{
	@Override
	public int getTankSize()
	{
		return FluidContainerRegistry.BUCKET_VOLUME * 2;
	}

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote)
		{
			if (ticks % (random.nextInt(5) * 10 + 20) == 0)
			{
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		FluidStack stack = new FluidStack(FluidRegistry.WATER, 0);
		if (this.getTank().getFluid() != null)
		{
			stack = this.getTank().getFluid();
		}
		return PacketHandler.instance().getTilePacket(ResonantInductionTransport.CHANNEL, "FluidLevel", this, stack.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public boolean simplePacket(String id, ByteArrayDataInput data, Player player)
	{
		try
		{
			if (id.equalsIgnoreCase("FluidLevel"))
			{
				this.getTank().setFluid(FluidStack.loadFluidStackFromNBT(PacketHandler.instance().readNBTTagCompound(data)));
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return true;
		}
		return false;
	}

	@Override
	public int fill(ForgeDirection side, FluidStack resource, boolean doFill)
	{
		int f = super.fill(side, resource, doFill);
		if (doFill && f > 0)
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		return f;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return fluid != null && fluid.getName().equalsIgnoreCase("water") && from != ForgeDirection.UP;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setColor(Object obj)
	{
		return false;
	}

	@Override
	public ColorCode getColor()
	{
		return ColorCode.BLUE;
	}
}
