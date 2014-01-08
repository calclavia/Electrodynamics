package resonantinduction.client.render;

import java.util.HashMap;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.client.model.ModelLargePipe;
import resonantinduction.client.model.ModelOpenTrough;
import resonantinduction.fluid.pipes.EnumPipeType;
import resonantinduction.fluid.pipes.FluidPartsMaterial;
import resonantinduction.fluid.pipes.TileEntityPipe;
import resonantinduction.transport.ResonantInductionTransport;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPipe extends TileEntitySpecialRenderer
{
	public static ModelLargePipe MODEL_PIPE = new ModelLargePipe();
	public static ModelOpenTrough MODEL_TROUGH_PIPE = new ModelOpenTrough();
	private static HashMap<Pair<FluidPartsMaterial, Integer>, ResourceLocation> TEXTURES = new HashMap<Pair<FluidPartsMaterial, Integer>, ResourceLocation>();
	public static ResourceLocation TEXTURE = new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "pipes/Pipe.png");

	@Override
	public void renderTileEntityAt(TileEntity te, double d, double d1, double d2, float f)
	{
		// Texture file

		FluidPartsMaterial mat = FluidPartsMaterial.IRON;
		if (te.getBlockMetadata() < FluidPartsMaterial.values().length)
		{
			mat = FluidPartsMaterial.values()[te.getBlockMetadata()];
		}
		if (te instanceof TileEntityPipe)
		{
			boolean[] sides = ((TileEntityPipe) te).renderConnection;
			if (mat == FluidPartsMaterial.WOOD || mat == FluidPartsMaterial.STONE)
			{
				FluidStack liquid = ((TileEntityPipe) te).getTank().getFluid();
				int cap = ((TileEntityPipe) te).getTankInfo()[0].capacity;
				// FluidStack liquid = new FluidStack(FluidRegistry.WATER, cap);
				if (liquid != null && liquid.amount > 100)
				{
					float per = Math.max(1, (float) liquid.amount / (float) (cap));
					int[] displayList = RenderFluidHelper.getFluidDisplayLists(liquid, te.worldObj, false);
					bindTexture(RenderFluidHelper.getFluidSheet(liquid));

					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0.3F);
					GL11.glScalef(0.4F, 0.4F, 0.4F);

					GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

					GL11.glPopAttrib();
					GL11.glPopMatrix();
					if (sides[4])
					{
						GL11.glPushMatrix();
						GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
						GL11.glEnable(GL11.GL_CULL_FACE);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

						GL11.glTranslatef((float) d + 0F, (float) d1 + 0.1F, (float) d2 + 0.3F);
						GL11.glScalef(0.3F, 0.4F, 0.4F);

						GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

						GL11.glPopAttrib();
						GL11.glPopMatrix();
					}
					if (sides[5])
					{
						GL11.glPushMatrix();
						GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
						GL11.glEnable(GL11.GL_CULL_FACE);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

						GL11.glTranslatef((float) d + 0.7F, (float) d1 + 0.1F, (float) d2 + 0.3F);
						GL11.glScalef(0.3F, 0.4F, 0.4F);

						GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

						GL11.glPopAttrib();
						GL11.glPopMatrix();
					}

					if (sides[2])
					{
						GL11.glPushMatrix();
						GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
						GL11.glEnable(GL11.GL_CULL_FACE);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

						GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0F);
						GL11.glScalef(0.4F, 0.4F, 0.3F);

						GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

						GL11.glPopAttrib();
						GL11.glPopMatrix();
					}
					if (sides[3])
					{
						GL11.glPushMatrix();
						GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
						GL11.glEnable(GL11.GL_CULL_FACE);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

						GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0.7F);
						GL11.glScalef(0.4F, 0.4F, 0.3F);

						GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

						GL11.glPopAttrib();
						GL11.glPopMatrix();
					}
				}
			}
			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);
			bindTexture(RenderPipe.getTexture(mat, 0));
			RenderPipe.render(mat, ((TileEntityPipe) te).getSubID(), sides);
			GL11.glPopMatrix();
		}
		else
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);
			RenderPipe.render(mat, 0, new boolean[6]);
			GL11.glPopMatrix();
		}

	}

	public static ResourceLocation getTexture(FluidPartsMaterial mat, int pipeID)
	{
		if (mat != null)
		{
			Pair<FluidPartsMaterial, Integer> index = new Pair<FluidPartsMaterial, Integer>(mat, pipeID);
			if (!TEXTURES.containsKey(index))
			{
				String pipeName = "";
				if (EnumPipeType.get(pipeID) != null)
				{
					pipeName = EnumPipeType.get(pipeID).getName(pipeID);
				}
				TEXTURES.put(index, new ResourceLocation(ResonantInductionTransport.DOMAIN, ResonantInductionTransport.MODEL_DIRECTORY + "pipes/" + mat.matName + "/" + pipeName + "Pipe.png"));
			}
			return TEXTURES.get(index);
		}
		return TEXTURE;
	}

	public static ResourceLocation getTexture(int meta)
	{
		return getTexture(FluidPartsMaterial.getFromItemMeta(meta), FluidPartsMaterial.getType(meta));
	}

	public static void render(FluidPartsMaterial mat, int pipeID, boolean[] side)
	{
		if (mat == FluidPartsMaterial.WOOD)
		{
			MODEL_TROUGH_PIPE.render(side, false);
		}
		else if (mat == FluidPartsMaterial.STONE)
		{
			MODEL_TROUGH_PIPE.render(side, true);
		}
		else
		{
			MODEL_PIPE.render(side);
		}
	}

	public static void render(int meta, boolean[] bs)
	{
		render(FluidPartsMaterial.getFromItemMeta(meta), FluidPartsMaterial.getType(meta), bs);
	}

}