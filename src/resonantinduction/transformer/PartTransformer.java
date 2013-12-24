package resonantinduction.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.multimeter.PartMultimeter.DetectMode;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroClass;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TFacePart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartTransformer extends JCuboidPart implements JNormalOcclusion, TFacePart
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

	public int side;
	private boolean stepUp;
	public int multiplier;

	public void preparePlacement(int side, int itemDamage)
	{
		this.side = (byte) (side ^ 1);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		this.side = packet.readByte();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(this.side);
	}

	public boolean stepUp()
	{
		return this.stepUp;
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
		return Arrays.asList(oBoxes[this.side]);
	}

	protected ItemStack getItem()
	{
		return new ItemStack(ResonantInduction.itemTransformer);
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
			RenderTransformer.render(this, pos.x, pos.y, pos.z);
		}
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		this.side = nbt.getByte("side");
		this.stepUp = nbt.getBoolean("stepUp");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setByte("side", (byte) this.side);
		nbt.setBoolean("stepUp", this.stepUp);
	}

	@Override
	public String getType()
	{
		return "resonant_induction_transformer";
	}
}