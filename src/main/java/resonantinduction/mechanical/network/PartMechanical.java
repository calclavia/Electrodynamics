package resonantinduction.mechanical.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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

	protected float angularVelocity;

	protected long torque;

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
		angle += angularVelocity / 20;

		if (!world().isRemote)
		{
			sendRotationPacket();
		}

		super.update();
	}

	@Override
	public void onAdded()
	{
		super.onAdded();
		refresh();
	}

	@Override
	public void onMoved()
	{
		this.refresh();
	}

	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		refresh();
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		refresh();
	}

	@Override
	public void onPartChanged(TMultiPart part)
	{
		refresh();
	}

	/**
	 * Refresh should be called sparingly.
	 */
	public void refresh()
	{
		connections = new Object[6];

		/** Look for gears that are back-to-back with this gear. Equate torque. */
		TileEntity tileBehind = new universalelectricity.api.vector.Vector3(tile()).translate(placementSide).getTileEntity(world());

		if (tileBehind instanceof IMechanical)
		{
			IMechanical instance = (IMechanical) ((IMechanical) tileBehind).getInstance(placementSide.getOpposite());

			if (instance != null && instance.canConnect(placementSide))
			{
				connections[placementSide.getOpposite().ordinal()] = instance;
				getNetwork().merge(instance.getNetwork());
			}

		}
		/** Look for gears that are internal and adjacent to this gear. (The 4 sides) */
		for (int i = 0; i < 6; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(i);
			IMechanical instance = (IMechanical) ((IMechanical) tile()).getInstance(checkDir);

			if (connections[checkDir.ordinal()] == null && checkDir != placementSide && checkDir != placementSide.getOpposite() && instance != null && instance.canConnect(checkDir.getOpposite()))
			{
				connections[checkDir.ordinal()] = instance;
				getNetwork().merge(instance.getNetwork());
			}
		}

		/** Look for gears outside this block space, the relative UP, DOWN, LEFT, RIGHT */
		for (int i = 0; i < 4; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(Rotation.rotateSide(this.placementSide.ordinal(), i));
			TileEntity checkTile = new universalelectricity.api.vector.Vector3(tile()).translate(checkDir).getTileEntity(world());

			if (connections[checkDir.ordinal()] == null && checkTile instanceof IMechanical)
			{
				IMechanical instance = (IMechanical) ((IMechanical) checkTile).getInstance(placementSide);

				if (instance != null && instance.canConnect(placementSide.getOpposite()))
				{
					connections[checkDir.ordinal()] = instance;
					getNetwork().merge(instance.getNetwork());
				}
			}
		}

		getNetwork().reconstruct();
	}

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
		if (world() != null && !world().isRemote && tile() != null)
		{
			tile().getWriteStream(this).writeByte(0).writeFloat(angularVelocity);
		}
	}

	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 0)
		{
			angularVelocity = packet.readFloat();
		}
	}

	/** Packet Code. */
	@Override
	public void readDesc(MCDataInput packet)
	{
		this.placementSide = ForgeDirection.getOrientation(packet.readByte());
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(this.placementSide.ordinal());
	}

	@Override
	public void read(MCDataInput packet)
	{
		read(packet, packet.readUByte());
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
		super.load(nbt);
		this.placementSide = ForgeDirection.getOrientation(nbt.getByte("side"));
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setByte("side", (byte) this.placementSide.ordinal());
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
	public float getRatio()
	{
		return 0.5f;
	}

	@Override
	public IMechanical getInstance(ForgeDirection from)
	{
		return this;
	}
}