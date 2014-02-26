package resonantinduction.archaic.gutter;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderFluidHelper;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGutter extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final RenderGutter INSTANCE = new RenderGutter();

	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "gutter.tcn");
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "gutter.png");

	public static void render(int meta, byte sides)
	{
		RenderUtility.bind(TEXTURE);

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (dir != ForgeDirection.UP && dir != ForgeDirection.DOWN)
			{
				if (!RenderUtility.canRenderSide(sides, dir))
				{
					GL11.glPushMatrix();
					RenderUtility.rotateBlockBasedOnDirection(dir);
					MODEL.renderOnly("left", "backCornerL", "frontCornerL");
					GL11.glPopMatrix();
				}
			}
		}

		if (!RenderUtility.canRenderSide(sides, ForgeDirection.DOWN))
			MODEL.renderOnly("base");
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		TileGutter tile = ((TileGutter) tileEntity);

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

		FluidStack liquid = tile.getInternalTank().getFluid();
		int capacity = tile.getInternalTank().getCapacity();
		byte renderSides = (tile instanceof TileGutter ? tile.renderSides : (byte) 0);

		render(0, renderSides);

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

			float xScale = RenderUtility.canRenderSide(renderSides, ForgeDirection.EAST) || RenderUtility.canRenderSide(renderSides, ForgeDirection.WEST) ? 1.01f : 0.8f;
			float zScale = RenderUtility.canRenderSide(renderSides, ForgeDirection.NORTH) || RenderUtility.canRenderSide(renderSides, ForgeDirection.SOUTH) ? 1.01f : 0.8f;
			GL11.glTranslatef(-xScale / 2, -0.45f, -zScale / 2);
			GL11.glScalef(xScale, 0.9f, zScale);

			GL11.glCallList(displayList[(int) (percentage * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(0.5, 0.5, 0.5);
		render(itemStack.getItemDamage(), Byte.parseByte("001100", 2));
		GL11.glPopMatrix();
	}
}