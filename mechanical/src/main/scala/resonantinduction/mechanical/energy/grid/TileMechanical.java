package resonantinduction.mechanical.energy.grid;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.content.module.TileBase;
import calclavia.lib.grid.INode;
import calclavia.lib.grid.INodeProvider;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileMechanical extends TileBase implements INodeProvider, IPacketReceiver
{
	@Deprecated
	public TileMechanical()
	{
		super(null);
	}

	public TileMechanical(Material material)
	{
		super(material);
	}

	protected static final int PACKET_VELOCITY = Mechanical.contentRegistry.getNextPacketID();

	public MechanicalNode mechanicalNode = new PacketMechanicalNode(this).setLoad(0.5f);

	protected class PacketMechanicalNode extends MechanicalNode
	{
		public PacketMechanicalNode(INodeProvider parent)
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
		super.initiate();
	}

	@Override
	public void invalidate()
	{
		mechanicalNode.deconstruct();
		super.invalidate();
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
	public <N extends INode> N getNode(Class<? super N> nodeType, ForgeDirection from)
	{
		if (nodeType.isAssignableFrom(mechanicalNode.getClass()))
			return (N) mechanicalNode;
		return null;
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
			mechanicalNode.angularVelocity = data.readDouble();
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
