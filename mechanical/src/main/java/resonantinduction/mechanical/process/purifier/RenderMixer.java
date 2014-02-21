package resonantinduction.mechanical.process.purifier;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderMixer extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "mixer.tcn");
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "mixer.png");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		if (t instanceof TileMixer)
		{
			TileMixer tile = (TileMixer) t;
			glPushMatrix();
			glTranslatef((float) x + 0.5F, (float) y + 0.5f, (float) z + 0.5F);
			RenderUtility.bind(TEXTURE);
			MODEL.renderOnly("centerTop", "centerBase");
			glPushMatrix();
			glRotatef((float) Math.toDegrees(tile.angle), 0, 1, 0);
			MODEL.renderAllExcept("centerTop", "centerBase");
			glPopMatrix();
			glPopMatrix();
		}
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		glTranslatef(0.5F, 0.5f, 1.5F);
		RenderUtility.bind(TEXTURE);
		MODEL.renderAll();
		glPopMatrix();
	}
}
