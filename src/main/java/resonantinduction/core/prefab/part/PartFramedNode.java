package resonantinduction.core.prefab.part;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import calclavia.lib.grid.INode;
import calclavia.lib.grid.INodeProvider;
import calclavia.lib.grid.Node;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class PartFramedNode<M extends Enum, N extends Node, T extends INodeProvider> extends PartColorableMaterial<M> implements INodeProvider, TSlottedPart, JNormalOcclusion, IHollowConnect, JIconHitEffects
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

	protected N node;

	/**
	 * Bitmask connections
	 */
	public byte currentConnections = 0x00;

	/** Client Side */
	private ForgeDirection testingSide;

	@SideOnly(Side.CLIENT)
	protected Icon breakIcon;

	public PartFramedNode(Item insulationType)
	{
		super(insulationType);
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
		if (breakIcon != null)
		{
			CCRenderState.reset();
			RenderUtils.renderBlock(sides[6], 0, new Translation(x(), y(), z()), new IconTransformation(breakIcon), null);
		}
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
		return (currentConnections);
	}

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return ((connections & tester) > 0);
	}

	@Override
	public void bind(TileMultipart t)
	{
		node.deconstruct();
		super.bind(t);
		node.reconstruct();
	}

	public boolean isCurrentlyConnected(ForgeDirection side)
	{
		return connectionMapContainsSide(getAllCurrentConnections(), side);
	}

	@Override
	public void onWorldJoin()
	{
		node.reconstruct();
	}

	@Override
	public void onNeighborChanged()
	{
		node.reconstruct();
	}

	@Override
	public void onWorldSeparate()
	{
		node.deconstruct();
	}

	public void copyFrom(PartFramedNode<M, N, T> other)
	{
		this.isInsulated = other.isInsulated;
		this.color = other.color;
		this.connections = other.connections;
		this.material = other.material;
	}

	/** Packet Methods */
	public void sendConnectionUpdate()
	{
		tile().getWriteStream(this).writeByte(0).writeByte(currentConnections);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		currentConnections = packet.readByte();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeByte(currentConnections);
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
			currentConnections = packet.readByte();
			tile().markRender();
		}
		else
		{
			super.read(packet, packetID);
		}
	}

	@SuppressWarnings("hiding")
	@Override
	public <N extends INode> N getNode(Class<? super N> nodeType, ForgeDirection from)
	{
		if (nodeType.isAssignableFrom(node.getClass()))
			return (N) node;
		return null;
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		node.save(nbt);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		node.load(nbt);
	}
}