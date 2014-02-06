package resonantinduction.mechanical.gear;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.PartMechanical;
import calclavia.lib.multiblock.reference.IMultiBlockStructure;
import calclavia.lib.multiblock.reference.MultiBlockHandler;
import calclavia.lib.utility.WrenchUtility;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroClass;
import codechicken.multipart.ControlKeyModifer;
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
public class PartGear extends PartMechanical implements IMechanical, IMultiBlockStructure<PartGear>
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

	private int manualCrankTime = 0;

	@Override
	public void update()
	{
		super.update();

		if (!this.world().isRemote)
		{
			if (manualCrankTime > 0)
			{
				if (angularVelocity > 0)
				{
					torque += 1;
					angularVelocity += 0.01f;
				}
				else
				{
					torque -= 1;
					angularVelocity -= 0.01f;
				}

				manualCrankTime--;
			}

			if (getMultiBlock().isPrimary())
			{
				// Decelerate the gear based on tier.
				switch (tier)
				{
					default:
						torque *= 0.9f;
						angularVelocity *= 0.95f;
						break;
					case 1:
						torque *= 0.95f;
						angularVelocity *= 0.9f;
						break;
					case 2:
						torque *= 0.99f;
						angularVelocity *= 0.99f;
						break;
				}
			}
			else
			{
				torque = 0;
				angularVelocity = 0;
			}
		}

		getMultiBlock().update();
	}

	@Override
	public void checkClientUpdate()
	{
		if (getMultiBlock().isPrimary())
			super.checkClientUpdate();
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		if (WrenchUtility.isUsableWrench(player, player.getCurrentEquippedItem(), x(), y(), z()))
		{
			if (player.isSneaking())
			{
				getMultiBlock().toggleConstruct();
			}
			else if (ControlKeyModifer.isControlDown(player))
			{
				if (!world().isRemote)
				{
					getMultiBlock().get().angularVelocity = -getMultiBlock().get().angularVelocity;
					player.addChatMessage("Flipped gear to rotate " + (angularVelocity > 0 ? "clockwise" : "anticlockwise") + ".");
				}
			}
			else
			{
				getMultiBlock().get().manualCrankTime = 10;
				world().playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, Reference.PREFIX + "gearCrank", 0.5f, 0.9f + world().rand.nextFloat() * 0.2f);
				player.addExhaustion(0.01f);
			}

			WrenchUtility.damageWrench(player, player.getCurrentEquippedItem(), x(), y(), z());
			return true;
		}
		else if (player.isSneaking())
		{
			if (!world().isRemote)
			{
				getMultiBlock().get().angularVelocity = -getMultiBlock().get().angularVelocity;
				player.addChatMessage("Flipped gear to rotate " + (angularVelocity > 0 ? "clockwise" : "anticlockwise") + ".");
			}
		}

		return super.activate(player, hit, item);
	}

	@Override
	public void preRemove()
	{
		super.preRemove();
		getMultiBlock().deconstruct();
	}

	/**
	 * Refresh should be called sparingly.
	 */
	@Override
	public void refresh()
	{
		connections = new Object[6];

		/**
		 * Only call refresh if this is the main block of a multiblock gear or a single gear block.
		 */
		if (!getMultiBlock().isPrimary())
		{
			return;
		}

		/** Look for gears that are back-to-back with this gear. Equate torque. */
		TileEntity tileBehind = new universalelectricity.api.vector.Vector3(tile()).translate(placementSide).getTileEntity(world());

		if (tileBehind instanceof IMechanical)
		{
			IMechanical instance = ((IMechanical) tileBehind).getInstance(placementSide.getOpposite());

			if (instance != null && instance != this && instance.canConnect(placementSide.getOpposite(), this))
			{
				connections[placementSide.getOpposite().ordinal()] = instance;
				getNetwork().merge(instance.getNetwork());
			}

		}

		/**
		 * Look for gears that are internal and adjacent to this gear. (The 4 sides + the internal
		 * center)
		 */
		for (int i = 0; i < 6; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(i);

			TileEntity tile = tile();

			if (getMultiBlock().isConstructed() && checkDir != placementSide && checkDir != placementSide.getOpposite())
			{
				tile = new universalelectricity.api.vector.Vector3(tile()).translate(checkDir).getTileEntity(world());
				System.out.println("MOIFIED" + checkDir);
			}

			if (tile instanceof IMechanical)
			{
				/**
				 * If we're checking for the block that is opposite to the gear's placement side
				 * (the center), then we try to look for a gear shaft in the center.
				 */
				IMechanical instance = ((IMechanical) tile).getInstance(checkDir == placementSide.getOpposite() ? ForgeDirection.UNKNOWN : checkDir);

				if (connections[checkDir.ordinal()] == null && instance != this && checkDir != placementSide && instance != null && instance.canConnect(checkDir.getOpposite(), this))
				{
					System.out.println("F" + instance);

					connections[checkDir.ordinal()] = instance;
					getNetwork().merge(instance.getNetwork());
				}
			}
		}

		int displaceCheck = 1;

		if (getMultiBlock().isPrimary() && getMultiBlock().isConstructed())
		{
			displaceCheck = 2;
		}

		/** Look for gears outside this block space, the relative UP, DOWN, LEFT, RIGHT */
		for (int i = 0; i < 4; i++)
		{
			ForgeDirection checkDir = ForgeDirection.getOrientation(Rotation.rotateSide(this.placementSide.ordinal(), i));
			TileEntity checkTile = new universalelectricity.api.vector.Vector3(tile()).translate(checkDir, displaceCheck).getTileEntity(world());

			if (connections[checkDir.ordinal()] == null && checkTile instanceof IMechanical)
			{
				IMechanical instance = ((IMechanical) checkTile).getInstance(placementSide);

				if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite(), this) && !(instance instanceof PartGearShaft))
				{
					connections[checkDir.ordinal()] = instance;
					getNetwork().merge(instance.getNetwork());
				}
			}
		}

		getNetwork().reconstruct();
	}

	/**
	 * Is this gear block the one in the center-edge of the multiblock that can interact with other
	 * gears?
	 * 
	 * @return
	 */
	public boolean isCenterMultiBlock()
	{
		if (!getMultiBlock().isConstructed())
		{
			return false;
		}

		universalelectricity.api.vector.Vector3 primaryPos = getMultiBlock().getPrimary().getPosition();

		if (primaryPos.intX() == x() && placementSide.offsetX == 0)
		{
			return true;
		}

		if (primaryPos.intY() == y() && placementSide.offsetY == 0)
		{
			return true;
		}

		if (primaryPos.intZ() == z() && placementSide.offsetZ == 0)
		{
			return true;
		}

		return false;
	}

	@Override
	public Object[] getConnections()
	{
		if (!getMultiBlock().isPrimary())
		{
			return new Object[6];
		}

		return connections;
	}

	@Override
	protected ItemStack getItem()
	{
		return new ItemStack(Mechanical.itemGear, 1, tier);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderGear.INSTANCE.renderDynamic(this, pos.x, pos.y, pos.z, tier);
		}
	}

	@Override
	public String getType()
	{
		return "resonant_induction_gear";
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		getMultiBlock().load(nbt);
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		getMultiBlock().save(nbt);
	}

	/**
	 * Multiblock
	 */
	private GearMultiBlockHandler multiBlock;

	@Override
	public universalelectricity.api.vector.Vector3[] getMultiBlockVectors()
	{
		Set<universalelectricity.api.vector.Vector3> vectors = new HashSet<universalelectricity.api.vector.Vector3>();
		ForgeDirection dir = placementSide;

		universalelectricity.api.vector.Vector3 rotationalAxis = universalelectricity.api.vector.Vector3.UP();

		if (placementSide == ForgeDirection.NORTH || placementSide == ForgeDirection.SOUTH)
		{
			rotationalAxis = universalelectricity.api.vector.Vector3.EAST();
		}
		else if (placementSide == ForgeDirection.WEST || placementSide == ForgeDirection.EAST)
		{
			rotationalAxis = universalelectricity.api.vector.Vector3.SOUTH();
		}

		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				universalelectricity.api.vector.Vector3 vector = new universalelectricity.api.vector.Vector3(x, 0, z);
				vector.rotate(90, rotationalAxis);
				vector = vector.round();
				vectors.add(vector);
			}
		}

		return vectors.toArray(new universalelectricity.api.vector.Vector3[0]);
	}

	@Override
	public World getWorld()
	{
		return world();
	}

	@Override
	public void onMultiBlockChanged()
	{
		if (world() != null)
		{
			tile().notifyPartChange(this);

			if (!world().isRemote)
			{
				sendDescUpdate();
			}
		}
	}

	@Override
	public MultiBlockHandler<PartGear> getMultiBlock()
	{
		if (multiBlock == null)
			multiBlock = new GearMultiBlockHandler(this);

		return multiBlock;
	}

	@Override
	public float getRatio(ForgeDirection dir)
	{
		if (dir == placementSide)
		{
			return super.getRatio(dir);
		}

		return getMultiBlock().isConstructed() ? 1.5f : super.getRatio(dir);
	}

	@Override
	public IMechanical getInstance(ForgeDirection from)
	{
		return getMultiBlock().get();
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		if (!getMultiBlock().isPrimary())
		{
			return false;
		}

		if (source instanceof IMechanical)
		{
			/**
			 * Check for flat connections (gear face on gear face) to make sure it's actually on
			 * this gear block.
			 */
			if (from == placementSide.getOpposite())
			{
				if (source instanceof PartGear || source instanceof PartGearShaft)
				{
					if (source instanceof PartGearShaft)
					{
						return true;
					}
					else if (source instanceof PartGear)
					{
						if (((PartGear) source).tile() == tile() && !getMultiBlock().isConstructed())
						{
							return true;
						}

						// For large gear to small gear on edge connection.
						return true;
					}
				}

				TileEntity sourceTile = getPosition().translate(from.getOpposite()).getTileEntity(world());

				if (sourceTile instanceof IMechanical)
				{
					IMechanical sourceInstance = ((IMechanical) sourceTile).getInstance(from);
					return sourceInstance == source;
				}
			}
			else
			{
				TileEntity destinationTile = ((IMechanical) source).getPosition().translate(from.getOpposite()).getTileEntity(world());

				if (destinationTile instanceof IMechanical && destinationTile instanceof TileMultipart)
				{
					TMultiPart destinationPart = ((TileMultipart) destinationTile).partMap(placementSide.ordinal());

					if (destinationPart instanceof PartGear)
					{
						if (this != destinationPart)
						{
							return ((PartGear) destinationPart).isCenterMultiBlock();
						}
						else
						{
							return true;
						}
					}
					else
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Multipart Bounds
	 */
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return Arrays.asList(oBoxes[this.placementSide.ordinal()]);
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
	public boolean inverseRotation(ForgeDirection dir, IMechanical with)
	{
		return true;
	}
}