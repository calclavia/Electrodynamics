package resonantinduction.transformer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.CalclaviaRenderHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTransformer
{
	private static final ModelTransformer MODEL = new ModelTransformer();
	public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "transformer.png");

	public static void render(PartTransformer part, double x, double y, double z)
	{
		String status = StatCollector.translateToLocal(part.stepUp() ? "tooltip.transformer.stepUp" : "tooltip.transformer.stepDown");
		String name = StatCollector.translateToLocal(ResonantInduction.itemTransformer.getUnlocalizedName() + "." + (int) Math.pow(2, part.multiplier + 1) + "x.name");

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		MovingObjectPosition movingPosition = player.rayTrace(5, 1f);

		if (movingPosition != null)
		{
			if (new Vector3(x, y, z).equals(new Vector3(movingPosition)))
			{
				CalclaviaRenderHelper.renderFloatingText(status, (float) ((float) x + .5), (float) y - 1, (float) ((float) z + .5));
				CalclaviaRenderHelper.renderFloatingText(name, (float) ((float) x + .5), (float) y - .70F, (float) ((float) z + .5));
			}
		}

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
		MODEL.render(null, 0, 0, 0, 0, 0, 0.0625F);
		//this.model.renderCores(te.getInput(), te.getOutput(), 0.0625F);

		// this.bindTexture(TextureLocations.MODEL_TRANSFORMER_INPUT);
		//this.model.renderIO(te.getInput(), 0.0625F);

		// this.bindTexture(TextureLocations.MODEL_TRANSFORMER_OUTPUT);
		//this.model.renderIO(te.getOutput(), 0.0625F);

		GL11.glPopMatrix();
	}
}