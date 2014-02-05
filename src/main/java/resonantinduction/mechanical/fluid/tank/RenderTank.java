package resonantinduction.mechanical.fluid.tank;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.archaic.Archaic;
import resonantinduction.core.render.RenderFluidHelper;
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

			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
			RenderUtility.renderBlockWithConnectedTextures(renderSides, Mechanical.blockTank, null, Archaic.blockMachinePart, null);
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