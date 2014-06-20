package resonantinduction.mechanical.gear;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INodeProvider;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.gearshaft.PartGearShaft;
import codechicken.lib.vec.Rotation;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

/**
 * Node for the gear
 *
 * @author Calclavia, Edited by: Darkguardsman
 */
public class GearNode extends MechanicalNode
{
	public GearNode(PartGear parent)
	{
		super(parent);
	}

	protected PartGear gear()
	{
		return (PartGear) this.getParent();
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (!gear().getMultiBlock().isPrimary())
		{
			torque = 0;
			angularVelocity = 0;
		}
		else
		{
			if (gear().tier == 10)
			{
				torque = 100;
				angularVelocity = 100;
			}
		}
	}

	@Override
	public double getTorqueLoad()
	{
		// Decelerate the gear based on tier.
		switch (gear().tier)
		{
			default:
				return 0.3;
			case 1:
				return 0.2;
			case 2:
				return 0.1;
			case 10:
				return 0;
		}
	}

	@Override
	public double getAngularVelocityLoad()
	{
		// Decelerate the gear based on tier.
		switch (gear().tier)
		{
			default:
				return 0.03;
			case 1:
				return 0.02;
			case 2:
				return 0.01;
			case 10:
				return 0;
		}
	}

	@Override
	public void recache()
	{
		synchronized (this)
		{
			getConnections().clear();

			/** Only call refresh if this is the main block of a multiblock gear or a single gear block. */
			if (!gear().getMultiBlock().isPrimary() || world() == null)
			{
				return;
			}

			/** Look for gears that are back-to-back with this gear. Equate torque. */
			TileEntity tileBehind = new universalelectricity.api.vector.Vector3(gear().tile()).translate(gear().placementSide).getTileEntity(world());

			if (tileBehind instanceof INodeProvider)
			{
				MechanicalNode instance = (MechanicalNode) ((INodeProvider) tileBehind).getNode(MechanicalNode.class, gear().placementSide.getOpposite());

				if (instance != null && instance != this && !(instance.getParent() instanceof PartGearShaft) && instance.canConnect(gear().placementSide.getOpposite(), this))
				{
					getConnections().put(instance, gear().placementSide);
				}
			}

			/** Look for gears that are internal and adjacent to this gear. (The 4 sides + the internal
			 * center) */
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection checkDir = ForgeDirection.getOrientation(i);

				TileEntity tile = gear().tile();

				if (gear().getMultiBlock().isConstructed() && checkDir != gear().placementSide && checkDir != gear().placementSide.getOpposite())
				{
					tile = new universalelectricity.api.vector.Vector3(gear().tile()).translate(checkDir).getTileEntity(world());
				}

				if (tile instanceof INodeProvider)
				{
					/** If we're checking for the block that is opposite to the gear's placement side
					 * (the center), then we try to look for a gear shaft in the center. */
					MechanicalNode instance = (MechanicalNode) ((INodeProvider) tile).getNode(MechanicalNode.class, checkDir == gear().placementSide.getOpposite() ? ForgeDirection.UNKNOWN : checkDir);

					if (!getConnections().containsValue(checkDir) && instance != this && checkDir != gear().placementSide && instance != null && instance.canConnect(checkDir.getOpposite(), this))
					{
						getConnections().put(instance, checkDir);
					}
				}
			}

			int displaceCheck = 1;

			if (gear().getMultiBlock().isPrimary() && gear().getMultiBlock().isConstructed())
			{
				displaceCheck = 2;
			}

			/** Look for gears outside this block space, the relative UP, DOWN, LEFT, RIGHT */
			for (int i = 0; i < 4; i++)
			{
				ForgeDirection checkDir = ForgeDirection.getOrientation(Rotation.rotateSide(gear().placementSide.ordinal(), i));
				TileEntity checkTile = new universalelectricity.api.vector.Vector3(gear().tile()).translate(checkDir, displaceCheck).getTileEntity(world());

				if (!getConnections().containsValue(checkDir) && checkTile instanceof INodeProvider)
				{
					MechanicalNode instance = (MechanicalNode) ((INodeProvider) checkTile).getNode(MechanicalNode.class, gear().placementSide);

					if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite(), this) && !(instance.getParent() instanceof PartGearShaft))
					{
						getConnections().put(instance, checkDir);
					}
				}
			}
		}
	}

	/**
	 * Can this gear be connected BY the source?
	 *
	 * @param from - Direction source is coming from.
	 * @param with - The source of the connection.
	 * @return True is so.
	 */
	@Override
	public boolean canConnect(ForgeDirection from, Object with)
	{
		if (!gear().getMultiBlock().isPrimary())
		{
			return false;
		}

		if (with instanceof MechanicalNode)
		{
			INodeProvider parent = ((MechanicalNode) with).getParent();

			/** Check for flat connections (gear face on gear face) to make sure it's actually on
			 * this gear block. */
			if (from == gear().placementSide.getOpposite())
			{
				if (parent instanceof PartGear || parent instanceof PartGearShaft)
				{
					if (parent instanceof PartGearShaft)
					{
						PartGearShaft shaft = (PartGearShaft) parent;
						return shaft.tile().partMap(from.getOpposite().ordinal()) == gear() && Math.abs(shaft.placementSide.offsetX) == Math.abs(gear().placementSide.offsetX) && Math.abs(shaft.placementSide.offsetY) == Math.abs(gear().placementSide.offsetY) && Math.abs(shaft.placementSide.offsetZ) == Math.abs(gear().placementSide.offsetZ);
					}
					else if (parent instanceof PartGear)
					{
						if (((PartGear) parent).tile() == gear().tile() && !gear().getMultiBlock().isConstructed())
						{
							return true;
						}

						if (((PartGear) parent).placementSide != gear().placementSide)
						{
							TMultiPart part = gear().tile().partMap(((PartGear) parent).placementSide.ordinal());

							if (part instanceof PartGear)
							{
								/** Case when we connect gears via edges internally. Large gear
								 * attempt to connect to small gear. */
								PartGear sourceGear = (PartGear) part;

								if (sourceGear.isCenterMultiBlock() && !sourceGear.getMultiBlock().isPrimary())
								{
									// For large gear to small gear on edge connection.
									return true;
								}
							}
							else
							{
								/** Small gear attempting to connect to large gear. */
								if (gear().getMultiBlock().isConstructed())
								{
									TMultiPart checkPart = ((PartGear) parent).tile().partMap(gear().placementSide.ordinal());

									if (checkPart instanceof PartGear)
									{
										ForgeDirection requiredDirection = ((PartGear) checkPart).getPosition().subtract(position()).toForgeDirection();
										return ((PartGear) checkPart).isCenterMultiBlock() && ((PartGear) parent).placementSide == requiredDirection;
									}
								}
							}
						}
					}
				}

				/** Face to face stick connection. */
				TileEntity sourceTile = position().translate(from.getOpposite()).getTileEntity(world());

				if (sourceTile instanceof INodeProvider)
				{
					MechanicalNode sourceInstance = (MechanicalNode) ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from);
					return sourceInstance == with;
				}
			}
			else if (from == gear().placementSide)
			{
				/** Face to face stick connection. */
				TileEntity sourceTile = position().translate(from).getTileEntity(world());

				if (sourceTile instanceof INodeProvider)
				{
					MechanicalNode sourceInstance = (MechanicalNode) ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from.getOpposite());
					return sourceInstance == with;
				}
			}
			else
			{
				TileEntity destinationTile = ((MechanicalNode) with).position().translate(from.getOpposite()).getTileEntity(world());

				if (destinationTile instanceof INodeProvider && destinationTile instanceof TileMultipart)
				{
					TMultiPart destinationPart = ((TileMultipart) destinationTile).partMap(gear().placementSide.ordinal());

					if (destinationPart instanceof PartGear)
					{
						if (gear() != destinationPart)
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

	@Override
	public float getRatio(ForgeDirection dir, IMechanicalNode with)
	{
		universalelectricity.api.vector.Vector3 deltaPos = with.position().subtract(position());

		boolean caseX = gear().placementSide.offsetX != 0 && deltaPos.y == 0 && deltaPos.z == 0;
		boolean caseY = gear().placementSide.offsetY != 0 && deltaPos.x == 0 && deltaPos.z == 0;
		boolean caseZ = gear().placementSide.offsetZ != 0 && deltaPos.x == 0 && deltaPos.y == 0;

		if (caseX || caseY || caseZ)
		{
			return super.getRatio(dir, with);
		}

		return gear().getMultiBlock().isConstructed() ? 1.5f : super.getRatio(dir, with);
	}

}
