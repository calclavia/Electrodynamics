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

public class PartGear extends JCuboidPart implements JNormalOcclusion, TFacePart, IMechanical
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
	/** Side of the block this is placed on */
	private ForgeDirection placementSide;

	/** Positive torque means it is spinning clockwise */
	private long torque = 0;

	public void preparePlacement(int side, int itemDamage)
	{
		this.placementSide = ForgeDirection.getOrientation((byte) (side ^ 1));
	}

	@Override
	public void update()
	{
		// TODO: Should we average the torque?
		/**
		 * Look for gears that are back-to-back with this gear. Equate torque.
		 */
		universalelectricity.api.vector.Vector3 vec = new universalelectricity.api.vector.Vector3(tile()).modifyPositionFromSide(placementSide);

		TileEntity tile = vec.getTileEntity(world());

		if (tile instanceof TileMultipart)
		{
			TMultiPart part = ((TileMultipart) tile).partMap(this.placementSide.getOpposite().ordinal());

			if (part instanceof PartGear)
			{
				torque = (torque + ((PartGear) part).torque) / 2;
				((PartGear) part).torque = torque;
			}
		}

		/**
		 * Look for gears outside this block space, the relative UP, DOWN, LEFT, RIGHT
		 */
		for (int i = 0; i < 4; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(Rotation.rotateSide(this.placementSide.ordinal(), i));
			universalelectricity.api.vector.Vector3 checkVec = new universalelectricity.api.vector.Vector3(tile()).modifyPositionFromSide(checkDir);

			TileEntity checkTile = checkVec.getTileEntity(world());

			if (checkTile instanceof TileMultipart)
			{
				TMultiPart neighbor = ((TileMultipart) checkTile).partMap(this.placementSide.ordinal());

				if (neighbor instanceof PartGear)
				{
					torque = (torque - ((PartGear) neighbor).torque) / 2;
					((PartGear) neighbor).torque = -torque;
				}
			}
		}

		/**
		 * Look for gears that are internal and adjacent to this gear. (The 2 sides)
		 */
		for (int i = 0; i < 6; i++)
		{
			// TODO: Make it work with UP-DOWN
			if (i < 2)
			{
				TMultiPart neighbor = tile().partMap(this.placementSide.getRotation(ForgeDirection.getOrientation(i)).ordinal());

				if (neighbor instanceof PartGear)
				{
					torque = (torque - ((PartGear) neighbor).torque) / 2;
					((PartGear) neighbor).torque = -torque;
				}
			}
		}
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		System.out.println("Torque" + this.torque);

		if (player.isSneaking())
		{
			this.torque += 10;
		}

		return false;
	}

	/**
	 * Packet Code.
	 */
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
	public long getTorque()
	{
		return this.torque;
	}

	@Override
	public void setTorque(long torque)
	{
		this.torque = torque;
	}

}