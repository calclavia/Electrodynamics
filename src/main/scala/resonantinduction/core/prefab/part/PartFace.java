package resonantinduction.core.prefab.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroClass;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;

public abstract class PartFace extends JCuboidPart implements JNormalOcclusion, TFacePart
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

	/** Side of the block this is placed on. */
	public ForgeDirection placementSide;

	/** The relative direction this block faces. */
	public byte facing = 0;

	protected int ticks;

	public void preparePlacement(int side, int facing)
	{
		this.placementSide = ForgeDirection.getOrientation(side);
		this.facing = (byte) (facing - 2);
	}

	public void initiate()
	{
	}

	@Override
	public void update()
	{
		super.update();

		if (ticks++ == 0)
		{
			initiate();
		}
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		placementSide = ForgeDirection.getOrientation(packet.readByte());
		facing = packet.readByte();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(placementSide.ordinal());
		packet.writeByte(facing);
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
		return Arrays.asList(bounds[this.placementSide.ordinal()]);
	}

	@Override
	public boolean occlusionTest(TMultiPart npart)
	{
		return NormalOcclusionTest.apply(this, npart);
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());
		return drops;
	}

	protected abstract ItemStack getItem();

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return getItem();
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		placementSide = ForgeDirection.getOrientation(nbt.getByte("side"));
		facing = nbt.getByte("facing");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setByte("side", (byte) placementSide.ordinal());
		nbt.setByte("facing", facing);
	}

	/**
	 * Gets the relative direction of this block relative to the face it is on.
	 */
	public ForgeDirection getFacing()
	{
		return ForgeDirection.getOrientation(this.facing + 2);
	}

	public ForgeDirection getAbsoluteFacing()
	{
		int s = 0;

		switch (facing)
		{
			case 0:
				s = 2;
				break;
			case 1:
				s = 0;
				break;
			case 2:
				s = 1;
				break;
			case 3:
				s = 3;
				break;
		}

		int absDir = Rotation.rotateSide(placementSide.ordinal(), s);
		return ForgeDirection.getOrientation(absDir);
	}
}