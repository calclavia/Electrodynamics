package resonantinduction.mechanical.gear;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.PartMechanical;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.PartMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * We assume all the force acting on the gear is 90 degrees.
 * 
 * @author Calclavia
 * 
 */
public class PartGearShaft extends PartMechanical
{
	public static Cuboid6[] sides = new Cuboid6[7];

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

	public void preparePlacement(int side, int itemDamage)
	{
		ForgeDirection dir = ForgeDirection.getOrientation((byte) (side ^ 1));
		this.placementSide = ForgeDirection.getOrientation(!(dir.ordinal() % 2 == 0) ? dir.ordinal() - 1 : dir.ordinal());
	}

	@Override
	public void update()
	{
		super.update();

		if (!this.world().isRemote)
		{
			// Decelerate the gear.
			torque *= 0.95f;
			angularVelocity *= 0.95f;
		}
	}

	/**
	 * Refresh should be called sparingly.
	 */
	public void refresh()
	{
		connections = new Object[6];

		/** Check for internal connections, the FRONT and BACK. */
		for (int i = 0; i < 6; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(i);

			if (checkDir == placementSide || checkDir == placementSide.getOpposite())
			{
				IMechanical instance = ((IMechanical) tile()).getInstance(checkDir);

				if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite(), this))
				{
					connections[checkDir.ordinal()] = instance;
					getNetwork().merge(instance.getNetwork());
				}
			}
		}

		/** Look for connections outside this block space, the relative FRONT and BACK */
		for (int i = 0; i < 6; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(i);

			if (connections[checkDir.ordinal()] == null && (checkDir == placementSide || checkDir == placementSide.getOpposite()))
			{
				TileEntity checkTile = new universalelectricity.api.vector.Vector3(tile()).translate(checkDir).getTileEntity(world());

				if (checkTile instanceof IMechanical)
				{
					IMechanical instance = (IMechanical) ((IMechanical) checkTile).getInstance(checkDir.getOpposite());

					// Only connect to shafts outside of this block space.
					if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite(), this) && instance instanceof PartGearShaft)
					{
						connections[checkDir.ordinal()] = instance;
						getNetwork().merge(instance.getNetwork());
					}
				}
			}
		}

		getNetwork().reconstruct();
	}

	@Override
	protected ItemStack getItem()
	{
		return new ItemStack(Mechanical.itemGearShaft);
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

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		return from == placementSide || from == placementSide.getOpposite();
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
		return Arrays.asList(sides);
	}

	@Override
	public Cuboid6 getBounds()
	{
		return new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625);
	}

	@Override
	public boolean inverseRotation(ForgeDirection dir, IMechanical with)
	{
		return dir == placementSide.getOpposite() && !(with instanceof PartGearShaft);
	}

}