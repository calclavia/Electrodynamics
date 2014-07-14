package resonantinduction.archaic.process;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.tile.TileExternalInventory;
import resonant.lib.utility.FluidUtility;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInduction.RecipeType;
import universalelectricity.api.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

/**
 * Turns molten fuilds into ingots.
 * 
 * 1 m^3 of molten fluid = 1 block
 * Approximately 100-110 L of fluid = 1 ingot.
 * 
 * @author Calclavia
 * 
 */
public class TileCastingMold extends TileExternalInventory implements IFluidHandler, IPacketReceiver
{
	protected FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	private final int amountPerIngot = 100;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			this.readFromNBT(PacketHandler.readNBTTagCompound(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onInventoryChanged()
	{
		if (worldObj != null)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void updateEntity()
	{
		/**
		 * Check blocks above for fluid.
		 */
		Vector3 checkPos = new Vector3(this).translate(0, 1, 0);
		FluidStack drainStack = FluidUtility.drainBlock(worldObj, checkPos, false);

		if (MachineRecipes.INSTANCE.getOutput(RecipeType.SMELTER.name(), drainStack).length > 0)
		{
			if (drainStack.amount == tank.fill(drainStack, false))
			{
				tank.fill(FluidUtility.drainBlock(worldObj, checkPos, true), true);
			}
		}

		/**
		 * Attempt to cast the fluid
		 */
		while (tank.getFluidAmount() >= amountPerIngot && (getStackInSlot(0) == null || getStackInSlot(0).stackSize < getStackInSlot(0).getMaxStackSize()))
		{
			RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.SMELTER.name(), tank.getFluid());

			for (RecipeResource output : outputs)
			{
				incrStackSize(0, output.getItemStack());
			}

			tank.drain(amountPerIngot, true);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		tank.writeToNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tank.readFromNBT(tag);
	}

	/* IFluidHandler */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		int fill = tank.fill(resource, doFill);
		updateEntity();
		return fill;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return fluid != null && fluid.getName().contains("molten");
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { tank.getInfo() };
	}
}
