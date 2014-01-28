package resonantinduction.mechanical.gear;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.PartMechanical;
import calclavia.lib.multiblock.reference.IMultiBlockStructure;
import calclavia.lib.multiblock.reference.MultiBlockHandler;
import calclavia.lib.prefab.block.BlockAdvanced;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
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
					torque += 10;
					angularVelocity += 0.1f;
				}
				else
				{
					torque -= 10;
					angularVelocity -= 0.1f;
				}

				manualCrankTime--;
			}

			if (getMultiBlock().isPrimary())
			{
				// Decelerate the gear.
				torque *= 0.95f;
				angularVelocity *= 0.95f;
			}
			else
			{
				torque = 0;
				angularVelocity = 0;
			}
		}

		getMultiBlock().update();
	}

	public void checkClientUpdate()
	{
		if (getMultiBlock().isPrimary())
			super.checkClientUpdate();
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		if (!world().isRemote)
		{
			// System.out.println(this + ":" + getNetwork());
			// for(Object obj : connections)
			// System.out.println(obj);
		}

		if (BlockAdvanced.isUsableWrench(player, player.getCurrentEquippedItem(), x(), y(), z()))
		{
			if (player.isSneaking())
			{
				if (!world().isRemote)
				{
					getMultiBlock().get().angularVelocity = -angularVelocity;
					player.addChatMessage("Flipped gear to rotate " + (angularVelocity > 0 ? "clockwise" : "anticlockwise") + ".");
				}
			}
			else if (ControlKeyModifer.isControlDown(player))
			{
				getMultiBlock().get().manualCrankTime = 10;
			}
			else
			{
				getMultiBlock().toggleConstruct();
			}

			BlockAdvanced.damageWrench(player, player.getCurrentEquippedItem(), x(), y(), z());
			return true;
		}

		return false;
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

			if (instance != null && instance.canConnect(placementSide, this))
			{
				connections[placementSide.getOpposite().ordinal()] = instance;
				getNetwork().merge(instance.getNetwork());
			}

		}

		// TODO: Make bending with large gears work.
		if (!getMultiBlock().isConstructed())
		{
			/** Look for gears that are internal and adjacent to this gear. (The 4 sides) */
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection checkDir = ForgeDirection.getOrientation(i);
				IMechanical instance = ((IMechanical) tile()).getInstance(checkDir);

				if (connections[checkDir.ordinal()] == null && checkDir != placementSide && checkDir != placementSide.getOpposite() && instance != null && instance.canConnect(checkDir.getOpposite(), this))
				{
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
				IMechanical instance = (IMechanical) ((IMechanical) checkTile).getInstance(placementSide);

				if (instance != null && instance.canConnect(checkDir.getOpposite(), this))
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
		return new ItemStack(Mechanical.itemGear);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderGear.INSTANCE.renderDynamic(this, pos.x, pos.y, pos.z, frame);
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
		getMultiBlock().save(this, nbt);
	}

	/**
	 * Multiblock
	 */
	private MultiPartMultiBlockHandler multiBlock;

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
		tile().notifyPartChange(this);

		if (!world().isRemote)
		{
			sendDescUpdate();
		}
	}

	@Override
	public MultiBlockHandler<PartGear> getMultiBlock()
	{
		if (multiBlock == null)
			multiBlock = new MultiPartMultiBlockHandler(this);

		return multiBlock;
	}

	@Override
	public universalelectricity.api.vector.Vector3 getPosition()
	{
		return new universalelectricity.api.vector.Vector3(x(), y(), z());
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
				if (source instanceof PartGear)
				{
					if (((PartGear) source).tile() == tile() && !getMultiBlock().isConstructed())
					{
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
}