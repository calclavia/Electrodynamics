package resonantinduction.electrical.wire.framed;

import java.nio.FloatBuffer;
import java.util.Map;

import net.minecraft.item.ItemDye;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.core.render.InvertX;
import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.ColourMultiplier;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author unpairedbracket
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderFramedWire
{
	public static final ResourceLocation WIRE_SHINE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "white.png");
	public static final Map<String, CCModel> models;
	public static final Map<String, CCModel> shinyModels;
	public static Icon wireIcon;
	public static Icon lainWireIcon;
	public static Icon insulationIcon;
	public static Icon breakIcon;
	public static FloatBuffer location = BufferUtils.createFloatBuffer(4);
	public static FloatBuffer specular = BufferUtils.createFloatBuffer(4);
	public static FloatBuffer zero = BufferUtils.createFloatBuffer(4);
	public static FloatBuffer defaultAmbient = BufferUtils.createFloatBuffer(4);
	public static final RenderFramedWire INSTANCE = new RenderFramedWire();

	static
	{
		models = CCModel.parseObjModels(new ResourceLocation(Reference.DOMAIN, "models/wire.obj"), 7, new InvertX());
		for (CCModel c : models.values())
		{
			c.apply(new Translation(.5, 0, .5));
			c.computeLighting(LightModel.standardLightModel);
			c.shrinkUVs(0.0005);
		}

		shinyModels = CCModel.parseObjModels(new ResourceLocation(Reference.DOMAIN, "models/wireShine.obj"), 7, new InvertX());
		for (CCModel c : shinyModels.values())
		{
			c.apply(new Translation(.5, 0, .5));
			c.computeLighting(LightModel.standardLightModel);
			c.shrinkUVs(0.0005);
		}

		loadBuffer(location, 0, 0, 0, 1);
		loadBuffer(specular, 1, 1, 1, 1);
		loadBuffer(zero, 0, 0, 0, 0);
		loadBuffer(defaultAmbient, 0.4F, 0.4F, 0.4F, 1);

		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, zero);

		GL11.glLight(GL11.GL_LIGHT3, GL11.GL_SPECULAR, specular);

		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, specular);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, zero);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, zero);
		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 128f);

	}

	public static void loadBuffer(FloatBuffer buffer, float... src)
	{
		buffer.clear();
		buffer.put(src);
		buffer.flip();
	}

	public void renderShine(PartFramedWire wire, double x, double y, double z, float f)
	{
		if (wire != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_LIGHT0);
			GL11.glDisable(GL11.GL_LIGHT1);
			GL11.glEnable(GL11.GL_LIGHT3);
			GL11.glLight(GL11.GL_LIGHT3, GL11.GL_POSITION, location);

			GL11.glTranslatef((float) x, (float) y, (float) z);
			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, zero);

			CCRenderState.reset();
			CCRenderState.useNormals(true);
			CCRenderState.changeTexture(WIRE_SHINE);
			CCRenderState.startDrawing(7);
			renderSideShine(ForgeDirection.UNKNOWN, wire);

			byte renderSides = wire.getAllCurrentConnections();

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if (PartFramedConnection.connectionMapContainsSide(renderSides, side))
					renderSideShine(side, wire);
			}
			CCRenderState.draw();

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_LIGHT0);
			GL11.glEnable(GL11.GL_LIGHT1);
			GL11.glDisable(GL11.GL_LIGHT3);

			GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, defaultAmbient);

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GL11.glPopMatrix();
		}
	}

	public void renderStatic(PartFramedWire wire)
	{
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.useModelColours(true);
		CCRenderState.setBrightness(wire.world(), wire.x(), wire.y(), wire.z());
		renderSide(ForgeDirection.UNKNOWN, wire);
		byte renderSides = wire.getAllCurrentConnections();

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if (PartFramedConnection.connectionMapContainsSide(renderSides, side))
			{
				renderSide(side, wire);
			}
		}
	}

	public void renderSide(ForgeDirection side, PartFramedWire wire)
	{
		String name = side.name().toLowerCase();
		name = name.equals("unknown") ? "center" : name;
		Colour colour = wire.getMaterial().color;
		renderPart(wireIcon, models.get(name), wire.x(), wire.y(), wire.z(), colour);

		if (wire.isInsulated())
		{
			Colour insulationColour = new ColourARGB(ItemDye.dyeColors[wire.getColor()]);
			insulationColour.a = (byte) 255;
			renderPart(insulationIcon, models.get(name + "Insulation"), wire.x(), wire.y(), wire.z(), insulationColour);
		}
	}

	public void renderSideShine(ForgeDirection side, PartFramedWire wire)
	{
		String name = side.name().toLowerCase();
		name = name.equals("unknown") ? "center" : name;
		renderPartShine(shinyModels.get(name));
	}

	public void renderPart(Icon icon, CCModel cc, double x, double y, double z, Colour colour)
	{
		cc.render(0, cc.verts.length, Rotation.sideOrientation(0, Rotation.rotationTo(0, 2)).at(codechicken.lib.vec.Vector3.center).with(new Translation(x, y, z)), new IconTransformation(icon), new ColourMultiplier(colour));
	}

	public void renderPartShine(CCModel cc)
	{
		cc.render(null, 0, 0);
	}
}