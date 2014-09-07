package resonantinduction.core.prefab.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import universalelectricity.api.core.grid.IConnector;
import universalelectricity.api.core.grid.INode;
import universalelectricity.core.transform.vector.Vector3;
import universalelectricity.core.transform.vector.VectorWorld;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class PartFramedConnection<M extends Enum> extends PartColorableMaterial<M> implements TSlottedPart, JNormalOcclusion
{
	public static IndexedCuboid6[] sides = new IndexedCuboid6[7];
	public static IndexedCuboid6[] insulatedSides = new IndexedCuboid6[7];

	static
	{
		sides[0] = new IndexedCuboid6(0, new Cuboid6(0.36, 0.000, 0.36, 0.64, 0.36, 0.64));
		sides[1] = new IndexedCuboid6(1, new Cuboid6(0.36, 0.64, 0.36, 0.64, 1.000, 0.64));
		sides[2] = new IndexedCuboid6(2, new Cuboid6(0.36, 0.36, 0.000, 0.64, 0.64, 0.36));
		sides[3] = new IndexedCuboid6(3, new Cuboid6(0.36, 0.36, 0.64, 0.64, 0.64, 1.000));
		sides[4] = new IndexedCuboid6(4, new Cuboid6(0.000, 0.36, 0.36, 0.36, 0.64, 0.64));
		sides[5] = new IndexedCuboid6(5, new Cuboid6(0.64, 0.36, 0.36, 1.000, 0.64, 0.64));
		sides[6] = new IndexedCuboid6(6, new Cuboid6(0.36, 0.36, 0.36, 0.64, 0.64, 0.64));
		insulatedSides[0] = new IndexedCuboid6(0, new Cuboid6(0.3, 0.0, 0.3, 0.7, 0.3, 0.7));
		insulatedSides[1] = new IndexedCuboid6(1, new Cuboid6(0.3, 0.7, 0.3, 0.7, 1.0, 0.7));
		insulatedSides[2] = new IndexedCuboid6(2, new Cuboid6(0.3, 0.3, 0.0, 0.7, 0.7, 0.3));
		insulatedSides[3] = new IndexedCuboid6(3, new Cuboid6(0.3, 0.3, 0.7, 0.7, 0.7, 1.0));
		insulatedSides[4] = new IndexedCuboid6(4, new Cuboid6(0.0, 0.3, 0.3, 0.3, 0.7, 0.7));
		insulatedSides[5] = new IndexedCuboid6(5, new Cuboid6(0.7, 0.3, 0.3, 1.0, 0.7, 0.7));
		insulatedSides[6] = new IndexedCuboid6(6, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7));
	}
	/**
	 * Bitmask connections
	 */
	public byte currentWireConnections = 0x00;
	public byte currentAcceptorConnections = 0x00;
	protected Object[] connections = new Object[6];
	@SideOnly(Side.CLIENT)
	protected IIcon breakIcon;
	/**
	 * Client Side
	 */
	private ForgeDirection testingSide;

	public PartFramedConnection(Item insulationType)
	{
		super(insulationType);
	}

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return ((connections & tester) > 0);
	}

	public void preparePlacement(int meta)
	{
		this.setMaterial(meta);
	}

	@Override
	public boolean occlusionTest(TMultiPart other)
	{
		return NormalOcclusionTest.apply(this, other);
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts()
	{
		Set<IndexedCuboid6> subParts = new HashSet<IndexedCuboid6>();
		IndexedCuboid6[] currentSides = isInsulated() ? insulatedSides : sides;

		if (tile() != null)
		{
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				int ord = side.ordinal();
				if (connectionMapContainsSide(getAllCurrentConnections(), side) || side == testingSide)
				{
					subParts.add(currentSides[ord]);
				}
			}
		}

		subParts.add(currentSides[6]);
		return subParts;
	}

	/**
	 * Rendering and block bounds.
	 */
	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		Set<Cuboid6> collisionBoxes = new HashSet<Cuboid6>();
		collisionBoxes.addAll((Collection<? extends Cuboid6>) getSubParts());

		return collisionBoxes;
	}

	@Override
	public float getStrength(MovingObjectPosition hit, EntityPlayer player)
	{
		return 10F;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return getCollisionBoxes();
	}

	@Override
	public int getSlotMask()
	{
		return PartMap.CENTER.mask;
	}

	public boolean isBlockedOnSide(ForgeDirection side)
	{
		TMultiPart blocker = tile().partMap(side.ordinal());
		testingSide = side;
		boolean expandable = NormalOcclusionTest.apply(this, blocker);
		testingSide = null;
		return !expandable;
	}

	public byte getAllCurrentConnections()
	{
		return (byte) (currentWireConnections | currentAcceptorConnections);
	}

	/**
	 * CONNECTION LOGIC CODE
	 */
	protected abstract boolean canConnectTo(TileEntity tile, ForgeDirection to);

	protected abstract INode getConnector(TileEntity tile);

	public boolean canConnectBothSides(TileEntity tile, ForgeDirection side)
	{
		boolean notPrevented = !isConnectionPrevented(tile, side);

		if (getConnector(tile) instanceof IConnector)
		{
			notPrevented &= ((IConnector) getConnector(tile)).canConnect(side.getOpposite(), this);
		}

		return notPrevented;
	}

	/**
	 * Override if there are ways of preventing a connection
	 *
	 * @param tile The TileEntity on the given side
	 * @param side The side we're checking
	 * @return Whether we're preventing connections on given side or to given tileEntity
	 */
	public boolean isConnectionPrevented(TileEntity tile, ForgeDirection side)
	{
		return (!this.canConnectTo(tile, side)) || (isBlockedOnSide(side));
	}

	public byte getPossibleWireConnections()
	{
		byte connections = 0x00;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = new VectorWorld(this.tile()).add(side).getTileEntity();

			if (getConnector(tileEntity) != null && canConnectBothSides(tileEntity, side))
			{
				connections |= 1 << side.ordinal();
			}
		}

		return connections;
	}

	public byte getPossibleAcceptorConnections()
	{
		byte connections = 0x00;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = new VectorWorld(this.tile()).add(side).getTileEntity();

			if (canConnectTo(tileEntity, side) && canConnectBothSides(tileEntity, side))
			{
				connections |= 1 << side.ordinal();
			}
		}

		return connections;
	}

	/**
	 * Should include connections that are in the current connection maps even if those connections
	 * aren't allowed any more. This is so that networks split correctly.
	 */
	public TileEntity[] getConnections()
	{
		TileEntity[] connections = new TileEntity[6];

		if (world() != null)
		{
			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = new VectorWorld(this.tile()).add(side).getTileEntity();

				if (isCurrentlyConnected(side))
				{
					connections[i] = tileEntity;
				}
			}

		}
		return connections;
	}

	public boolean isCurrentlyConnected(ForgeDirection side)
	{
		return connectionMapContainsSide(getAllCurrentConnections(), side);
	}

	/**
	 * Shouldn't need to be overridden. Override connectionPrevented instead
	 */
	public boolean canConnect(ForgeDirection direction, Object source)
	{
		Vector3 connectPos = new Vector3(tile()).add(direction);
		TileEntity connectTile = connectPos.getTileEntity(world());
		return !isConnectionPrevented(connectTile, direction);
	}

	/**
	 * Packet Methods
	 */
	public void sendConnectionUpdate()
	{
		tile().getWriteStream(this).writeByte(0).writeByte(this.currentWireConnections).writeByte(this.currentAcceptorConnections);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		this.currentWireConnections = packet.readByte();
		this.currentAcceptorConnections = packet.readByte();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeByte(this.currentWireConnections);
		packet.writeByte(this.currentAcceptorConnections);
	}

	@Override
	public void read(MCDataInput packet)
	{
		read(packet, packet.readUByte());
	}

	@Override
	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 0)
		{
			this.currentWireConnections = packet.readByte();
			this.currentAcceptorConnections = packet.readByte();
			tile().markRender();
		}
		else
		{
			super.read(packet, packetID);
		}
	}

	@Override
	public String toString()
	{
		return "[PartFramedConnection]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
	}

}