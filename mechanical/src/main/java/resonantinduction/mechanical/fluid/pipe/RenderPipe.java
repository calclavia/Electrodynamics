package resonantinduction.mechanical.fluid.pipe;

import java.awt.Color;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
public class RenderPipe implements ISimpleItemRenderer
{
	public static final RenderPipe INSTANCE = new RenderPipe();

	public static ModelPipe MODEL_PIPE = new ModelPipe();
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe.png");

	public void render(PartPipe part, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		render(0, part.getAllCurrentConnections());
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();

		FluidStack fluid = part.getInternalTank().getFluid();
		int capacity = part.getInternalTank().getCapacity();
		byte renderSides = part.getAllCurrentConnections();

		if (fluid != null && fluid.amount > 0)
		{
			float percentage = (float) fluid.amount / (float) capacity;
			int[] displayList = RenderFluidHelper.getFluidDisplayLists(fluid, part.world(), false);
			RenderUtility.bind(RenderFluidHelper.getFluidSheet(fluid));
			Color color = new Color(fluid.getFluid().getColor());
			GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, fluid.getFluid().isGaseous() ? 0.5f : 1);

			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslatef((float) x + 0.35f, (float) y + 0.35f, (float) z + 0.35f);
			GL11.glScalef(0.3f, 0.3f, 0.3f);

			GL11.glCallList(displayList[(int) (percentage * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

			GL11.glPopAttrib();
			GL11.glPopMatrix();

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				if (RenderUtility.canRenderSide(renderSides, direction))
				{
					GL11.glPushMatrix();
					GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					switch (direction.ordinal())
					{
						case 0:
							GL11.glTranslatef((float) x + 0.35F, (float) y - 0f, (float) z + 0.35F);
							break;
						case 1:
							GL11.glTranslatef((float) x + 0.35F, (float) y + 0.65f, (float) z + 0.35F);
							break;
						case 2:
							GL11.glTranslatef((float) x + 0.35F, (float) y + 0.35f, (float) z + 0F);
							break;
						case 3:
							GL11.glTranslatef((float) x + 0.35F, (float) y + 0.35f, (float) z + 0.65F);
							break;
						case 4:
							GL11.glTranslatef((float) x + 0F, (float) y + 0.35f, (float) z + 0.35F);
							break;
						case 5:
							GL11.glTranslatef((float) x + 0.65F, (float) y + 0.35f, (float) z + 0.35F);
							break;
					}

					GL11.glScalef(0.3f, 0.3f, 0.3f);
					GL11.glCallList(displayList[(int) (percentage * (RenderFluidHelper.DISPLAY_STAGES - 1))]);

					GL11.glPopAttrib();
					GL11.glPopMatrix();
				}
			}
		}
		GL11.glPopMatrix();

	}

	public static void render(int meta, byte sides)
	{
		RenderUtility.enableBlending();
		RenderUtility.bind(TEXTURE);
		MODEL_PIPE.render(sides);
		RenderUtility.disableBlending();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 1.5F, 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		render(itemStack.getItemDamage(), Byte.parseByte("001100", 2));
		GL11.glPopMatrix();
	}
}