package mffs.tile;

import calclavia.api.mffs.IProjector;
import com.google.common.io.ByteArrayDataInput;
import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.prefab.tile.TileAdvanced;
import universalelectricity.api.vector.Vector3;

public class TileForceField extends TileAdvanced implements IPacketReceiver
{
	private Vector3 projector = null;
	public ItemStack camoStack = null;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.getProjector() != null)
		{
			int itemID = -1;
			int itemMetadata = -1;

			if (camoStack != null)
			{
				itemID = camoStack.itemID;
				itemMetadata = camoStack.getItemDamage();
			}

			return ModularForceFieldSystem.PACKET_TILE.getPacket(this, this.projector.intX(), this.projector.intY(), this.projector.intZ(), itemID, itemMetadata);
		}

		return null;
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... obj)
	{
		try
		{
			this.setProjector(new Vector3(data.readInt(), data.readInt(), data.readInt()));
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);

			this.camoStack = null;
			int itemID = data.readInt();
			int itemMetadata = data.readInt();

			if (itemID != -1 && itemMetadata != -1)
			{
				this.camoStack = new ItemStack(Block.blocksList[itemID], 1, itemMetadata);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setProjector(Vector3 position)
	{
		this.projector = position;

		if (!this.worldObj.isRemote)
		{
			this.refreshCamoBlock();
		}
	}

	/**
	 * @return Gets the projector block controlling this force field. Removes the force field if no
	 * projector can be found.
	 */
	public TileForceFieldProjector getProjector()
	{
		if (this.getProjectorSafe() != null)
		{
			return getProjectorSafe();
		}

		if (!this.worldObj.isRemote)
		{
			this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, 0);
		}

		return null;
	}

	public TileForceFieldProjector getProjectorSafe()
	{
		if (this.projector != null)
		{
			if (this.projector.getTileEntity(this.worldObj) instanceof TileForceFieldProjector)
			{
				if (worldObj.isRemote || ((IProjector) projector.getTileEntity(this.worldObj)).getCalculatedField().contains(new Vector3(this)))
				{
					return (TileForceFieldProjector) this.projector.getTileEntity(this.worldObj);
				}
			}
		}

		return null;
	}

	/**
	 * Server Side Only
	 */
	public void refreshCamoBlock()
	{
		if (this.getProjectorSafe() != null)
		{
			this.camoStack = MFFSHelper.getCamoBlock(this.getProjector(), new Vector3(this));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.projector = new Vector3(nbt.getCompoundTag("projector"));

	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.getProjector() != null)
		{
			nbt.setCompoundTag("projector", this.projector.writeToNBT(new NBTTagCompound()));
		}
	}
}