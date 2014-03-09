package resonantinduction.quantum.gate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import resonantinduction.core.Reference;
import resonantinduction.electrical.Electrical;
import calclavia.lib.render.RenderUtility;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartQuantumGlyph extends JCuboidPart implements JNormalOcclusion
{
	static final Cuboid6[] bounds = new Cuboid6[8];

	static
	{
		bounds[0] = new Cuboid6(0, 0, 0, 0.5, 0.5, 0.5);
		bounds[1] = new Cuboid6(0, 0, 0.5, 0.5, 0.5, 1);
		bounds[2] = new Cuboid6(0.5, 0, 0, 1, 0.5, 0.5);
		bounds[3] = new Cuboid6(0.5, 0, 0.5, 1, 0.5, 1);

		bounds[4] = new Cuboid6(0, 0.5, 0, 0.5, 1, 0.5);
		bounds[5] = new Cuboid6(0, 0.5, 0.5, 0.5, 1, 1);
		bounds[6] = new Cuboid6(0.5, 0.5, 0, 1, 1, 0.5);
		bounds[7] = new Cuboid6(0.5, 0.5, 0.5, 1, 1, 1);
	}

	private byte side;
	byte number;

	public void preparePlacement(int side, int itemDamage)
	{
		this.side = (byte) side;
		this.number = (byte) itemDamage;
	}

	@Override
	public String getType()
	{
		return "resonant_induction_quantum_glyph";
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		RenderQuantumGlyph.INSTANCE.render(this, pos.x, pos.y, pos.z);
	}

	@Override
	public Cuboid6 getBounds()
	{
		if (side < bounds.length)
			if (bounds[side] != null)
				return bounds[side];

		return new Cuboid6(0, 0, 0, 0.5, 0.5, 0.5);
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return Arrays.asList(new Cuboid6[] { getBounds() });
	}

	protected ItemStack getItem()
	{
		return new ItemStack(Electrical.itemQuantumGlyph);
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

	/** Packet Code. */
	@Override
	public void readDesc(MCDataInput packet)
	{
		load(packet.readNBTTagCompound());
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		// packet.writeByte(0);
		NBTTagCompound nbt = new NBTTagCompound();
		save(nbt);
		packet.writeNBTTagCompound(nbt);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		side = nbt.getByte("side");
		number = nbt.getByte("number");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setByte("side", side);
		nbt.setByte("number", number);
	}

}
