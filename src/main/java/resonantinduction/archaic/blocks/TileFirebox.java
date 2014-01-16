package resonantinduction.archaic.blocks;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.prefab.tile.TileAdvancedInventory;

import com.google.common.io.ByteArrayDataInput;

/**
 * Meant to replace the furnace class.
 * 
 * @author Calclavia
 * 
 */
public class TileFirebox extends TileAdvancedInventory implements IPacketSender, IPacketReceiver
{
	/**
	 * The power of the firebox in terms of thermal energy. The thermal energy can be transfered
	 * into fluids to increase their internal energy.
	 */
	private final int POWER = 1000;
	private int burnTime;

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote)
		{
			if (burnTime > 0)
			{
				if (burnTime-- == 0)
				{
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}

			if (canBurn(this.getStackInSlot(0)))
			{
				if (burnTime == 0)
				{
					burnTime = TileEntityFurnace.getItemBurnTime(this.getStackInSlot(0));
					decrStackSize(1, 1);
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
		}
	}

	private boolean canBurn(ItemStack stack)
	{
		return TileEntityFurnace.getItemBurnTime(stack) > 0;
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
	public ArrayList getPacketData(int type)
	{
		ArrayList data = new ArrayList();
		data.add(this.burnTime);
		return data;
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			this.burnTime = data.readInt();
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isBurning()
	{
		return burnTime > 0;
	}
}
