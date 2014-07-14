package resonantinduction.archaic.process;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.tile.TileExternalInventory;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInduction.RecipeType;
import universalelectricity.api.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

public class TileMillstone extends TileExternalInventory implements IPacketReceiver
{
	private int grindCount = 0;

	@Override
	public void onInventoryChanged()
	{
		grindCount = 0;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void doGrind(Vector3 spawnPos)
	{
		RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name(), getStackInSlot(0));

		if (outputs.length > 0)
		{
			if (++grindCount > 20)
			{
				for (RecipeResource res : outputs)
				{
					InventoryUtility.dropItemStack(worldObj, spawnPos, res.getItemStack().copy());
				}

				decrStackSize(0, 1);
				onInventoryChanged();
			}
		}
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack)
	{
		return MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name(), itemStack).length > 0;
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return true;
	}

	/**
	 * Packets
	 */
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
}
