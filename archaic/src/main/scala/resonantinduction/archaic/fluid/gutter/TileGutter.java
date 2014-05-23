package resonantinduction.archaic.fluid.gutter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import org.lwjgl.opengl.GL11;

import resonant.api.recipe.MachineRecipes;
import resonant.api.recipe.RecipeResource;
import resonant.lib.content.module.TileRender;
import resonant.lib.prefab.vector.Cuboid;
import resonant.lib.render.FluidRenderUtility;
import resonant.lib.render.RenderUtility;
import resonant.lib.utility.FluidUtility;
import resonant.lib.utility.WorldUtility;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.archaic.fluid.grate.TileGrate;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction.RecipeType;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;
import resonantinduction.core.grid.fluid.TilePressureNode;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The gutter, used for fluid transfer.
 *
 * @author Calclavia
 */
public class TileGutter extends TilePressureNode
{
	public TileGutter()
	{
		super(Material.rock);
		textureName = "material_wood_surface";
		isOpaqueCube = false;
		normalRender = false;
		bounds = new Cuboid(0, 0, 0, 1, 0.99, 1);

		node = new FluidGravityNode(this)
		{
			@Override
			public void doRecache()
			{
				connections.clear();
				byte previousConnections = renderSides;
				renderSides = 0;

				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				{
					TileEntity tile = position().translate(dir).getTileEntity(world());

					if (tile instanceof IFluidHandler)
					{
						if (tile instanceof IPressureNodeProvider)
						{
							FluidPressureNode check = (FluidPressureNode) ((IPressureNodeProvider) tile).getNode(FluidPressureNode.class, dir.getOpposite());

							if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite(), this))
							{
								connections.put(check, dir);

								if (tile instanceof TileGutter)
								{
									renderSides = WorldUtility.setEnableSide(renderSides, dir, true);
								}

							}
						}
						else
						{
							connections.put(tile, dir);

							if (tile instanceof TileGrate)
							{
								renderSides = WorldUtility.setEnableSide(renderSides, dir, true);
							}
						}
					}
				}

				/** Only send packet updates if visuallyConnected changed. */
				if (previousConnections != renderSides)
				{
					sendRenderUpdate();
				}
			}
		};
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.world().isRemote && this.ticks % 20 == 0)
		{
			/** Drain block above if it is a fluid. */
			Vector3 drainPos = position().translate(0, 1, 0);
			FluidStack drain = FluidUtility.drainBlock(worldObj, drainPos, false);

			if (drain != null)
			{
				ArrayList<IFluidTank> tanks = new ArrayList<IFluidTank>();

				synchronized (node.getGrid().getNodes())
				{
					for (Object check : node.getGrid().getNodes())
					{
						if (check instanceof FluidPressureNode)
						{
							if (((FluidPressureNode) check).parent instanceof TileGutter)
							{
								tanks.add(((FluidPressureNode) check).parent.getPressureTank());
							}
						}
					}
				}

				if (FluidUtility.fillAllTanks(tanks, drain, false) >= drain.amount)
				{
					FluidUtility.fillAllTanks(tanks, drain, true);
					FluidUtility.drainBlock(worldObj, drainPos, true);

					synchronized (node.getGrid().getNodes())
					{
						for (Object check : node.getGrid().getNodes())
						{
							if (check instanceof FluidPressureNode)
							{
								((FluidPressureNode) check).parent.onFluidChanged();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Iterable<Cuboid> getCollisionBoxes()
	{
		List<Cuboid> list = new ArrayList<Cuboid>();

		float thickness = 0.1F;

		if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.DOWN))
		{
			list.add(new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, thickness, 1.0F));
		}

		if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.WEST))
		{
			list.add(new Cuboid(0.0F, 0.0F, 0.0F, thickness, 1.0F, 1.0F));
		}
		if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.NORTH))
		{
			list.add(new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, thickness));
		}

		if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.EAST))
		{
			list.add(new Cuboid(1.0F - thickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F));
		}

		if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.SOUTH))
		{
			list.add(new Cuboid(0.0F, 0.0F, 1.0F - thickness, 1.0F, 1.0F, 1.0F));
		}

		return list;
	}

	@Override
	public void collide(Entity entity)
	{
		if (getInternalTank().getFluidAmount() > 0)
		{
			for (int i = 2; i < 6; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				int pressure = node.getPressure(dir);
				Vector3 position = position().translate(dir);

				TileEntity checkTile = position.getTileEntity(world());

				if (checkTile instanceof TileGutter)
				{
					int deltaPressure = pressure - ((TileGutter) checkTile).node.getPressure(dir.getOpposite());

					entity.motionX += 0.01 * dir.offsetX * deltaPressure;
					entity.motionY += 0.01 * dir.offsetY * deltaPressure;
					entity.motionZ += 0.01 * dir.offsetZ * deltaPressure;
				}
			}

			if (getInternalTank().getFluid().getFluid().getTemperature() >= 373)
			{
				entity.setFire(5);
			}
			else
			{
				entity.extinguish();
			}
		}

		if (entity instanceof EntityItem)
		{
			entity.noClip = true;
		}
	}

	@Override
	public boolean activate(EntityPlayer player, int side, Vector3 vector3)
	{
		if (player.getCurrentEquippedItem() != null)
		{
			/** Manually wash dust into refined dust. */
			ItemStack itemStack = player.getCurrentEquippedItem();

			RecipeResource[] outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER.name(), itemStack);

			if (outputs.length > 0)
			{
				if (!world().isRemote)
				{
					int drainAmount = 50 + world().rand.nextInt(50);
					FluidStack drain = drain(ForgeDirection.UP, drainAmount, false);

					if (drain != null && drain.amount > 0 && world().rand.nextFloat() > 0.9)
					{
						if (world().rand.nextFloat() > 0.1)
						{
							for (RecipeResource res : outputs)
							{
								InventoryUtility.dropItemStack(world(), new Vector3(player), res.getItemStack().copy(), 0);
							}
						}

						itemStack.stackSize--;

						if (itemStack.stackSize <= 0)
						{
							itemStack = null;
						}

						player.inventory.setInventorySlotContents(player.inventory.currentItem, itemStack);
					}

					drain(ForgeDirection.UP, drainAmount, true);

					world().playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, "liquid.water", 0.5f, 1);
				}

				return true;
			}
		}

		if (!world().isRemote)
		{
			ArrayList<IFluidTank> tanks = new ArrayList<IFluidTank>();

			synchronized (node.getGrid().getNodes())
			{
				for (Object check : node.getGrid().getNodes())
				{
					if (check instanceof FluidPressureNode)
					{
						if (((FluidPressureNode) check).parent instanceof TileGutter)
						{
							tanks.add(((FluidPressureNode) check).parent.getPressureTank());
						}
					}
				}
			}

			if (FluidUtility.playerActivatedFluidItem(tanks, player, side))
			{
				synchronized (node.getGrid().getNodes())
				{
					for (Object check : node.getGrid().getNodes())
					{
						if (check instanceof FluidPressureNode)
						{
							((FluidPressureNode) check).parent.onFluidChanged();
						}
					}
				}

				return true;
			}

			return false;

		}

		return true;
	}

	@Override
	public void onFillRain()
	{
		if (!world().isRemote)
		{
			fill(ForgeDirection.UP, new FluidStack(FluidRegistry.WATER, 10), true);
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (!resource.getFluid().isGaseous())
		{
			return super.fill(from, resource, doFill);
		}

		return 0;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return from != ForgeDirection.UP && !fluid.isGaseous();
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return from != ForgeDirection.UP && !fluid.isGaseous();
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected TileRender newRenderer()
	{
		return new TileRender()
		{
			public final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "gutter.tcn");
			public final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "gutter.png");

			@Override
			public boolean renderStatic(RenderBlocks renderer, Vector3 position)
			{
				return true;
			}

			@Override
			public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
			{
				GL11.glPushMatrix();
				GL11.glTranslated(position.x + 0.5, position.y + 0.5, position.z + 0.5);

				FluidStack liquid = getInternalTank().getFluid();
				int capacity = getInternalTank().getCapacity();

				render(0, renderSides);

				if (world() != null)
				{
					FluidTank tank = getInternalTank();
					double percentageFilled = (double) tank.getFluidAmount() / (double) tank.getCapacity();

					if (percentageFilled > 0.1)
					{
						GL11.glPushMatrix();
						GL11.glScaled(0.990, 0.99, 0.990);

						double ySouthEast = FluidUtility.getAveragePercentageFilledForSides(TileGutter.class, percentageFilled, world(), position(), ForgeDirection.SOUTH, ForgeDirection.EAST);
						double yNorthEast = FluidUtility.getAveragePercentageFilledForSides(TileGutter.class, percentageFilled, world(), position(), ForgeDirection.NORTH, ForgeDirection.EAST);
						double ySouthWest = FluidUtility.getAveragePercentageFilledForSides(TileGutter.class, percentageFilled, world(), position(), ForgeDirection.SOUTH, ForgeDirection.WEST);
						double yNorthWest = FluidUtility.getAveragePercentageFilledForSides(TileGutter.class, percentageFilled, world(), position(), ForgeDirection.NORTH, ForgeDirection.WEST);

						FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest);
						GL11.glPopMatrix();
					}
				}

				GL11.glPopMatrix();
				return true;
			}

			@Override
			public boolean renderItem(ItemStack itemStack)
			{
				GL11.glTranslated(0.5, 0.5, 0.5);
				render(itemStack.getItemDamage(), Byte.parseByte("001100", 2));
				return true;
			}

			public void render(int meta, byte sides)
			{
				RenderUtility.bind(TEXTURE);

				double thickness = 0.055;
				double height = 0.5;

				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				{
					if (dir != ForgeDirection.UP && dir != ForgeDirection.DOWN)
					{
						GL11.glPushMatrix();
						RenderUtility.rotateBlockBasedOnDirection(dir);

						if (WorldUtility.isEnabledSide(sides, ForgeDirection.DOWN))
						{
							GL11.glTranslatef(0, -0.075f, 0);
							GL11.glScalef(1, 1.15f, 1);
						}

						if (!WorldUtility.isEnabledSide(sides, dir))
						{
							/** Render sides */
							MODEL.renderOnly("left");
						}

						if (!WorldUtility.isEnabledSide(sides, dir) || !WorldUtility.isEnabledSide(sides, dir.getRotation(ForgeDirection.UP)))
						{
							/** Render strips */
							MODEL.renderOnly("backCornerL");
						}
						GL11.glPopMatrix();
					}
				}

				if (!WorldUtility.isEnabledSide(sides, ForgeDirection.DOWN))
				{
					MODEL.renderOnly("base");
				}

			}
		};
	}

}
