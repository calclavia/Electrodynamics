package resonantinduction.electrical.generator;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import resonantinduction.core.Reference;
import cpw.mods.fml.client.FMLClientHandler;

/**
 * @author Calclavia
 * 
 */
public class RenderGenerator extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "generator.png");
	public static final ModelGenerator MODEL = new ModelGenerator();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		bindTexture(TEXTURE);
		GL11.glRotatef(180, 0f, 0f, 1f);

		// Rotate the model
		int facingDirection = t.getBlockMetadata();
		switch (facingDirection)
		{
			case 2:
				GL11.glRotatef(180, 0f, 1f, 0f);
				break;
			case 3:
				GL11.glRotatef(0, 0f, 1f, 0f);
				break;
			case 4:
				GL11.glRotatef(90, 0f, 1f, 0f);
				break;
			case 5:
				GL11.glRotatef(-90, 0f, 1f, 0f);
				break;
		}
		MODEL.render(0.0625F);
		glPopMatrix();
	}
}
