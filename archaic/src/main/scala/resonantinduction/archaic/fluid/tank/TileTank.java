package resonantinduction.archaic.fluid.tank;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import resonant.api.IRemovable.ISneakPickup;
import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.content.module.TileBlock.IComparatorInputOverride;
import resonant.lib.content.module.TileRender;
import resonant.lib.render.FluidRenderUtility;
import resonant.lib.render.RenderUtility;
import resonant.lib.utility.FluidUtility;
import resonant.lib.utility.WorldUtility;
import resonant.lib.utility.render.RenderBlockUtility;
import resonantinduction.archaic.Archaic;
import resonantinduction.core.Reference;
import resonantinduction.core.grid.fluid.FluidDistributionetwork;
import resonantinduction.core.grid.fluid.IFluidDistribution;
import resonantinduction.core.grid.fluid.TileFluidDistribution;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Tile/Block class for basic Dynamic tanks
 *
 * @author Darkguardsman
 */
public class TileTank extends TileFluidDistribution implements IComparatorInputOverride, ISneakPickup
{
	public static final int VOLUME = 16;

	public TileTank()
	{
		super(UniversalElectricity.machine, VOLUME * FluidContainerRegistry.BUCKET_VOLUME);
		isOpaqueCube = false;
		normalRender = false;
		itemBlock = ItemBlockTank.class;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side)
	{
		return access != null && block != null && access.getBlockId(x, y, z) != block.blockID;
	}

	@Override
	protected boolean use(EntityPlayer player, int side, Vector3 vector3)
	{
		if (!world().isRemote)
		{
			return FluidUtility.playerActivatedFluidItem(world(), x(), y(), z(), player, side);
		}

		return true;
	}

	@Override
	public int getComparatorInputOverride(int side)
	{
		if (getNetwork().getTank().getFluid() != null)
		{
			return (int) (15 * ((double) getNetwork().getTank().getFluidAmount() / (double) getNetwork().getTank().getCapacity()));
		}
		return 0;
	}

	@Override
	public int getLightValue(IBlockAccess access)
	{
		if (getInternalTank().getFluid() != null)
		{
			return getInternalTank().getFluid().getFluid().getLuminosity();
		}
		return super.getLightValue(access);
	}

	@Override
	public FluidDistributionetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new TankNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public void setNetwork(FluidDistributionetwork network)
	{
		if (network instanceof TankNetwork)
		{
			this.network = network;
		}
	}

	@Override
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof TileTank)
			{
				getNetwork().merge(((IFluidDistribution) tileEntity).getNetwork());
				renderSides = WorldUtility.setEnableSide(renderSides, side, true);
				connectedBlocks[side.ordinal()] = tileEntity;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected TileRender newRenderer()
	{
		return new TileRender()
		{
			@Override
			public boolean renderStatic(RenderBlocks renderer, Vector3 position)
			{
				RenderBlockUtility.tessellateBlockWithConnectedTextures(renderSides, world(), x(), y(), z(), Archaic.blockTank, null, RenderUtility.getIcon(Reference.PREFIX + "tankEdge"));
				return true;
			}

			public void renderTank(TileEntity tileEntity, double x, double y, double z, FluidStack fluid)
			{
				if (tileEntity.worldObj != null && tileEntity instanceof TileTank)
				{
					GL11.glPushMatrix();
					GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

					if (fluid != null)
					{
						GL11.glPushMatrix();

						if (!fluid.getFluid().isGaseous())
						{
							GL11.glScaled(0.99, 0.99, 0.99);
							FluidTank tank = ((TileTank) tileEntity).getInternalTank();
							double percentageFilled = (double) tank.getFluidAmount() / (double) tank.getCapacity();

							double ySouthEast = FluidUtility.getAveragePercentageFilledForSides(TileTank.class, percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.SOUTH, ForgeDirection.EAST);
							double yNorthEast = FluidUtility.getAveragePercentageFilledForSides(TileTank.class, percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.NORTH, ForgeDirection.EAST);
							double ySouthWest = FluidUtility.getAveragePercentageFilledForSides(TileTank.class, percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.SOUTH, ForgeDirection.WEST);
							double yNorthWest = FluidUtility.getAveragePercentageFilledForSides(TileTank.class, percentageFilled, tileEntity.worldObj, new Vector3(tileEntity), ForgeDirection.NORTH, ForgeDirection.WEST);
							FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest);
						}
						else
						{
							GL11.glTranslated(-0.5, -0.5, -0.5);
							GL11.glScaled(0.99, 0.99, 0.99);
							int capacity = tileEntity instanceof TileTank ? ((TileTank) tileEntity).getInternalTank().getCapacity() : fluid.amount;
							double filledPercentage = (double) fluid.amount / (double) capacity;
							double renderPercentage = fluid.getFluid().isGaseous() ? 1 : filledPercentage;

							int[] displayList = FluidRenderUtility.getFluidDisplayLists(fluid, tileEntity.worldObj, false);

							GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
							GL11.glEnable(GL11.GL_CULL_FACE);
							GL11.glDisable(GL11.GL_LIGHTING);
							GL11.glEnable(GL11.GL_BLEND);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

							Color color = new Color(fluid.getFluid().getColor());
							RenderUtility.enableBlending();
							GL11.glColor4d(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, fluid.getFluid().isGaseous() ? filledPercentage : 1);

							RenderUtility.bind(FluidRenderUtility.getFluidSheet(fluid));
							GL11.glCallList(displayList[(int) (renderPercentage * (FluidRenderUtility.DISPLAY_STAGES - 1))]);
							RenderUtility.disableBlending();
							GL11.glPopAttrib();
						}

						GL11.glPopMatrix();
					}

					GL11.glPopMatrix();
				}
			}

			@Override
			public boolean renderDynamic(Vector3 position, boolean isItem, float frame)
			{
				renderTank(TileTank.this, position.x, position.y, position.z, getInternalTank().getFluid());
				return false;
			}
		};
	}

	public static class ItemRenderer implements ISimpleItemRenderer
	{
		public static ItemRenderer instance = new ItemRenderer();

		public void renderTank(double x, double y, double z, FluidStack fluid, int capacity)
		{
			FluidTank tank = new FluidTank(fluid, capacity);
			GL11.glPushMatrix();

			GL11.glTranslated(0.02, 0.02, 0.02);
			GL11.glScaled(0.92, 0.92, 0.92);

			if (fluid != null)
			{
				GL11.glPushMatrix();

				if (!fluid.getFluid().isGaseous())
				{
					double percentageFilled = (double) tank.getFluidAmount() / (double) tank.getCapacity();
					FluidRenderUtility.renderFluidTesselation(tank, percentageFilled, percentageFilled, percentageFilled, percentageFilled);
				}
				else
				{
					double filledPercentage = (double) fluid.amount / (double) capacity;

					GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					Color color = new Color(fluid.getFluid().getColor());
					RenderUtility.enableBlending();
					GL11.glColor4d(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, fluid.getFluid().isGaseous() ? filledPercentage : 1);

					RenderUtility.bind(FluidRenderUtility.getFluidSheet(fluid));
					FluidRenderUtility.renderFluidTesselation(tank, 1, 1, 1, 1);
					RenderUtility.disableBlending();
					GL11.glPopAttrib();
				}

				GL11.glPopMatrix();
			}

			GL11.glPopMatrix();
		}

		@Override
		public void renderInventoryItem(ItemStack itemStack)
		{
			GL11.glPushMatrix();
			RenderBlockUtility.tessellateBlockWithConnectedTextures(itemStack.getItemDamage(), Archaic.blockTank, null, RenderUtility.getIcon(Reference.PREFIX + "tankEdge"));
			GL11.glPopMatrix();

			GL11.glPushMatrix();

			if (itemStack.getTagCompound() != null && itemStack.getTagCompound().hasKey("fluid"))
			{
				renderTank(0, 0, 0, FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound().getCompoundTag("fluid")), VOLUME * FluidContainerRegistry.BUCKET_VOLUME);
			}

			GL11.glPopMatrix();
		}

	}

	@Override
	public List<ItemStack> getRemovedItems(EntityPlayer entity)
	{
		List<ItemStack> drops = new ArrayList();

		ItemStack itemStack = new ItemStack(Archaic.blockTank, 1, 0);
		if (itemStack != null)
		{
			if (getInternalTank() != null && getInternalTank().getFluid() != null)
			{
				FluidStack stack = getInternalTank().getFluid();

				if (stack != null)
				{
					if (itemStack.getTagCompound() == null)
					{
						itemStack.setTagCompound(new NBTTagCompound());
					}
					drain(ForgeDirection.UNKNOWN, stack.amount, false);
					itemStack.getTagCompound().setCompoundTag("fluid", stack.writeToNBT(new NBTTagCompound()));
				}
			}
			drops.add(itemStack);
		}
		return drops;
	}
}
