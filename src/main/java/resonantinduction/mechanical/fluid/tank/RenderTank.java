package resonantinduction.mechanical.fluid.tank;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.render.RenderFluidHelper;
import resonantinduction.electrical.Electrical;
import resonantinduction.mechanical.Mechanical;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTank extends TileEntitySpecialRenderer
{
	public static final RenderTank INSTANCE = new RenderTank();
	public final ModelTank model = new ModelTank();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		this.renderTank(tileEntity, x, y, z, tileEntity instanceof TileTank ? ((TileTank) tileEntity).getInternalTank().getFluid() : null);
	}

	public void renderTank(TileEntity tileEntity, double x, double y, double z, FluidStack fluid)
	{
		if (tileEntity instanceof TileTank)
		{
			byte renderSides = ((TileTank) tileEntity).renderSides;

			boolean down = TileTank.canRenderSide(renderSides, ForgeDirection.DOWN);
			boolean up = TileTank.canRenderSide(renderSides, ForgeDirection.UP);
			boolean north = TileTank.canRenderSide(renderSides, ForgeDirection.NORTH);
			boolean south = TileTank.canRenderSide(renderSides, ForgeDirection.SOUTH);
			boolean east = TileTank.canRenderSide(renderSides, ForgeDirection.EAST);
			boolean west = TileTank.canRenderSide(renderSides, ForgeDirection.WEST);

			bindTexture(TextureMap.locationBlocksTexture);
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

			/**
			 * Render faces
			 */
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);

				if (!TileTank.canRenderSide(renderSides, dir))
				{
					GL11.glPushMatrix();
					GL11.glScalef(0.99f, 0.99f, 0.99f);

					switch (i)
					{
						case 0:
							GL11.glRotatef(0, 0, 0, 1);
							break;
						case 1:
							GL11.glRotatef(180, 0, 0, 1);
							break;
						case 2:
							GL11.glRotatef(90, 1, 0, 0);
							break;
						case 3:
							GL11.glRotatef(-90, 1, 0, 0);
							break;
						case 4:
							GL11.glRotatef(-90, 0, 0, 1);
							break;
						case 5:
							GL11.glRotatef(90, 0, 0, 1);
							break;
					}

					RenderUtility.renderCube(-0.501, -0.501, -0.501, 0.501, -0.475, 0.501, Mechanical.blockTank);
					GL11.glPopMatrix();
				}
			}

			/**
			 * Render edges
			 */
			GL11.glPushMatrix();

			if (!east)
			{
				if (!north)
				{
					// north east
					RenderUtility.renderCube(0.475, -0.501, -0.501, 0.501, 0.501, -0.475, Electrical.blockMachinePart);
				}
				if (!south)
				{
					// south east
					RenderUtility.renderCube(0.475, -0.501, 0.475, 0.501, 0.501, 0.501, Electrical.blockMachinePart);
				}

				if (!down)
				{
					// bottom east
					RenderUtility.renderCube(0.475, -0.501, -0.501, 0.501, -0.475, 0.501, Electrical.blockMachinePart);
				}

				if (!up)
				{
					// top east
					RenderUtility.renderCube(0.475, 0.475, -0.501, 0.501, 0.501, 0.501, Electrical.blockMachinePart);
				}
			}

			if (!west)
			{
				if (!north)
				{
					// north west
					RenderUtility.renderCube(-0.501, -0.501, -0.501, -0.475, 0.501, -0.475, Electrical.blockMachinePart);
				}
				if (!south)
				{
					// south west
					RenderUtility.renderCube(-0.501, -0.501, 0.475, -0.475, 0.501, 0.501, Electrical.blockMachinePart);
				}
				if (!down)
				{
					// bottom west
					RenderUtility.renderCube(-0.501, -0.501, -0.501, -0.475, -0.475, 0.501, Electrical.blockMachinePart);
				}
				if (!up)
				{
					// top west
					RenderUtility.renderCube(-0.501, 0.475, -0.501, -0.475, 0.501, 0.501, Electrical.blockMachinePart);
				}
			}
			if (!north)
			{
				if (!up)
				{
					// top north
					RenderUtility.renderCube(-0.501, 0.475, -0.501, 0.501, 0.501, -0.475, Electrical.blockMachinePart);
				}
				if (!down)
				{
					// bottom north
					RenderUtility.renderCube(-0.501, -0.501, -0.501, 0.501, -0.475, -0.475, Electrical.blockMachinePart);
				}
			}

			if (!south)
			{
				if (!up)
				{
					// top south
					RenderUtility.renderCube(-0.501, 0.475, 0.475, 0.501, 0.501, 0.501, Electrical.blockMachinePart);
				}
				if (!down)
				{
					// bottom south
					RenderUtility.renderCube(-0.501, -0.501, 0.475, 0.501, -0.475, 0.501, Electrical.blockMachinePart);
				}
			}

			GL11.glPopMatrix();
			GL11.glPopMatrix();

			if (fluid != null && fluid.amount > 100)
			{
				int[] displayList = RenderFluidHelper.getFluidDisplayLists(fluid, tileEntity.worldObj, false);

				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				bindTexture(RenderFluidHelper.getFluidSheet(fluid));
				// Prevent Z-fighting
				GL11.glTranslatef((float) x, (float) y + 0.001f, (float) z);
				int cap = tileEntity instanceof TileTank ? ((TileTank) tileEntity).getInternalTank().getCapacity() : fluid.amount;
				GL11.glCallList(displayList[(int) ((float) fluid.amount / (float) (cap) * (RenderFluidHelper.DISPLAY_STAGES - 1))]);
				GL11.glPopAttrib();
				GL11.glPopMatrix();
			}
		}
	}
}