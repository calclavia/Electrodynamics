package resonantinduction.mechanical.energy.gear;

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
import resonantinduction.core.resource.ItemHandCrank;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.grid.PartMechanical;
import calclavia.api.resonantinduction.IMechanicalNode;
import calclavia.lib.grid.INode;
import calclavia.lib.grid.INodeProvider;
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
 */
public class PartGear extends PartMechanical implements IMultiBlockStructure<PartGear>
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

	private boolean isClockwiseCrank = true;
	private int manualCrankTime = 0;
	private int multiBlockRadius = 1;

	public PartGear()
	{
		super();
		node = new MechanicalNode(this)
		{
			@Override
			public void onUpdate()
			{
				if (!getMultiBlock().isPrimary())
				{
					torque = 0;
					angularVelocity = 0;
				}
			}

			@Override
			public double getTorqueLoad()
			{
				// Decelerate the gear based on tier.
				switch (tier)
				{
					default:
						return 0.03;
					case 1:
						return 0.02;
					case 2:
						return 0.01;
				}
			}

			@Override
			public double getAngularVelocityLoad()
			{
				// Decelerate the gear based on tier.
				switch (tier)
				{
					default:
						return 0.03;
					case 1:
						return 0.02;
					case 2:
						return 0.01;
				}
			}

			@Override
			public void doRecache()
			{
				connections.clear();

				/**
				 * Only call refresh if this is the main block of a multiblock gear or a single
				 * gear
				 * block.
				 */
				if (!getMultiBlock().isPrimary() || world() == null)
				{
					return;
				}

				/** Look for gears that are back-to-back with this gear. Equate torque. */
				TileEntity tileBehind = new universalelectricity.api.vector.Vector3(tile()).translate(placementSide).getTileEntity(world());

				if (tileBehind instanceof INodeProvider)
				{
					MechanicalNode instance = ((INodeProvider) tileBehind).getNode(MechanicalNode.class, placementSide.getOpposite());

					if (instance != null && instance != this && !(instance.parent instanceof PartGearShaft) && instance.canConnect(placementSide.getOpposite(), this))
					{
						connections.put(instance, placementSide);
					}
				}

				/**
				 * Look for gears that are internal and adjacent to this gear. (The 4 sides +
				 * the internal center)
				 */
				for (int i = 0; i < 6; i++)
				{
					ForgeDirection checkDir = ForgeDirection.getOrientation(i);

					TileEntity tile = tile();

					if (getMultiBlock().isConstructed() && checkDir != placementSide && checkDir != placementSide.getOpposite())
					{
						tile = new universalelectricity.api.vector.Vector3(tile()).translate(checkDir).getTileEntity(world());
					}

					if (tile instanceof INodeProvider)
					{
						/**
						 * If we're checking for the block that is opposite to the gear's
						 * placement
						 * side
						 * (the center), then we try to look for a gear shaft in the center.
						 */
						MechanicalNode instance = ((INodeProvider) tile).getNode(MechanicalNode.class, checkDir == placementSide.getOpposite() ? ForgeDirection.UNKNOWN : checkDir);

						if (!connections.containsValue(checkDir) && instance != this && checkDir != placementSide && instance != null && instance.canConnect(checkDir.getOpposite(), this))
						{
							connections.put(instance, checkDir);
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
					ForgeDirection checkDir = ForgeDirection.getOrientation(Rotation.rotateSide(PartGear.this.placementSide.ordinal(), i));
					TileEntity checkTile = new universalelectricity.api.vector.Vector3(tile()).translate(checkDir, displaceCheck).getTileEntity(world());

					if (!connections.containsValue(checkDir) && checkTile instanceof INodeProvider)
					{
						MechanicalNode instance = ((INodeProvider) checkTile).getNode(MechanicalNode.class, placementSide);

						if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite(), this) && !(instance.parent instanceof PartGearShaft))
						{
							connections.put(instance, checkDir);
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
				if (!getMultiBlock().isPrimary())
				{
					return false;
				}

				if (with instanceof MechanicalNode)
				{
					INodeProvider parent = ((MechanicalNode) with).parent;

					/**
					 * Check for flat connections (gear face on gear face) to make sure it's
					 * actually on this gear block.
					 */
					if (from == placementSide.getOpposite())
					{
						if (parent instanceof PartGear || parent instanceof PartGearShaft)
						{
							if (parent instanceof PartGearShaft)
							{
								PartGearShaft shaft = (PartGearShaft) parent;
								return shaft.tile().partMap(from.getOpposite().ordinal()) == PartGear.this && Math.abs(shaft.placementSide.offsetX) == Math.abs(placementSide.offsetX) && Math.abs(shaft.placementSide.offsetY) == Math.abs(placementSide.offsetY) && Math.abs(shaft.placementSide.offsetZ) == Math.abs(placementSide.offsetZ);
							}
							else if (parent instanceof PartGear)
							{
								if (((PartGear) parent).tile() == tile() && !getMultiBlock().isConstructed())
								{
									return true;
								}

								if (((PartGear) parent).placementSide != placementSide)
								{
									TMultiPart part = tile().partMap(((PartGear) parent).placementSide.ordinal());

									if (part instanceof PartGear)
									{
										/**
										 * Case when we connect gears via edges internally. Large
										 * gear
										 * attempt to connect to small gear.
										 */
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
										if (getMultiBlock().isConstructed())
										{
											TMultiPart checkPart = ((PartGear) parent).tile().partMap(placementSide.ordinal());

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
							MechanicalNode sourceInstance = ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from);
							return sourceInstance == with;
						}
					}
					else if (from == placementSide)
					{
						/** Face to face stick connection. */
						TileEntity sourceTile = position().translate(from).getTileEntity(world());

						if (sourceTile instanceof INodeProvider)
						{
							MechanicalNode sourceInstance = ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from.getOpposite());
							return sourceInstance == with;
						}
					}
					else
					{
						TileEntity destinationTile = ((MechanicalNode) with).position().translate(from.getOpposite()).getTileEntity(world());

						if (destinationTile instanceof INodeProvider && destinationTile instanceof TileMultipart)
						{
							TMultiPart destinationPart = ((TileMultipart) destinationTile).partMap(placementSide.ordinal());

							if (destinationPart instanceof PartGear)
							{
								if (PartGear.this != destinationPart)
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

				boolean caseX = placementSide.offsetX != 0 && deltaPos.y == 0 && deltaPos.z == 0;
				boolean caseY = placementSide.offsetY != 0 && deltaPos.x == 0 && deltaPos.z == 0;
				boolean caseZ = placementSide.offsetZ != 0 && deltaPos.x == 0 && deltaPos.y == 0;

				if (caseX || caseY || caseZ)
				{
					return super.getRatio(dir, with);
				}

				return getMultiBlock().isConstructed() ? 3f : super.getRatio(dir, with);
			}
		};
	}

	@Override
	public void update()
	{
		super.update();

		if (!this.world().isRemote)
		{
			if (manualCrankTime > 0)
			{
				node.apply(isClockwiseCrank ? 15 : -15, isClockwiseCrank ? 0.025f : -0.025f);
				manualCrankTime--;
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
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack itemStack)
	{
		if (itemStack != null && itemStack.getItem() instanceof ItemHandCrank)
		{
			if (!world().isRemote && ControlKeyModifer.isControlDown(player))
			{
				getMultiBlock().get().node.torque = -getMultiBlock().get().node.torque;
				getMultiBlock().get().node.angularVelocity = -getMultiBlock().get().node.angularVelocity;
				return true;
			}

			isClockwiseCrank = player.isSneaking();
			getMultiBlock().get().manualCrankTime = 20;
			world().playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, Reference.PREFIX + "gearCrank", 0.5f, 0.9f + world().rand.nextFloat() * 0.2f);
			player.addExhaustion(0.01f);
			return true;
		}

		if (WrenchUtility.isWrench(itemStack))
		{
			getMultiBlock().toggleConstruct();
			return true;
		}

		return super.activate(player, hit, itemStack);
	}

	@Override
	public void preRemove()
	{
		super.preRemove();
		getMultiBlock().deconstruct();
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
			return true;
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

	/** Multiblock */
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
	public <N extends INode> N getNode(Class<? super N> nodeType, ForgeDirection from)
	{
		if (nodeType.isAssignableFrom(node.getClass()))
			return (N) getMultiBlock().get().node;
		return null;
	}

	/** Multipart Bounds */
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
	@SideOnly(Side.CLIENT)
	public Cuboid6 getRenderBounds()
	{
		return Cuboid6.full.copy().expand(multiBlockRadius);
	}
}