package resonantinduction.mechanical.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	public static Cuboid6[][] oBoxes = new Cuboid6[6][2];

	static
	{
		oBoxes[0][0] = new Cuboid6(1 / 8D, 0, 0, 7 / 8D, 1 / 8D, 1);
		oBoxes[0][1] = new Cuboid6(0, 0, 1 / 8D, 1, 1 / 8D, 7 / 8D);
		for (int s = 1; s < 6; s++)
		{
			Transformation t = Rotation.sideRotations[s].at(Vector3.center);
			oBoxes[s][0] = oBoxes[0][0].copy().apply(t);
			oBoxes[s][1] = oBoxes[0][1].copy().apply(t);
		}
	}

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
	public void update()
	{
		if (ticks == 0)
		{
			getNetwork().addConnector(this);
		}

		ticks++;
		angle += angularVelocity / 20;

		super.update();
	}

	public void checkClientUpdate()
	{
		if (Math.abs(prevAngularVelocity - angularVelocity) > 0.1f)
		{
			prevAngularVelocity = angularVelocity;
			markPacketUpdate = true;
		}

		if (!world().isRemote && markPacketUpdate && ticks % 10 == 0)
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
	public void onMoved()
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
	public int getSlotMask()
	{
		return 1 << this.placementSide.ordinal();
	}

	@Override
	public Cuboid6 getBounds()
	{
		return FaceMicroClass.aBounds()[0x10 | this.placementSide.ordinal()];
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

	/**
	 * Multipart Methods
	 */

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return Arrays.asList(oBoxes[this.placementSide.ordinal()]);
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
		if (!world().isRemote)
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
		if (!world().isRemote)
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
}