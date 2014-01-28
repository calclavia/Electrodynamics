package resonantinduction.mechanical.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroClass;
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

	/** The mechanical connections this connector has made */
	protected Object[] connections = new Object[6];

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

	public void preparePlacement(int side, int itemDamage)
	{
		this.placementSide = ForgeDirection.getOrientation((byte) (side ^ 1));
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		if (!world().isRemote)
		{
			System.out.println(this + ":" + getNetwork());
			for (Object obj : connections)
				System.out.println(obj);
		}

		return false;
	}

	@Override
	public void update()
	{
		// TODO: Fix gear network somehow tick while network is invalid.
		getNetwork().addConnector(this);

		ticks++;
		angle += angularVelocity / 20;

		if (!world().isRemote)
			checkClientUpdate();

		super.update();
	}

	public void checkClientUpdate()
	{
		if (Math.abs(prevAngularVelocity - angularVelocity) > 0.1f)
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
		refresh();
	}

	@Override
	public void onNeighborChanged()
	{
		refresh();
	}

	@Override
	public void onPartChanged(TMultiPart part)
	{
		refresh();
	}

	protected abstract void refresh();

	@Override
	public Object[] getConnections()
	{
		return connections;
	}

	@Override
	public void preRemove()
	{
		this.getNetwork().split(this);
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
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setByte("side", (byte) placementSide.ordinal());
		nbt.setFloat("angularVelocity", angularVelocity);
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
	@Deprecated
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public float getAngularVelocity()
	{
		return angularVelocity;
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
		return torque;
	}

	@Override
	public void setTorque(long torque)
	{
		if (world() != null && !world().isRemote)
			this.torque = torque;
	}

	@Override
	public float getRatio(ForgeDirection dir)
	{
		return 0.5f;
	}

	@Override
	public IMechanical getInstance(ForgeDirection from)
	{
		return this;
	}

	@Override
	public universalelectricity.api.vector.Vector3 getPosition()
	{
		return new universalelectricity.api.vector.Vector3(x(), y(), z());
	}
}