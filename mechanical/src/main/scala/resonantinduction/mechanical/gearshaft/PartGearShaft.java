package resonantinduction.mechanical.gearshaft;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.energy.grid.PartMechanical;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.PartMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * We assume all the force acting on the gear is 90 degrees.
 *
 * @author Calclavia
 */
public class PartGearShaft extends PartMechanical
{
	public static IndexedCuboid6[] sides = new IndexedCuboid6[7];

	static
	{
		sides[0] = new IndexedCuboid6(0, new Cuboid6(0.36, 0.000, 0.36, 0.64, 0.36, 0.64));
		sides[1] = new IndexedCuboid6(1, new Cuboid6(0.36, 0.64, 0.36, 0.64, 1.000, 0.64));
		sides[2] = new IndexedCuboid6(2, new Cuboid6(0.36, 0.36, 0.000, 0.64, 0.64, 0.36));
		sides[3] = new IndexedCuboid6(3, new Cuboid6(0.36, 0.36, 0.64, 0.64, 0.64, 1.000));
		sides[4] = new IndexedCuboid6(4, new Cuboid6(0.000, 0.36, 0.36, 0.36, 0.64, 0.64));
		sides[5] = new IndexedCuboid6(5, new Cuboid6(0.64, 0.36, 0.36, 1.000, 0.64, 0.64));
		sides[6] = new IndexedCuboid6(6, new Cuboid6(0.36, 0.36, 0.36, 0.64, 0.64, 0.64));
	}

	public PartGearShaft()
	{
		super();
		node = new GearShaftNode(this);
	}

	@Override
	public void preparePlacement(int side, int itemDamage)
	{
		ForgeDirection dir = ForgeDirection.getOrientation((byte) (side ^ 1));
		// Unwind rotation. We can only have "3" axis.
		this.placementSide = ForgeDirection.getOrientation(!(dir.ordinal() % 2 == 0) ? dir.ordinal() - 1 : dir.ordinal());
		tier = itemDamage;
	}

	@Override
	protected ItemStack getItem()
	{
		return new ItemStack(Mechanical.itemGearShaft, 1, tier);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderGearShaft.INSTANCE.renderDynamic(this, pos.x, pos.y, pos.z, frame);
		}
	}

	@Override
	public String getType()
	{
		return "resonant_induction_gear_shaft";
	}

	/**
	 * Multipart Bounds
	 */
	@Override
	public int getSlotMask()
	{
		return PartMap.CENTER.mask;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return getCollisionBoxes();
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		Set<Cuboid6> collisionBoxes = new HashSet<Cuboid6>();
		collisionBoxes.addAll((Collection<? extends Cuboid6>) getSubParts());

		return collisionBoxes;
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts()
	{
		Set<IndexedCuboid6> subParts = new HashSet<IndexedCuboid6>();
		IndexedCuboid6[] currentSides = sides;

		if (tile() != null)
		{
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if (side == placementSide || side == placementSide.getOpposite())
				{
					subParts.add(currentSides[side.ordinal()]);
				}
			}
		}

		subParts.add(currentSides[6]);
		return subParts;
	}

	@Override
	public Cuboid6 getBounds()
	{
		return new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625);
	}

}