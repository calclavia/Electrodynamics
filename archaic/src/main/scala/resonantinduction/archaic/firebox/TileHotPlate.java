package resonantinduction.archaic.firebox;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.IPacketSender;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.tile.TileExternalInventory;
import resonantinduction.core.ResonantInduction;

import com.google.common.io.ByteArrayDataInput;

/**
 * For smelting items.
 * 
 * @author Calclavia
 * 
 */
public class TileHotPlate extends TileExternalInventory implements IPacketSender, IPacketReceiver
{
	private final int POWER = 50000;
	public final int[] smeltTime = new int[] { 0, 0, 0, 0 };
	public final int[] stackSizeCache = new int[] { 0, 0, 0, 0 };
	public static final int MAX_SMELT_TIME = 200;

	public TileHotPlate()
	{
		maxSlots = 4;
	}

	@Override
	public void updateEntity()
	{
		if (canRun())
		{
			boolean didSmelt = false;

			for (int i = 0; i < maxSlots; i++)
			{
				if (canSmelt(this.getStackInSlot(i)))
				{
					if (smeltTime[i] <= 0)
					{
						/**
						 * Heat up all slots
						 */
						stackSizeCache[i] = this.getStackInSlot(i).stackSize;
						smeltTime[i] = MAX_SMELT_TIME * stackSizeCache[i];
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					}
					else if (smeltTime[i] > 0)
					{
						/**
						 * Do the smelt action.
						 */
						if (--smeltTime[i] == 0)
						{
							if (!worldObj.isRemote)
							{
								ItemStack outputStack = FurnaceRecipes.smelting().getSmeltingResult(getStackInSlot(i)).copy();
								outputStack.stackSize = stackSizeCache[i];
								setInventorySlotContents(i, outputStack);
								worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
							}
						}
					}

					didSmelt = true;
				}
				else
				{
					smeltTime[i] = 0;
				}
			}
		}
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();

		/**
		 * Update cache calculation.
		 */
		for (int i = 0; i < maxSlots; i++)
		{
			if (getStackInSlot(i) != null)
			{
				if (stackSizeCache[i] != getStackInSlot(i).stackSize)
				{
					if (smeltTime[i] > 0)
					{
						smeltTime[i] += (getStackInSlot(i).stackSize - stackSizeCache[i]) * MAX_SMELT_TIME;
					}

					stackSizeCache[i] = getStackInSlot(i).stackSize;
				}
			}
			else
			{
				stackSizeCache[i] = 0;
			}
		}

		if (worldObj != null)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public boolean canRun()
	{

		TileEntity tileEntity = worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);

		if (tileEntity instanceof TileFirebox)
		{
			if (((TileFirebox) tileEntity).isBurning())
			{
				return true;
			}
		}

		return false;
	}

	public boolean canSmelt(ItemStack stack)
	{
		return FurnaceRecipes.smelting().getSmeltingResult(stack) != null;
	}

	public boolean isSmelting()
	{
		for (int i = 0; i < maxSlots; i++)
		{
			if (getSmeltTime(i) > 0)
			{
				return true;
			}
		}

		return false;
	}

	public int getSmeltTime(int i)
	{
		return smeltTime[i];
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack)
	{
		return i < maxSlots && canSmelt(itemStack);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_TILE.getPacket(this, this.getPacketData(0).toArray());
	}

	/**
	 * 1 - Description Packet
	 * 2 - Energy Update
	 * 3 - Tesla Beam
	 */
	@Override
	public List getPacketData(int type)
	{
		List list = new ArrayList();
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		list.add(nbt);
		return list;
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			this.readFromNBT(PacketHandler.readNBTTagCompound(data));
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		for (int i = 0; i < maxSlots; i++)
			smeltTime[i] = nbt.getInteger("smeltTime" + i);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		for (int i = 0; i < maxSlots; i++)
			nbt.setInteger("smeltTime" + i, smeltTime[i]);
	}

}
