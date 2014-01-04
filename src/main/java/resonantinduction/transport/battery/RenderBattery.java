/**
 * 
 */
package resonantinduction.transport.battery;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.transport.model.ModelBattery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "battery.png");
	public static final ResourceLocation TEXTURE_MULTI = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "battery_multi.png");

	public static final ModelBattery MODEL = new ModelBattery();

	/*
	 * public static final Map<String, CCModel> MODELS;
	 * static
	 * {
	 * MODELS = CCModel.parseObjModels(new ResourceLocation("resonantinduction", "models/wire.obj"),
	 * 7, new InvertX());
	 * for (CCModel c : MODELS.values())
	 * {
	 * c.apply(new Translation(.5, 0, .5));
	 * c.computeLighting(LightModel.standardLightModel);
	 * c.shrinkUVs(0.0005);
	 * }
	 * loadBuffer(location, 0, 0, 0, 1);
	 * loadBuffer(specular, 1, 1, 1, 1);
	 * loadBuffer(zero, 0, 0, 0, 0);
	 * loadBuffer(defaultAmbient, 0.4F, 0.4F, 0.4F, 1);
	 * GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, zero);
	 * GL11.glLight(GL11.GL_LIGHT3, GL11.GL_SPECULAR, specular);
	 * GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, specular);
	 * GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, zero);
	 * GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, zero);
	 * GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 128f);
	 * }
	 * public void renderStatic(TileBattery battery)
	 * {
	 * TextureUtils.bindAtlas(0);
	 * CCRenderState.reset();
	 * CCRenderState.useModelColours(true);
	 * CCRenderState.setBrightness(battery.worldObj, battery.xCoord, battery.yCoord,
	 * battery.zCoord);
	 * renderPart(ForgeDirection.UNKNOWN, wire);
	 * byte renderSides = wire.getAllCurrentConnections();
	 * for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
	 * {
	 * if (PartWire.connectionMapContainsSide(renderSides, side))
	 * {
	 * renderSide(side, wire);
	 * }
	 * }
	 * }
	 * public void renderPart(Icon icon, CCModel cc, double x, double y, double z, Colour colour)
	 * {
	 * cc.render(0, cc.verts.length, Rotation.sideOrientation(0, Rotation.rotationTo(0,
	 * 2)).at(codechicken.lib.vec.Vector3.center).with(new Translation(x, y, z)), new
	 * IconTransformation(icon), new ColourMultiplier(colour));
	 * }
	 */

	static IModelCustom advancedmodel = AdvancedModelLoader.loadModel(ResonantInduction.MODEL_DIRECTORY + "battery.obj");

	public static void render()
	{
		/*
		 * advancedmodel.renderAll();
		 * FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderBattery.TEXTURE);
		 */
	}

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		this.bindTexture(TEXTURE);

		MODEL.render(0.0625f);
		GL11.glPopMatrix();
	}
}
