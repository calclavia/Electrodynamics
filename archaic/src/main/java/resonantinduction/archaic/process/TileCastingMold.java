package resonantinduction.archaic.process;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.RecipeResource;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.fluid.BlockFluidMaterial;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileExternalInventory;
import calclavia.lib.utility.LanguageUtility;

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
		int blockID = checkPos.getBlockID(worldObj);
		Block block = Block.blocksList[blockID];

		if (block instanceof BlockFluidMaterial)
		{
			/**
			 * Only drain if everything is accepted by the tank.
			 */
			FluidStack drainStack = ((BlockFluidMaterial) block).drain(worldObj, checkPos.intX(), checkPos.intY(), checkPos.intZ(), false);

			if (drainStack.amount == tank.fill(drainStack, false))
			{
				tank.fill(((BlockFluidMaterial) block).drain(worldObj, checkPos.intX(), checkPos.intY(), checkPos.intZ(), true), true);
			}
		}

		/**
		 * Attempt to cast the fluid
		 */
		if (tank.getFluidAmount() > amountPerIngot)
		{
			RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.SMELTER, tank.getFluid());

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
