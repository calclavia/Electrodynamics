package resonantinduction.archaic.process;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonant.lib.utility.FluidUtility;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInduction.RecipeType;
import universalelectricity.core.transform.vector.Vector3;
import resonant.lib.content.prefab.java.TileInventory;
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
public class TileCastingMold extends TileInventory implements IFluidHandler, IPacketReceiver
{
	protected FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	private final int amountPerIngot = 100;

    public TileCastingMold() {
        super(Material.rock);
        setTextureName(Reference.prefix() + "material_metal_side");
        normalRender(false);
        isOpaqueCube(false);
    }

    @Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public PacketTile getDescPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new PacketTile(this, nbt);
	}

	@Override
	public void read(ByteBuf data, EntityPlayer player, PacketType type)
	{
		try
		{
			this.readFromNBT(ByteBufUtils.readTag(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onInventoryChanged()
	{
		if (worldObj != null)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public void update()
	{
		/**
		 * Check blocks above for fluid.
		 */
		Vector3 checkPos = new Vector3(this).add(0, 1, 0);
		FluidStack drainStack = FluidUtility.drainBlock(worldObj, checkPos, false);

		if (MachineRecipes.INSTANCE.getOutput(RecipeType.SMELTER().toString(), drainStack).length > 0)
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
			RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.SMELTER().toString(), tank.getFluid());

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

    @Override
    public void click(EntityPlayer player)
    {
        if (!world().isRemote) {

            ItemStack output = getStackInSlot(0);

            if (output != null) {
                InventoryUtility.dropItemStack(world(), new Vector3(player), output, 0);
                setInventorySlotContents(0, null);
            }

            onInventoryChanged();
        }
    }

    @Override
    public boolean use(EntityPlayer player, int hitSide, Vector3 hit)
    {
            update();

            ItemStack current = player.inventory.getCurrentItem();
            ItemStack output = getStackInSlot(0);

            if (output != null)
            {
                InventoryUtility.dropItemStack(world(), new Vector3(player), output, 0);
                setInventorySlotContents(0, null);
            }

            return true;
    }
}
