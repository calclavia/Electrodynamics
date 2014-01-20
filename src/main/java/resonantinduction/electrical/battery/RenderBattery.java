/**
 * 
 */
package resonantinduction.electrical.battery;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_CAP = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_base_cap_tex.png");
	public static final ResourceLocation TEXTURE_CASE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_case_tex.png");
	public static final ResourceLocation TEXTURE_LEVELS = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_levels.png");
	public static final WavefrontObject MODEL = (WavefrontObject) AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "battery/battery.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		for (int i = 2; i < 6; i++)
		{
			glPushMatrix();
			glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);
			glScalef(0.46f, 0.46f, 0.46f);
			GL11.glRotatef(90 * i, 0, 1, 0);
			ForgeDirection dir = ForgeDirection.getOrientation(i);

			if (t.worldObj != null)
			{
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_LEVELS);
				MODEL.renderPart("Battery");
				
				// Render top and bottom
				//if (!(new Vector3(t).translate(dir).getTileEntity(t.worldObj) instanceof TileBattery))
				{
					FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CAP);

					if (!(new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery))
						MODEL.renderPart("CapCorner");
					if (!(new Vector3(t).translate(ForgeDirection.DOWN).getTileEntity(t.worldObj) instanceof TileBattery))
						MODEL.renderPart("BaseCorner");
				}

				// If quadrant with one external neighbor
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CAP);
				
				if (!(new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("CapEdge");
				if (!(new Vector3(t).translate(ForgeDirection.DOWN).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("BaseEdge");

				// if quadrant with three external neighbors //can't have quadrant with 2 external
				// neighbors in rectangular prism
				if (!(new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("CapInterior");
				if (!(new Vector3(t).translate(ForgeDirection.DOWN).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("BaseInterior");

				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CASE);
				MODEL.renderPart("BatteryCase");

				if (new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery)
					MODEL.renderPart("VertConnector");
			}else
			{
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CAP);
				MODEL.renderAll();
			}
			
			glPopMatrix();
		}
	}
}
