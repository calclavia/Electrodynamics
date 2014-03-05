package resonantinduction.mechanical.energy.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileAdvanced;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileMechanical extends TileAdvanced implements IMechanicalNodeProvider, IPacketReceiver
{
	protected static final int PACKET_VELOCITY = Mechanical.contentRegistry.getNextPacketID();

	public MechanicalNode mechanicalNode = new PacketMechanicalNode(this).setLoad(0.5f);

	protected class PacketMechanicalNode extends MechanicalNode
	{
		public PacketMechanicalNode(IMechanicalNodeProvider parent)
		{
			super(parent);
		}

		@Override
		protected void onUpdate()
		{
			if (Math.abs(prevAngularVelocity - angularVelocity) > 0.001 || (prevAngularVelocity != angularVelocity && (prevAngularVelocity == 0 || angularVelocity == 0)))
			{
				prevAngularVelocity = angularVelocity;
				markPacketUpdate = true;
			}
		}
	};

	/**
	 * For sending client update packets
	 */
	private boolean markPacketUpdate;

	@Override
	public void initiate()
	{
		mechanicalNode.reconstruct();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (markPacketUpdate && ticks % 10 == 0)
		{
			sendRotationPacket();
			markPacketUpdate = false;
		}
	}

	@Override
	public MechanicalNode getNode(ForgeDirection dir)
	{
		return mechanicalNode;
	}

	private void sendRotationPacket()
	{
		PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, PACKET_VELOCITY, mechanicalNode.angularVelocity), worldObj, new Vector3(this), 20);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		onReceivePacket(data.readInt(), data, player, extra);
	}

	public void onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		if (id == PACKET_VELOCITY)
			mechanicalNode.angularVelocity = data.readFloat();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		mechanicalNode.load(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		mechanicalNode.save(nbt);
	}
}
