package resonantinduction.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.client.model.ModelTankSide;
import resonantinduction.transport.fluid.TileEntityTank;

import com.builtbroken.minecraft.helpers.ColorCode;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTank extends TileEntitySpecialRenderer
{
	private ModelTankSide model;

	// TODO https://www.opengl.org/sdk/docs/man/xhtml/glDepthMask.xml <- fix z fighting
	public RenderTank()
	{
		model = new ModelTankSide();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		FluidStack liquid = tileEntity instanceof TileEntityTank ? ((TileEntityTank) tileEntity).getTankInfo()[0].fluid : null;
		this.renderTank(tileEntity, x, y, z, 0, liquid);
	}

	public void renderTank(TileEntity tileEntity, double x, double y, double z, int meta, FluidStack liquid)
	{
		boolean[] render = new boolean[6];
		if (tileEntity instanceof TileEntityTank)
		{
			render = ((TileEntityTank) tileEntity).renderConnection;
		}
		if (liquid != null && liquid.amount > 100)
		{

			int[] displayList = RenderFluidHelper.getFluidDisplayLists(liquid, tileEntity.worldObj, false);

			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			bindTexture(RenderFluidHelper.getFluidSheet(liquid));

			GL11.glTranslatef((float) x, (float) y, (float) z);
			GL11.glScalef(1.01F, 1.01F, 1.01F);
			int cap = tileEntity instanceof TileEntityTank ? ((TileEntityTank) tileEntity).getTankInfo()[0].capacity : liquid.amount;
			GL11.glCallList(displayList[(int) ((float) liquid.amount / (float) (cap) * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}

		boolean bot = render[1];
		boolean top = render[0];
		boolean north = render[2];
		boolean south = render[3];
		boolean east = render[5];
		boolean west = render[4];
		for (int i = 0; i < 4; i++)
		{
			ForgeDirection dir = ForgeDirection.getOrientation(i + 2);
			if (!render[dir.getOpposite().ordinal()])
			{
				GL11.glPushMatrix();

				GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
				GL11.glScalef(1.0F, -1F, -1F);
				boolean left = false;
				boolean right = false;
				switch (dir)
				{
					case NORTH:
						GL11.glRotatef(180f, 0f, 1f, 0f);
						left = west;
						right = east;
						break;
					case SOUTH:
						GL11.glRotatef(0f, 0f, 1f, 0f);
						left = east;
						right = west;
						break;
					case WEST:
						GL11.glRotatef(90f, 0f, 1f, 0f);
						left = south;
						right = north;
						break;
					case EAST:
						GL11.glRotatef(270f, 0f, 1f, 0f);
						left = north;
						right = south;
						break;
				}
				bindTexture(this.getTexture(tileEntity.getBlockType().blockID, tileEntity.getBlockMetadata()));
				model.render(0.0625F, left, right, top, bot);
				GL11.glPopMatrix();
			}
		}

	}

	public ResourceLocation getTexture(int block, int meta)
	{
		String texture = "";
		if (ColorCode.get(meta) == ColorCode.RED)
		{
			texture = "textures/blocks/obsidian.png";
		}
		else
		{
			texture = "textures/blocks/iron_block.png";
		}
		return new ResourceLocation(texture);
	}
}