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
import codechicken.multipart.TileMultipart;

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

	/** Side of the block this is placed on */
	public ForgeDirection placementSide;

	/** The size of the gear */
	private float radius = 0.5f;

	public boolean isClockwise = true;

	/** The current angle the gear is on. In radians per second. */
	public float angle = 0;

	public void preparePlacement(int side, int itemDamage)
	{
		this.placementSide = ForgeDirection.getOrientation((byte) (side ^ 1));
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
		/** Look for gears that are back-to-back with this gear. Equate torque. */
		universalelectricity.api.vector.Vector3 vec = new universalelectricity.api.vector.Vector3(tile()).translate(placementSide);

		TileEntity tile = vec.getTileEntity(world());

		if (tile instanceof IMechanical)
		{
			IMechanicalNetwork networkToMerge = ((IMechanical) tile).getNetwork(this.placementSide.getOpposite());

			if (networkToMerge != null)
			{
				connections[this.placementSide.getOpposite().ordinal()] = ((IMechanical) tile).getInstance(this.placementSide.getOpposite());
				getNetwork().merge(networkToMerge);
			}

		}

		/** Look for gears outside this block space, the relative UP, DOWN, LEFT, RIGHT */
		for (int i = 0; i < 4; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(Rotation.rotateSide(this.placementSide.ordinal(), i));
			universalelectricity.api.vector.Vector3 checkVec = new universalelectricity.api.vector.Vector3(tile()).translate(checkDir);

			TileEntity checkTile = checkVec.getTileEntity(world());

			if (checkTile instanceof IMechanical)
			{
				IMechanicalNetwork networkToMerge = ((IMechanical) checkTile).getNetwork(this.placementSide);

				if (networkToMerge != null)
				{
					connections[checkDir.ordinal()] = ((IMechanical) checkTile).getInstance(this.placementSide);
					getNetwork().merge(networkToMerge);
				}
			}
		}

		/** Look for gears that are internal and adjacent to this gear. (The 2 sides) */
		for (int i = 0; i < 6; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(i);
			IMechanicalNetwork networkToMerge = ((IMechanical) tile()).getNetwork(checkDir);

			if (networkToMerge != null)
			{
				connections[checkDir.ordinal()] = ((IMechanical) tile()).getInstance(checkDir);
				getNetwork().merge(networkToMerge);
			}
		}

		getNetwork().reconstruct();

		if (!world().isRemote)
		{
			sendRefreshPacket();
		}
	}

	@Override
	public IMechanicalNetwork getNetwork(ForgeDirection from)
	{
		return getNetwork();
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

	@Override
	public boolean sendNetworkPacket(long torque, float angularVelocity)
	{
		if (world() != null && !world().isRemote && tile() != null)
		{
			tile().getWriteStream(this).writeByte(0).writeLong(torque).writeFloat(angularVelocity).writeBoolean(isClockwise);
		}

		return true;
	}

	public void sendRefreshPacket()
	{
		if (world() != null && !world().isRemote && tile() != null)
		{
			tile().getWriteStream(this).writeByte(1);
		}
	}

	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 0)
		{
			getNetwork().setPower(packet.readLong(), packet.readFloat());
			isClockwise = packet.readBoolean();
		}
		else if (packetID == 1)
		{
			this.refresh();
		}
	}

	/**
	 * Network Methods
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
		return new universalelectricity.api.vector.Vector3(this.x() + direction.offsetX, this.y() + direction.offsetY, this.z() + direction.offsetZ).getTileEntity(this.world()) instanceof IMechanical;
	}

	@Override
	public boolean isClockwise()
	{
		return isClockwise;
	}

	@Override
	public void setClockwise(boolean isClockwise)
	{
		if (this.isClockwise != isClockwise)
		{
			if (getNetwork().getPower() > 0)
			{
				getNetwork().setPower(0, 0);
			}

		}

		this.isClockwise = isClockwise;
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

}