package resonantinduction.mechanical.gear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalConnector;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import resonantinduction.mechanical.network.MechanicalNetwork;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * We assume all the force acting on the gear is 90 degrees.
 * 
 * @author Calclavia
 * 
 */
public class PartGear extends JCuboidPart implements JNormalOcclusion, TFacePart, IMechanical, IMechanicalConnector
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

	/** Side of the block this is placed on */
	public ForgeDirection placementSide;

	/** The size of the gear */
	private float radius = 0.5f;

	public boolean isClockwise = true;

	/** The current angle the gear is on. In radians per second. */
	public float angle = 0;

	/** When true, it will start marking nearby gears for update */
	public boolean markRotationUpdate = true;

	/** The mechanical connections this gear has made */
	protected Object[] connections = new Object[6];

	private int manualCrankTime = 0;

	public void preparePlacement(int side, int itemDamage)
	{
		this.placementSide = ForgeDirection.getOrientation((byte) (side ^ 1));
	}

	@Override
	public void update()
	{
		if (manualCrankTime > 0)
		{
			onReceiveEnergy(null, 20, 0.3f);
			manualCrankTime--;
		}

		if (markRotationUpdate)
		{
			refresh();
		}

	}

	@Override
	public void networkUpdate()
	{
		/**
		 * Update angle rotation.
		 */
		if (isClockwise)
			angle += this.getNetwork().getAngularVelocity() / 20;
		else
			angle -= this.getNetwork().getAngularVelocity() / 20;
	}

	public void refresh()
	{
		/** Look for gears that are back-to-back with this gear. Equate torque. */
		universalelectricity.api.vector.Vector3 vec = new universalelectricity.api.vector.Vector3(tile()).modifyPositionFromSide(placementSide);

		TileEntity tile = vec.getTileEntity(world());

		if (tile instanceof TileMultipart)
		{
			TMultiPart neighbor = ((TileMultipart) tile).partMap(this.placementSide.getOpposite().ordinal());

			if (neighbor instanceof PartGear)
			{
				connections[this.placementSide.getOpposite().ordinal()] = neighbor;
				getNetwork().merge(((PartGear) neighbor).getNetwork());
				equateRotation((PartGear) neighbor, false);
			}
		}
		else if (tile instanceof IMechanical)
		{
			connections[this.placementSide.getOpposite().ordinal()] = tile;
			getNetwork().reconstruct();
		}

		/** Look for gears outside this block space, the relative UP, DOWN, LEFT, RIGHT */
		for (int i = 0; i < 4; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(Rotation.rotateSide(this.placementSide.ordinal(), i));
			universalelectricity.api.vector.Vector3 checkVec = new universalelectricity.api.vector.Vector3(tile()).modifyPositionFromSide(checkDir);

			TileEntity checkTile = checkVec.getTileEntity(world());

			if (checkTile instanceof TileMultipart)
			{
				TMultiPart neighbor = ((TileMultipart) checkTile).partMap(this.placementSide.ordinal());

				if (neighbor != this && neighbor instanceof PartGear)
				{
					connections[checkDir.ordinal()] = neighbor;
					getNetwork().merge(((PartGear) neighbor).getNetwork());
					equateRotation((PartGear) neighbor, false);
				}
			}
		}

		/** Look for gears that are internal and adjacent to this gear. (The 2 sides) */
		for (int i = 0; i < 4; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(i);
			TMultiPart neighbor = tile().partMap(this.placementSide.getRotation(checkDir).ordinal());

			if (neighbor != this && neighbor instanceof PartGear)
			{
				connections[checkDir.ordinal()] = neighbor;
				getNetwork().merge(((PartGear) neighbor).getNetwork());
				equateRotation((PartGear) neighbor, false);
			}
		}

		markRotationUpdate = false;
	}

	@Override
	public Object[] getConnections()
	{
		return connections;
	}

	public void equateRotation(PartGear neighbor, boolean isPositive)
	{
		if (!neighbor.markRotationUpdate)
		{
			if (isPositive)
			{
				((PartGear) neighbor).isClockwise = isClockwise;
			}
			else
			{
				((PartGear) neighbor).isClockwise = !isClockwise;
			}

			neighbor.markRotationUpdate = true;
		}
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		System.out.println(this.getNetwork().getAngularVelocity());
		if (player.isSneaking())
		{
			this.manualCrankTime = 20;
		}

		return false;
	}

	public void onReceiveEnergy(ForgeDirection from, long torque, float angularVelocity)
	{
		getNetwork().applyEnergy(torque, angularVelocity);
		markRotationUpdate = true;

		if (!world().isRemote)
			this.sendRotationUpdate(torque, angularVelocity);
	}

	@Override
	public void preRemove()
	{
		if (!world().isRemote)
		{
			this.getNetwork().split(this);
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

	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 0)
		{
			onReceiveEnergy(null, packet.readLong(), packet.readFloat());
		}
	}

	public void sendRotationUpdate(long torque, float angularVelocity)
	{
		tile().getWriteStream(this).writeByte(0).writeLong(torque).writeFloat(angularVelocity);
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

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return Arrays.asList(oBoxes[this.placementSide.ordinal()]);
	}

	protected ItemStack getItem()
	{
		return new ItemStack(Mechanical.itemGear);
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
			RenderGear.INSTANCE.renderDynamic(this, pos.x, pos.y, pos.z);
		}
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

	@Override
	public String getType()
	{
		return "resonant_induction_gear";
	}

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
		return new universalelectricity.api.vector.Vector3(this.x() + direction.offsetX, this.y() + direction.offsetY, this.z() + direction.offsetZ).getTileEntity(this.world()) instanceof IMechanicalConnector;
	}

}