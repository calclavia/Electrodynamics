package resonantinduction.core.prefab.part;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import resonantinduction.mechanical.fluid.pipe.PartPipe;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.net.IConnector;
import universalelectricity.api.net.INetwork;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;

public abstract class PartFramedConnection<M extends Enum, C extends IConnector<N>, N extends INetwork> extends PartColorableMaterial<M> implements IConnector<N>, TSlottedPart, JNormalOcclusion, IHollowConnect, JIconHitEffects
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

	protected Object[] connections = new Object[6];

	protected N network;

	/**
	 * Bitmask connections
	 */
	public byte currentWireConnections = 0x00;
	public byte currentAcceptorConnections = 0x00;

	/** Client Side */
	private ForgeDirection testingSide;
	protected Icon breakIcon;

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
					subParts.add(currentSides[ord]);
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
	public void drawBreaking(RenderBlocks renderBlocks)
	{
		CCRenderState.reset();
		RenderUtils.renderBlock(sides[6], 0, new Translation(x(), y(), z()), new IconTransformation(renderBlocks.overrideBlockTexture), null);
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

	@Override
	public int getHollowSize()
	{
		return isInsulated ? 8 : 6;
	}

	@Override
	public Cuboid6 getBounds()
	{
		return new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625);
	}

	@Override
	public Icon getBreakingIcon(Object subPart, int side)
	{
		return breakIcon;
	}

	@Override
	public Icon getBrokenIcon(int side)
	{
		return breakIcon;
	}

	@Override
	public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer)
	{
		IconHitEffects.addHitEffects(this, hit, effectRenderer);
	}

	@Override
	public void addDestroyEffects(EffectRenderer effectRenderer)
	{
		IconHitEffects.addDestroyEffects(this, effectRenderer, false);
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

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return ((connections & tester) > 0);
	}

	@Override
	public void bind(TileMultipart t)
	{
		if (tile() != null && this.getNetwork() != null)
		{
			getNetwork().getConnectors().remove(tile());
			super.bind(t);
			getNetwork().getConnectors().add(getConnector(tile()));
		}
		else
		{
			super.bind(t);
		}
	}

	/**
	 * CONNECTION LOGIC CODE
	 */
	protected abstract boolean canConnectTo(TileEntity tile);

	protected abstract C getConnector(TileEntity tile);

	public boolean canConnectBothSides(TileEntity tile, ForgeDirection side)
	{
		boolean notPrevented = !isConnectionPrevented(tile, side);

		if (getConnector(tile) != null)
		{
			notPrevented &= getConnector(tile).canConnect(side.getOpposite());
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
		return (!this.canConnectTo(tile)) || (isBlockedOnSide(side));
	}

	public byte getPossibleWireConnections()
	{
		byte connections = 0x00;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);

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
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);

			if (canConnectTo(tileEntity) && canConnectBothSides(tileEntity, side))
			{
				connections |= 1 << side.ordinal();
			}
		}

		return connections;
	}

	public void refresh()
	{
		if (!world().isRemote)
		{
			byte possibleWireConnections = getPossibleWireConnections();
			byte possibleAcceptorConnections = getPossibleAcceptorConnections();

			if (possibleWireConnections != this.currentWireConnections)
			{
				byte or = (byte) (possibleWireConnections | this.currentWireConnections);

				// Connections have been removed
				if (or != possibleWireConnections)
				{
					this.getNetwork().removeConnector(this);
					this.getNetwork().split(getConnector(tile()));
					setNetwork(null);
				}

				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					if (connectionMapContainsSide(possibleWireConnections, side))
					{
						TileEntity tileEntity = VectorHelper.getConnectorFromSide(world(), new Vector3(tile()), side);

						if (getConnector(tileEntity) != null)
						{
							this.getNetwork().merge(getConnector(tileEntity).getNetwork());
						}
					}
				}

				this.currentWireConnections = possibleWireConnections;
			}

			this.currentAcceptorConnections = possibleAcceptorConnections;
			this.getNetwork().reconstruct();
			this.sendConnectionUpdate();
		}

		tile().markRender();
	}

	/**
	 * Should include connections that are in the current connection maps even if those connections
	 * aren't allowed any more. This is so that networks split correctly.
	 */
	@Override
	public TileEntity[] getConnections()
	{
		TileEntity[] connections = new TileEntity[6];

		if (world() != null && tile() != null)
		{
			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);

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
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (world().isBlockIndirectlyGettingPowered(x(), y(), z()))
		{
			return false;
		}

		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(world());
		return !isConnectionPrevented(connectTile, direction);
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

	public void copyFrom(PartFramedConnection<M, C, N> other)
	{
		this.isInsulated = other.isInsulated;
		this.color = other.color;
		this.connections = other.connections;
		this.material = other.material;
		this.currentWireConnections = other.currentWireConnections;
		this.currentAcceptorConnections = other.currentAcceptorConnections;
		this.setNetwork(other.getNetwork());
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

	/**
	 * Network Methods
	 */
	@Override
	public void setNetwork(N network)
	{
		this.network = network;
	}

}