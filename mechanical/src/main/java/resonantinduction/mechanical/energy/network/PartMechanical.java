package resonantinduction.mechanical.energy.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.mechanical.IMechanical;
import resonantinduction.api.mechanical.IMechanicalNetwork;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;

/**
 * We assume all the force acting on the gear is 90 degrees.
 * 
 * @author Calclavia
 * 
 */
public abstract class PartMechanical extends JCuboidPart implements JNormalOcclusion, TFacePart, IMechanical
{
	private IMechanicalNetwork network;

	protected float prevAngularVelocity, angularVelocity;
	protected long torque;

	/**
	 * Packets
	 */
	int ticks = 0;
	boolean markPacketUpdate = false;

	/** Side of the block this is placed on */
	public ForgeDirection placementSide = ForgeDirection.UNKNOWN;

	/** The current angle the gear is on. In radians. */
	public float angle = 0;

	public int tier;

	public void preparePlacement(int side, int itemDamage)
	{
		this.placementSide = ForgeDirection.getOrientation((byte) (side));
		this.tier = itemDamage;
	}

	@Override
	public void update()
	{
		ticks++;
		angle += angularVelocity / 20;

		if (!world().isRemote)
			checkClientUpdate();

		super.update();
	}

	public void checkClientUpdate()
	{
		if (Math.abs(prevAngularVelocity - angularVelocity) > 0.05f || (prevAngularVelocity != angularVelocity && (prevAngularVelocity == 0 || angularVelocity == 0)))
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

	@Override
	public void onWorldJoin()
	{
		getNetwork().reconstruct();
	}

	@Override
	public void onNeighborChanged()
	{
		getNetwork().reconstruct();
	}

	@Override
	public void onPartChanged(TMultiPart part)
	{
		getNetwork().reconstruct();
	}

	@Override
	public void onWorldSeparate()
	{
		getNetwork().split(this);
	}

	/** Packet Code. */
	public void sendRotationPacket()
	{
		if (world() != null && !world().isRemote)
		{
			sendDescUpdate();
			// TODO: Make packets more efficient.
			// getWriteStream().writeByte(1).writeFloat(angularVelocity);
		}
	}

	public void read(MCDataInput packet, int packetID)
	{
		switch (packetID)
		{
			case 0:
				readDesc(packet);
				break;
			case 1:
				angularVelocity = packet.readFloat();
				break;
		}
	}

	/** Packet Code. */
	@Override
	public void readDesc(MCDataInput packet)
	{
		load(packet.readNBTTagCompound());
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		// packet.writeByte(0);
		NBTTagCompound nbt = new NBTTagCompound();
		save(nbt);
		packet.writeNBTTagCompound(nbt);
	}

	@Override
	public void read(MCDataInput packet)
	{
		super.read(packet);
		// read(packet, packet.readUByte());
	}

	@Override
	public int redstoneConductionMap()
	{
		return 0;
	}

	@Override
	public boolean solid(int arg0)
	{
		return true;
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		placementSide = ForgeDirection.getOrientation(nbt.getByte("side"));
		angularVelocity = nbt.getFloat("angularVelocity");
		tier = nbt.getByte("tier");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setByte("side", (byte) placementSide.ordinal());
		nbt.setFloat("angularVelocity", angularVelocity);
		nbt.setByte("tier", (byte) tier);
	}

	protected abstract ItemStack getItem();

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());
		return drops;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return getItem();
	}

	/**
	 * Mechanical implementations
	 */
	@Override
	public IMechanicalNetwork getNetwork()
	{
		if (network == null)
		{
			network = new MechanicalNetwork();
			network.addConnector(this);
		}

		return network;
	}

	@Override
	public void setNetwork(IMechanicalNetwork network)
	{
		this.network = network;
	}

	@Override
	public float getAngularVelocity()
	{
		return torque != 0 ? angularVelocity : 0;
	}

	@Override
	public void setAngularVelocity(float velocity)
	{
		if (world() != null && !world().isRemote)
			this.angularVelocity = velocity;
	}

	@Override
	public long getTorque()
	{
		return angularVelocity != 0 ? torque : 0;
	}

	@Override
	public void setTorque(long torque)
	{
		if (world() != null && !world().isRemote)
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
	public universalelectricity.api.vector.Vector3 position()
	{
		return new universalelectricity.api.vector.Vector3(x(), y(), z());
	}
}