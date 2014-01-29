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
public class PartMultimeter extends JCuboidPart implements TFacePart, JNormalOcclusion, IPacketReceiver
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
	private long peakDetection;
	private long energyLimit;
	private long detectedEnergy;
	private long detectedAverageEnergy;
	public boolean redstoneOn;
	private byte side;
	private int ticks;

	public Graph graph;

	public void preparePlacement(int side, int itemDamage)
	{
		this.side = (byte) (side ^ 1);
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

		if (!world().isRemote)
		{
			if (ticks % 20 == 0)
			{
				long prevDetectedEnergy = detectedEnergy;
				updateDetection(doGetDetectedEnergy());

				boolean outputRedstone = false;

				switch (detectMode)
				{
					default:
						break;
					case EQUAL:
						outputRedstone = detectedEnergy == energyLimit;
						break;
					case GREATER_THAN:
						outputRedstone = detectedEnergy > energyLimit;
						break;
					case GREATER_THAN_EQUAL:
						outputRedstone = detectedEnergy >= energyLimit;
						break;
					case LESS_THAN:
						outputRedstone = detectedEnergy < energyLimit;
						break;
					case LESS_THAN_EQUAL:
						outputRedstone = detectedEnergy <= energyLimit;
						break;
				}

				if (outputRedstone != redstoneOn)
				{
					redstoneOn = outputRedstone;
					tile().notifyPartChange(this);
				}

				if (prevDetectedEnergy != detectedEnergy)
				{
					this.getWriteStream().writeByte(3).writeByte((byte) detectMode.ordinal()).writeLong(detectedEnergy).writeLong(detectedAverageEnergy).writeLong(energyLimit);
				}
			}
		}

		if (!world().isRemote)
		{
			for (EntityPlayer player : playersUsing)
			{
				this.getWriteStream().writeByte(3).writeByte((byte) detectMode.ordinal()).writeLong(detectedEnergy).writeLong(detectedAverageEnergy).writeLong(energyLimit);
			}
		}
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		this.side = packet.readByte();
		detectMode = DetectMode.values()[packet.readByte()];
		detectedEnergy = packet.readLong();
		detectedAverageEnergy = packet.readLong();
		energyLimit = packet.readLong();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(this.side);
		packet.writeByte((byte) detectMode.ordinal());
		packet.writeLong(detectedEnergy);
		packet.writeLong(detectedAverageEnergy);
		packet.writeLong(energyLimit);
	}

	@Override
	public void read(MCDataInput packet)
	{
		read(packet, packet.readUByte());
	}

	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 1)
		{
			energyLimit = packet.readLong();
		}
		else if (packetID == 3)
		{
			this.detectMode = DetectMode.values()[packet.readByte()];
			this.detectedEnergy = packet.readLong();
			this.detectedAverageEnergy = packet.readLong();
			this.energyLimit = packet.readLong();
		}
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		toggleMode();
	}

	public long doGetDetectedEnergy()
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

	public void updateDetection(long detected)
	{
		detectedEnergy = detected;
		detectedAverageEnergy = (detectedAverageEnergy + detectedEnergy) / 2;
		peakDetection = Math.max(peakDetection, detectedEnergy);
	}

	public long getDetectedEnergy()
	{
		return detectedEnergy;
	}

	public long getAverageDetectedEnergy()
	{
		return detectedAverageEnergy;
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
		energyLimit = nbt.getLong("energyLimit");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setByte("side", this.side);
		nbt.setByte("detectMode", (byte) detectMode.ordinal());
		nbt.setLong("energyLimit", energyLimit);
	}

	public DetectMode getMode()
	{
		return detectMode;
	}

	public float getLimit()
	{
		return energyLimit;
	}

	public float getPeak()
	{
		return peakDetection;
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

	// @Override
	public boolean canConnectRedstone(int arg0)
	{
		return true;
	}

	// @Override
	public int strongPowerLevel(int arg0)
	{
		return redstoneOn ? 14 : 0;
	}

	// @Override
	public int weakPowerLevel(int arg0)
	{
		return redstoneOn ? 14 : 0;
	}

}
