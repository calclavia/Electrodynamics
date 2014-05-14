package resonantinduction.mechanical.fluid.pipe;

import java.awt.Color;

import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.FluidRenderUtility;
import resonant.lib.render.RenderUtility;
import resonant.lib.utility.WorldUtility;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPipe implements ISimpleItemRenderer
{
	public static final RenderPipe INSTANCE = new RenderPipe();

	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "pipe.tcn");
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe.png");

	public void render(PartPipe part, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		render(part.getMaterialID(), part.getColor() > 0 ? ItemDye.dyeColors[part.getColor()] : -1, part.getAllCurrentConnections());
		GL11.glPopMatrix();

		GL11.glPushMatrix();

		FluidStack fluid = part.getPressureTank().getFluid();
		int capacity = part.getPressureTank().getCapacity();
		byte renderSides = part.getAllCurrentConnections();

		if (fluid != null && fluid.amount > 0)
		{
			double filledPercentage = Math.min((double) fluid.amount / (double) capacity, 1);
			double renderPercentage = fluid.getFluid().isGaseous() ? 1 : filledPercentage;

			int[] displayList = FluidRenderUtility.getFluidDisplayLists(fluid, part.world(), false);
			RenderUtility.bind(FluidRenderUtility.getFluidSheet(fluid));
			Color color = new Color(fluid.getFluid().getColor());
			GL11.glColor4d(color.getRed() / 255, color.getGreen() / 255, color.getBlue() / 255, fluid.getFluid().isGaseous() ? filledPercentage : 1);

			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glTranslatef((float) x + 0.35f, (float) y + 0.35f, (float) z + 0.35f);
			GL11.glScalef(0.33f, 0.33f, 0.33f);

			GL11.glCallList(displayList[(int) (renderPercentage * (FluidRenderUtility.DISPLAY_STAGES - 1))]);

			GL11.glPopAttrib();
			GL11.glPopMatrix();

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				if (WorldUtility.isEnabledSide(renderSides, direction))
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

					GL11.glScalef(0.33f, 0.33f, 0.33f);
					GL11.glCallList(displayList[(int) (renderPercentage * (FluidRenderUtility.DISPLAY_STAGES - 1))]);

					GL11.glPopAttrib();
					GL11.glPopMatrix();
				}
			}
		}
		GL11.glPopMatrix();

	}

	@SuppressWarnings("incomplete-switch")
	public static void render(int meta, int colorCode, byte sides)
	{
		RenderUtility.enableBlending();
		RenderUtility.bind(TEXTURE);
		EnumPipeMaterial material = EnumPipeMaterial.values()[meta];

		GL11.glColor4f(material.color.getRed() / 255f, material.color.getGreen() / 255f, material.color.getBlue() / 255f, 1);
		MODEL.renderOnly("Mid");

		/**
		 * Render each side
		 */
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (WorldUtility.isEnabledSide(sides, dir))
			{
				GL11.glColor4f(material.color.getRed() / 255f, material.color.getGreen() / 255f, material.color.getBlue() / 255f, 1);
				String prefix = null;

				switch (dir)
				{
					case DOWN:
						prefix = "Bottom";
						break;
					case UP:
						prefix = "Top";
						break;
					case NORTH:
						prefix = "Front";
						break;
					case SOUTH:
						prefix = "Back";
						break;
					case WEST:
						prefix = "Right";
						break;
					case EAST:
						prefix = "Left";
						break;
				}

				MODEL.renderOnly(prefix + "Inter", prefix + "Connect");

				if (colorCode > 0)
				{
					Color color = new Color(colorCode);
					GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);
				}

				MODEL.renderOnly(prefix + "Pipe");
			}
		}

		RenderUtility.disableBlending();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		render(itemStack.getItemDamage(), -1, Byte.parseByte("001100", 2));
		GL11.glPopMatrix();
	}
}