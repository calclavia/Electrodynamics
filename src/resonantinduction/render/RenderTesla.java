/**
 * 
 */
package resonantinduction.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

/**
 * @author Calclavia
 * 
 */
public class RenderTesla extends TileEntitySpecialRenderer
{
	public static final ModelBase MODEL;

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		MODEL.render(0.0625f);
		GL11.glPopMatrix();
	}

}
