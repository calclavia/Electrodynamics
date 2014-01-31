/**
 * 
 */
package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.battery.TileBattery;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.net.IConnector;
import calclavia.lib.network.IPacketReceiver;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroClass;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class PartMultimeter extends JCuboidPart implements IConnector<MultimeterNetwork>, TFacePart, JNormalOcclusion, IRedstonePart, IPacketReceiver
{
	public static Cuboid6[][] bounds = new Cuboid6[6][2];

	static
	{
		bounds[0][0] = new Cuboid6(1 / 8D, 0, 0, 7 / 8D, 1 / 8D, 1);
		bounds[0][1] = new Cuboid6(0, 0, 1 / 8D, 1, 1 / 8D, 7 / 8D);
		for (int s = 1; s < 6; s++)
		{
			Transformation t = Rotation.sideRotations[s].at(Vector3.center);
			bounds[s][0] = bounds[0][0].copy().apply(t);
			bounds[s][1] = bounds[0][1].copy().apply(t);
		}
	}

	public enum DetectMode
	{
		NONE("none"), LESS_THAN("lessThan"), LESS_THAN_EQUAL("lessThanOrEqual"), EQUAL("equal"),
		GREATER_THAN("greaterThanOrEqual"), GREATER_THAN_EQUAL("greaterThan");

		public String display;

		private DetectMode(String s)
		{
			display = s;
		}
	}

	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	private DetectMode detectMode = DetectMode.NONE;
	private long redstoneTriggerLimit;

	public boolean redstoneOn;
	private byte side;
	private int ticks;

	private MultimeterNetwork network;

	public void preparePlacement(int side, int itemDamage)
	{
		this.side = (byte) (side ^ 1);
	}

	public boolean hasMultimeter(int x, int y, int z)
	{
		return getMultimeter(x, y, z) != null;
	}

	public void refresh()
	{
		if (world() != null && !world().isRemote)
		{
			for (Object obj : getConnections())
			{
				if (obj instanceof PartMultimeter)
				{
					getNetwork().merge(((PartMultimeter) obj).getNetwork());
				}
			}

			getNetwork().reconstruct();
		}
	}

	public void updateDesc()
	{
		writeDesc(getWriteStream());
	}

	public void updateGraph()
	{
		writeGraph(getWriteStream());
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

	/**
	 * Gets the multimeter on the same plane.
	 */
	public PartMultimeter getMultimeter(int x, int y, int z)
	{
		TileEntity tileEntity = world().getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMultipart)
		{
			TMultiPart part = ((TileMultipart) tileEntity).partMap(side);

			if (part instanceof PartMultimeter)
			{
				return (PartMultimeter) part;
			}
		}

		return null;
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		player.openGui(Electrical.INSTANCE, side, world(), x(), y(), z());
		return true;
	}

	@Override
	public void update()
	{
		super.update();

		this.ticks++;
		getNetwork().addConnector(this);

		if (!world().isRemote)
		{
			long detectedEnergy = getDetectedEnergy();

			boolean outputRedstone = false;

			switch (detectMode)
			{
				default:
					break;
				case EQUAL:
					outputRedstone = detectedEnergy == redstoneTriggerLimit;
					break;
				case GREATER_THAN:
					outputRedstone = detectedEnergy > redstoneTriggerLimit;
					break;
				case GREATER_THAN_EQUAL:
					outputRedstone = detectedEnergy >= redstoneTriggerLimit;
					break;
				case LESS_THAN:
					outputRedstone = detectedEnergy < redstoneTriggerLimit;
					break;
				case LESS_THAN_EQUAL:
					outputRedstone = detectedEnergy <= redstoneTriggerLimit;
					break;
			}

			getNetwork().updateGraph(detectedEnergy, getDetectedCapacity());

			if (ticks % 10 == 0)
			{
				if (outputRedstone != redstoneOn)
				{
					redstoneOn = outputRedstone;
					tile().notifyPartChange(this);
				}

				if (getNetwork().valueGraph.get(1) != detectedEnergy)
				{
					updateGraph();
				}
			}
		}

		if (!world().isRemote)
		{
			for (EntityPlayer player : playersUsing)
			{
				updateGraph();
			}
		}
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		packet.readByte();
		this.side = packet.readByte();
		detectMode = DetectMode.values()[packet.readByte()];
		getNetwork().center = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
		getNetwork().size = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
		getNetwork().isEnabled = packet.readBoolean();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(0);
		packet.writeByte(this.side);
		packet.writeByte((byte) detectMode.ordinal());
		packet.writeNBTTagCompound(getNetwork().center.writeToNBT(new NBTTagCompound()));
		packet.writeNBTTagCompound(getNetwork().size.writeToNBT(new NBTTagCompound()));
		packet.writeBoolean(getNetwork().isEnabled);
	}

	public void writeGraph(MCDataOutput packet)
	{
		packet.writeByte(2);
		packet.writeNBTTagCompound(getNetwork().valueGraph.save());
		packet.writeNBTTagCompound(getNetwork().capacityGraph.save());
	}

	@Override
	public void read(MCDataInput packet)
	{
		read(packet, packet.readUByte());
	}

	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 0)
		{
			this.side = packet.readByte();
			detectMode = DetectMode.values()[packet.readByte()];
			getNetwork().center = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
			getNetwork().size = new universalelectricity.api.vector.Vector3(packet.readNBTTagCompound());
			getNetwork().isEnabled = packet.readBoolean();
			refresh();
		}
		else if (packetID == 1)
		{
			redstoneTriggerLimit = packet.readLong();
		}
		else if (packetID == 2)
		{
			getNetwork().valueGraph.load(packet.readNBTTagCompound());
			getNetwork().capacityGraph.load(packet.readNBTTagCompound());
		}
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		toggleMode();
	}

	public long getDetectedEnergy()
	{
		return getDetectedEnergy(getDirection().getOpposite(), getDetectedTile());
	}

	public TileEntity getDetectedTile()
	{
		ForgeDirection direction = getDirection();
		return world().getBlockTileEntity(x() + direction.offsetX, y() + direction.offsetY, z() + direction.offsetZ);
	}

	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.side);
	}

	public static long getDetectedEnergy(ForgeDirection side, TileEntity tileEntity)
	{
		if (tileEntity instanceof IConductor)
		{
			IConnector<IEnergyNetwork> conductor = ((IConductor) tileEntity).getInstance(side.getOpposite());

			if (conductor == null)
			{
				conductor = ((IConductor) tileEntity).getInstance(ForgeDirection.UNKNOWN);
			}

			if (conductor != null)
			{
				// TODO: Conductor may always return null in some cases.
				IEnergyNetwork network = conductor.getNetwork();
				return network.getLastBuffer();
			}
		}

		return CompatibilityModule.getEnergy(tileEntity, side);
	}

	public long getDetectedCapacity()
	{
		return getDetectedCapacity(getDirection().getOpposite(), getDetectedTile());
	}

	public static long getDetectedCapacity(ForgeDirection side, TileEntity tileEntity)
	{
		return CompatibilityModule.getMaxEnergy(tileEntity, side);
	}

	public void toggleMode()
	{
		if (!this.world().isRemote)
		{
			detectMode = DetectMode.values()[(detectMode.ordinal() + 1) % DetectMode.values().length];
		}
		else
		{
			PacketDispatcher.sendPacketToServer(ResonantInduction.PACKET_MULTIPART.getPacket(new universalelectricity.api.vector.Vector3(x(), y(), z()), side));
		}
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		side = nbt.getByte("side");
		detectMode = DetectMode.values()[nbt.getByte("detectMode")];
		redstoneTriggerLimit = nbt.getLong("energyLimit");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setByte("side", this.side);
		nbt.setByte("detectMode", (byte) detectMode.ordinal());
		nbt.setLong("energyLimit", redstoneTriggerLimit);
	}

	public DetectMode getMode()
	{
		return detectMode;
	}

	public float getLimit()
	{
		return redstoneTriggerLimit;
	}

	@Override
	public String getType()
	{
		return "resonant_induction_multimeter";
	}

	@Override
	public int getSlotMask()
	{
		return 1 << this.side;
	}

	@Override
	public Cuboid6 getBounds()
	{
		return FaceMicroClass.aBounds()[0x10 | this.side];
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return Arrays.asList(bounds[this.side]);
	}

	@Override
	public boolean occlusionTest(TMultiPart npart)
	{
		return NormalOcclusionTest.apply(this, npart);
	}

	@Override
	public int redstoneConductionMap()
	{
		return 0x1F;
	}

	@Override
	public boolean solid(int arg0)
	{
		return true;
	}

	protected ItemStack getItem()
	{
		return new ItemStack(Electrical.itemMultimeter);
	}

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

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderMultimeter.render(this, pos.x, pos.y, pos.z);
		}
	}

	@Override
	public boolean canConnectRedstone(int arg0)
	{
		return true;
	}

	@Override
	public int strongPowerLevel(int arg0)
	{
		return redstoneOn ? 14 : 0;
	}

	@Override
	public int weakPowerLevel(int arg0)
	{
		return redstoneOn ? 14 : 0;
	}

	@Override
	public MultimeterNetwork getNetwork()
	{
		if (network == null)
		{
			network = new MultimeterNetwork();
			network.addConnector(this);
		}

		return network;
	}

	@Override
	public void setNetwork(MultimeterNetwork network)
	{
		this.network = network;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public Object[] getConnections()
	{
		Object[] connections = new Object[6];

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			universalelectricity.api.vector.Vector3 vector = getPosition().translate(dir);

			if (hasMultimeter(vector.intX(), vector.intY(), vector.intZ()))
			{
				connections[dir.ordinal()] = getMultimeter(vector.intX(), vector.intY(), vector.intZ());
			}
		}

		return connections;
	}

	@Override
	public IConnector<MultimeterNetwork> getInstance(ForgeDirection dir)
	{
		return this;
	}

	public universalelectricity.api.vector.Vector3 getPosition()
	{
		return new universalelectricity.api.vector.Vector3(x(), y(), z());
	}

	/**
	 * Only one multimeter renders the text.
	 */
	public boolean isPrimaryRendering()
	{
		for (PartMultimeter m : getNetwork().getConnectors())
		{
			return m == this;
		}

		return false;
	}
}
