package resonantinduction.archaic.channel;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderFluidHelper;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChannel extends TileEntitySpecialRenderer
{
	public static final RenderChannel INSTANCE = new RenderChannel();

	public static ModelChannel MODEL_TROUGH_PIPE = new ModelChannel();
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "white.png");

	public static void render(int meta, byte sides)
	{
		RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
		MODEL_TROUGH_PIPE.render(sides, meta == 0 ? true : false);
	}

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		FluidStack liquid = ((TileChannel) tile).getInternalTank().getFluid();
		int capacity = ((TileChannel) tile).getInternalTank().getCapacity();
		byte renderSides = (tile instanceof TileChannel ? ((TileChannel) tile).renderSides : (byte) 0);

		if (liquid != null && liquid.amount > 0)
		{
			float percentage = (float) liquid.amount / (float) capacity;
			int[] displayList = RenderFluidHelper.getFluidDisplayLists(liquid, tile.worldObj, false);
			bindTexture(RenderFluidHelper.getFluidSheet(liquid));

			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslatef((float) x + 0.3F, (float) y + 0.1F, (float) z + 0.3F);
			GL11.glScalef(0.4F, 0.4F, 0.4F);

			GL11.glCallList(displayList[(int) (percentage * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

			GL11.glPopAttrib();
			GL11.glPopMatrix();

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				if (RenderUtility.canRenderSide(renderSides, direction) && direction != ForgeDirection.UP && direction != ForgeDirection.DOWN)
				{
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					switch (direction.ordinal())
					{
						case 4:
							GL11.glTranslatef((float) x + 0F, (float) y + 0.1F, (float) z + 0.3F);
							break;
						case 5:
							GL11.glTranslatef((float) x + 0.7F, (float) y + 0.1F, (float) z + 0.3F);
							break;
						case 2:
							GL11.glTranslatef((float) x + 0.3F, (float) y + 0.1F, (float) z + 0F);
							break;
						case 3:
							GL11.glTranslatef((float) x + 0.3F, (float) y + 0.1F, (float) z + 0.7F);
							break;
					}
					GL11.glScalef(0.3F, 0.4F, 0.4F);

					GL11.glCallList(displayList[(int) (percentage * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

					GL11.glPopAttrib();
					GL11.glPopMatrix();
				}
			}
		}

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		render(0, renderSides);
		GL11.glPopMatrix();
	}
}