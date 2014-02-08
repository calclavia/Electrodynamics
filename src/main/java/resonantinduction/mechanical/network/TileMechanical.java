package resonantinduction.mechanical.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.gear.PartGearShaft;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileAdvanced;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileMechanical extends TileAdvanced implements IMechanical, IPacketReceiver
{
	/** The mechanical connections this connector has made */
	protected Object[] connections = new Object[6];
	private IMechanicalNetwork network;
	protected float angularVelocity;
	protected long torque;
	public float angle = 0;

	/**
	 * For sending client update packets
	 */
	private float prevAngularVelocity;
	private boolean markPacketUpdate;

	@Override
	public void initiate()
	{
		refresh();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		angle += angularVelocity / 20;
		torque *= getLoad();
		angularVelocity *= getLoad();

		if (Math.abs(prevAngularVelocity - angularVelocity) > 0.05f)
		{
			prevAngularVelocity = angularVelocity;
			markPacketUpdate = true;
		}

		if (markPacketUpdate && ticks % 10 == 0)
		{
			sendRotationPacket();
			markPacketUpdate = false;
		}
	}

	private void sendRotationPacket()
	{
		PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, angularVelocity), worldObj, new Vector3(this), 20);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		angularVelocity = data.readFloat();
	}

	@Override
	public void invalidate()
	{
		getNetwork().split(this);
		super.invalidate();
	}

	/**
	 * Refreshes all the connections of this block.
	 */
	public void refresh()
	{

	}

	protected float getLoad()
	{
		return 0.95f;
	}

	@Override
	@Deprecated
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public Object[] getConnections()
	{
		connections = new Object[6];

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = new Vector3(this).translate(dir).getTileEntity(worldObj);

			if (tile instanceof IMechanical)
			{
				IMechanical mech = ((IMechanical) tile).getInstance(dir.getOpposite());

				// Don't connect with shafts
				if (mech != null && !(mech instanceof PartGearShaft) && canConnect(dir, this) && mech.canConnect(dir.getOpposite(), this))
				{
					connections[dir.ordinal()] = mech;
					getNetwork().merge(mech.getNetwork());
				}
			}
		}

		return connections;
	}

	@Override
	public IMechanicalNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new MechanicalNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public void setNetwork(IMechanicalNetwork network)
	{
		this.network = network;
	}

	@Override
	public float getAngularVelocity()
	{
		return angularVelocity;
	}

	@Override
	public void setAngularVelocity(float velocity)
	{
		this.angularVelocity = velocity;
	}

	@Override
	public long getTorque()
	{
		return torque;
	}

	@Override
	public void setTorque(long torque)
	{
		this.torque = torque;
	}

	@Override
	public float getRatio(ForgeDirection dir, Object source)
	{
		return 0.5f;
	}

	@Override
	public IMechanical getInstance(ForgeDirection from)
	{
		return this;
	}

	@Override
	public Vector3 getPosition()
	{
		return new Vector3(this);
	}

	@Override
	public boolean inverseRotation(ForgeDirection dir, IMechanical with)
	{
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		torque = nbt.getLong("torque");
		angularVelocity = nbt.getFloat("angularVelocity");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setLong("torque", torque);
		nbt.setFloat("angularVelocity", angularVelocity);
	}
}
