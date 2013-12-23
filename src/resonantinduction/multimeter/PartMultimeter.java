/**
 * 
 */
package resonantinduction.multimeter;

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

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.base.PartAdvanced;
import universalelectricity.api.CompatibilityType;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.net.IConnectable;
import buildcraft.api.power.IPowerReceptor;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.TileEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class PartMultimeter extends PartAdvanced implements IConnectable, TFacePart, JNormalOcclusion
{
	public static Cuboid6[] bounds = new Cuboid6[6];

	static
	{
		// Subtract the box a little because we'd like things like posts to get first hit
		Cuboid6 selection = new Cuboid6(0, 0, 0, 1, (2) / 16D, 1).expand(-0.005);
		for (int s = 0; s < 6; s++)
		{
			bounds[s] = selection.copy().apply(Rotation.sideRotations[s].at(Vector3.center));
		}
	}

	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public enum DetectMode
	{
		NONE("None"), LESS_THAN("Less Than"), LESS_THAN_EQUAL("Less Than or Equal"),
		EQUAL("Equal"), GREATER_THAN("Greater Than or Equal"), GREATER_THAN_EQUAL("Greater Than");

		public String display;

		private DetectMode(String s)
		{
			display = s;
		}
	}

	private DetectMode detectMode = DetectMode.NONE;
	private long peakDetection;
	private long energyLimit;
	private long detectedEnergy;
	private long detectedAverageEnergy;
	public boolean redstoneOn;
	private byte side;

	public void preparePlacement(int side, int itemDamage)
	{
		this.side = (byte) (side ^ 1);
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		player.openGui(ResonantInduction.INSTANCE, this.side, world(), x(), y(), z());
		return true;
	}

	@Override
	public void update()
	{
		super.update();

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
					this.tile().notifyTileChange();
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
		if (packetID == 0)
		{
			toggleMode();
		}
		else if (packetID == 1)
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

	public long doGetDetectedEnergy()
	{
		ForgeDirection direction = getDirection();
		TileEntity tileEntity = world().getBlockTileEntity(x() + direction.offsetX, y() + direction.offsetY, z() + direction.offsetZ);
		return getDetectedEnergy(direction.getOpposite(), tileEntity);
	}

	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.side);
	}

	// TODO: Check if side is correct.
	public static long getDetectedEnergy(ForgeDirection side, TileEntity tileEntity)
	{
		if (tileEntity instanceof IEnergyContainer)
		{
			return ((IEnergyContainer) tileEntity).getEnergy(side);
		}
		else if (tileEntity instanceof IConductor)
		{
			IEnergyNetwork network = ((IConductor) tileEntity).getNetwork();
			return network.getLastBuffer();
		}
		else if (tileEntity instanceof IEnergyHandler)
		{
			return (long) (((IEnergyHandler) tileEntity).getEnergyStored(side) * CompatibilityType.INDUSTRIALCRAFT.reciprocal_ratio);
		}
		else if (tileEntity instanceof TileEnergyHandler)
		{
			return (long) (((TileEnergyHandler) tileEntity).getEnergyStored(side.getOpposite()) * CompatibilityType.THERMAL_EXPANSION.reciprocal_ratio);
		}
		else if (tileEntity instanceof IPowerReceptor)
		{
			if (((IPowerReceptor) tileEntity).getPowerReceiver(side) != null)
			{
				return (long) (((IPowerReceptor) tileEntity).getPowerReceiver(side).getEnergyStored() * CompatibilityType.BUILDCRAFT.reciprocal_ratio);
			}
		}

		return 0;
	}

	public void updateDetection(long detected)
	{
		detectedEnergy = detected;
		detectedAverageEnergy = (detectedAverageEnergy + detectedEnergy) / 2;
		peakDetection = Math.max(peakDetection, detectedEnergy);
	}

	public float getDetectedEnergy()
	{
		return detectedEnergy;
	}

	public float getAverageDetectedEnergy()
	{
		return detectedAverageEnergy;
	}

	public void toggleMode()
	{
		detectMode = DetectMode.values()[(detectMode.ordinal() + 1) % DetectMode.values().length];
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
		nbt.setByte("side", (byte) this.side);
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
	public boolean canConnect(ForgeDirection direction)
	{
		return direction == getDirection();
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
	public Iterable<IndexedCuboid6> getSubParts()
	{
		return Arrays.asList(new IndexedCuboid6(0, bounds[side]));
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
		return 0;
	}

	@Override
	public boolean solid(int arg0)
	{
		return true;
	}

	protected ItemStack getItem()
	{
		return new ItemStack(ResonantInduction.itemMultimeter);
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

}
