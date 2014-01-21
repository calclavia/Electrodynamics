/**
 * 
 */
package resonantinduction.electrical.battery;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
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
import calclavia.lib.render.RenderUtility;
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
	public static final WavefrontObject MODEL = (WavefrontObject) AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "battery/battery.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		glPushMatrix();

		for (int i = 2; i < 6; i++)
		{
			glPushMatrix();
			glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);
			glScalef(0.5f, 0.5f, 0.5f);
			ForgeDirection dir = ForgeDirection.getOrientation(i);

			switch (dir)
			{
				case NORTH:
					glRotatef(0, 0, 1, 0);
					break;
				case SOUTH:
					glRotatef(180, 0, 1, 0);
					break;
				case WEST:
					glRotatef(90, 0, 1, 0);
					break;
				case EAST:
					glRotatef(-90, 0, 1, 0);
					break;
			}

			/**
			 * If we're rendering in the world:
			 */
			if (t.worldObj != null)
			{
				TileBattery tile = (TileBattery) t;

				int energyLevel = (int) (((double) tile.energy.getEnergy() / (double) TileBattery.getEnergyForTier(tile.getBlockMetadata())) * 10);
				RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_level_" + energyLevel + ".png");
				MODEL.renderPart("Battery");

				// Render top and bottom
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CAP);
				if (!(new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("CapCorner");
				if (!(new Vector3(t).translate(ForgeDirection.DOWN).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("BaseCorner");

				// If quadrant with one external neighbor
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CAP);

				if (!(new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("CapEdge");
				if (!(new Vector3(t).translate(ForgeDirection.DOWN).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("BaseEdge");

				/*
				 * If quadrant with three external neighbors //can't have quadrant with 2 external
				 * neighbors in rectangular prism
				 */
				if (!(new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("CapInterior");
				if (!(new Vector3(t).translate(ForgeDirection.DOWN).getTileEntity(t.worldObj) instanceof TileBattery))
					MODEL.renderPart("BaseInterior");

				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CASE);

				if (tile.getInputDirections().contains(dir))
				{
					GL11.glColor3f(0, 0.294f, 0.498f);
				}
				else if (tile.getOutputDirections().contains(dir))
				{
					GL11.glColor3f(1, 0.478f, 0.01f);
				}

				MODEL.renderPart("BatteryCase");

				GL11.glColor3f(1, 1, 1);

				if (new Vector3(t).translate(ForgeDirection.UP).getTileEntity(t.worldObj) instanceof TileBattery)
					MODEL.renderPart("VertConnector");
			}
			else
			{
				// TODO: Fix energy level.
				RenderUtility.bind(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_level_0.png");
				MODEL.renderPart("Battery");
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CAP);
				MODEL.renderOnly("CapCorner", "BaseCorner", "CapEdge", "BaseEdge", "CapInterior", "BaseInterior");
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CASE);
				MODEL.renderOnly("BatteryCase");
			}

			glPopMatrix();
		}
		
		glPopMatrix();
	}
}
