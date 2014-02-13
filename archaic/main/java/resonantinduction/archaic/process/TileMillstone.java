package resonantinduction.archaic.process;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import resonantinduction.api.recipe.MachineRecipes;
import resonantinduction.api.recipe.MachineRecipes.RecipeType;
import resonantinduction.api.recipe.RecipeResource;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileExternalInventory;
import calclavia.lib.utility.inventory.InventoryUtility;

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
		RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER, getStackInSlot(0));

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
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER, itemstack).length > 0;
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
