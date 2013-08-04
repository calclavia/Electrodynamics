/**
 * 
 */
package resonantinduction.multimeter;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.PacketHandler;
import resonantinduction.ResonantInduction;
import resonantinduction.base.IPacketReceiver;
import resonantinduction.base.TileEntityBase;
import resonantinduction.tesla.TileEntityTesla;

import com.google.common.io.ByteArrayDataInput;

/**
 * @author Calclavia
 * 
 */
public class TileEntityMultimeter extends TileEntityBase implements IPacketReceiver
{
	public enum DetectMode
	{
		NONE("None"), LESS_THAN("Less Than"), LESS_THAN_EQUAL("Less Than or Equal"),
		EQUAL("Equal"), GREATER_THAN("Greater Than or Equal"), GREATER_THAN_EQUAL("Greater Than");

		public String display;

		private DetectMode(String display)
		{
			this.display = display;
		}
	}

	private DetectMode detectMode = DetectMode.NONE;
	private float energyLimit;
	private float detectedEnergy;
	private float detectedAverageEnergy;
	public boolean redstoneOn;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.ticks % 20 == 0)
		{
			this.detectedEnergy = this.doGetDetectedEnergy();
			this.detectedAverageEnergy = (detectedAverageEnergy + this.detectedEnergy) / 2;

			boolean outputRedstone = false;

			switch (detectMode)
			{
				default:
					break;
				case EQUAL:
					outputRedstone = this.detectedEnergy == this.energyLimit;
					break;
				case GREATER_THAN:
					outputRedstone = this.detectedEnergy > this.energyLimit;
					break;
				case GREATER_THAN_EQUAL:
					outputRedstone = this.detectedEnergy >= this.energyLimit;
					break;
				case LESS_THAN:
					outputRedstone = this.detectedEnergy < this.energyLimit;
					break;
				case LESS_THAN_EQUAL:
					outputRedstone = this.detectedEnergy <= this.energyLimit;
					break;
			}

			if (outputRedstone != this.redstoneOn)
			{
				this.redstoneOn = outputRedstone;
				this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, ResonantInduction.blockMultimeter.blockID);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketHandler.getTileEntityPacket(this, (byte) 1, (byte) this.detectMode.ordinal(), this.energyLimit);
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			switch (input.readByte())
			{
				default:
					this.detectMode = DetectMode.values()[input.readByte()];
					this.energyLimit = input.readFloat();
					break;
				case 2:
					this.toggleMode();
					break;
				case 3:
					this.energyLimit = input.readFloat();
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		return null;
	}

	public float doGetDetectedEnergy()
	{
		ForgeDirection direction = ForgeDirection.getOrientation(this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));
		ForgeDirection opp = direction.getOpposite();
		TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.xCoord + opp.offsetX, this.yCoord + opp.offsetY, this.zCoord + opp.offsetZ);

		// TODO: Universal Compatiblity in the future.
		if (tileEntity instanceof TileEntityTesla)
		{
			return ((TileEntityTesla) tileEntity).getEnergyStored();
		}
		return 0;
	}

	public float getDetectedEnergy()
	{
		return this.detectedEnergy;
	}

	public float getAverageDetectedEnergy()
	{
		return this.detectedAverageEnergy;
	}

	public void toggleMode()
	{
		this.detectMode = DetectMode.values()[(this.detectMode.ordinal() + 1) % DetectMode.values().length];
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.detectMode = DetectMode.values()[nbt.getInteger("detectMode")];
		this.energyLimit = nbt.getFloat("energyLimit");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("detectMode", this.detectMode.ordinal());
		nbt.setFloat("energyLimit", this.energyLimit);
	}

	public DetectMode getMode()
	{
		return this.detectMode;
	}

	public float getLimit()
	{
		return this.energyLimit;
	}

}
