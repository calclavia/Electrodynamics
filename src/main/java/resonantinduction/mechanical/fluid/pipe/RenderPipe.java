package resonantinduction.mechanical.fluid.pipe;

import java.util.HashMap;

import javax.rmi.CORBA.Tie;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderFluidHelper;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetworkTile;
import resonantinduction.old.client.model.ModelOpenTrough;
import resonantinduction.old.client.model.ModelPipe;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPipe extends TileEntitySpecialRenderer
{
	public static ModelPipe MODEL_PIPE = new ModelPipe();
	public static ModelOpenTrough MODEL_TROUGH_PIPE = new ModelOpenTrough();
	private static HashMap<Pair<FluidContainerMaterial, Integer>, ResourceLocation> TEXTURES = new HashMap<Pair<FluidContainerMaterial, Integer>, ResourceLocation>();
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "pipe.png");

	@Override
	public void renderTileEntityAt(TileEntity te, double d, double d1, double d2, float f)
	{
		// Texture file

		FluidContainerMaterial mat = FluidContainerMaterial.IRON;
		if (te.getBlockMetadata() < FluidContainerMaterial.values().length)
		{
			mat = FluidContainerMaterial.values()[te.getBlockMetadata()];
		}

		if (te instanceof TilePipe)
		{
			TilePipe tile = (TilePipe) te;

			if (mat == FluidContainerMaterial.WOOD || mat == FluidContainerMaterial.STONE)
			{
				FluidStack liquid = tile.getTank().getFluid();
				int cap = tile.getTankInfo()[0].capacity;

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

					for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
					{
						if (tile.canRenderSide(direction))
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
									GL11.glTranslatef((float) d + 0F, (float) d1 + 0.1F, (float) d2 + 0.3F);
									break;
								case 5:
									GL11.glTranslatef((float) d + 0.7F, (float) d1 + 0.1F, (float) d2 + 0.3F);
									break;
								case 2:
									GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0F);
									break;
								case 3:
									GL11.glTranslatef((float) d + 0.3F, (float) d1 + 0.1F, (float) d2 + 0.7F);
									break;
							}
							GL11.glScalef(0.3F, 0.4F, 0.4F);

							GL11.glCallList(displayList[(int) (per * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

							GL11.glPopAttrib();
							GL11.glPopMatrix();
						}
					}
				}
			}

			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);
			bindTexture(RenderPipe.getTexture(mat, 0));
			render(mat, tile.getSubID(), tile.renderSides);
			GL11.glPopMatrix();
		}
		else
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
			GL11.glScalef(1.0F, -1F, -1F);
			render(mat, 0, (byte) 0b0);
			GL11.glPopMatrix();
		}

	}

	public static ResourceLocation getTexture(FluidContainerMaterial mat, int pipeID)
	{
		if (mat != null)
		{
			Pair<FluidContainerMaterial, Integer> index = new Pair<FluidContainerMaterial, Integer>(mat, pipeID);

			if (!TEXTURES.containsKey(index))
			{
				String pipeName = "";
				if (EnumPipeType.get(pipeID) != null)
				{
					pipeName = EnumPipeType.get(pipeID).getName(pipeID);
				}
				TEXTURES.put(index, new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe/" + mat.matName + ".png"));
			}
			return TEXTURES.get(index);
		}
		return TEXTURE;
	}

	public static ResourceLocation getTexture(int meta)
	{
		return getTexture(FluidContainerMaterial.getFromItemMeta(meta), FluidContainerMaterial.getType(meta));
	}

	public static void render(FluidContainerMaterial mat, int pipeID, byte side)
	{
		if (mat == FluidContainerMaterial.WOOD)
		{
			// MODEL_TROUGH_PIPE.render(side, false);
		}
		else if (mat == FluidContainerMaterial.STONE)
		{
			// MODEL_TROUGH_PIPE.render(side, true);
		}
		else
		{
			System.out.println(Integer.toBinaryString(side));
			if (TileFluidNetworkTile.canRenderSide(side,ForgeDirection.DOWN))
			{
				MODEL_PIPE.renderBottom();
			}
			if (TileFluidNetworkTile.canRenderSide(side,ForgeDirection.UP))
			{
				MODEL_PIPE.renderTop();
			}
			if (TileFluidNetworkTile.canRenderSide(side,ForgeDirection.NORTH))
			{
				MODEL_PIPE.renderBack();
			}
			if (TileFluidNetworkTile.canRenderSide(side,ForgeDirection.SOUTH))
			{
				MODEL_PIPE.renderFront();
			}
			if (TileFluidNetworkTile.canRenderSide(side, ForgeDirection.WEST))
			{
				MODEL_PIPE.renderLeft();
			}
			if (TileFluidNetworkTile.canRenderSide(side, ForgeDirection.EAST))
			{
				MODEL_PIPE.renderRight();
			}

			MODEL_PIPE.renderMiddle();
		}
	}

	public static void render(int meta, byte sides)
	{
		render(FluidContainerMaterial.getFromItemMeta(meta), FluidContainerMaterial.getType(meta), sides);
	}

}