package resonantinduction.render;

import java.nio.FloatBuffer;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.ColourMultiplier;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import resonantinduction.ResonantInduction;
import resonantinduction.model.ModelInsulation;
import resonantinduction.model.ModelWire;
import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.multipart.PartWire;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TODO: Use ISBRH.
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderPartWire
{
	private static final ResourceLocation WIRE_SHINE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "white.png");
	public static final ModelWire WIRE_MODEL = new ModelWire();
	public static final ModelInsulation INSULATION_MODEL = new ModelInsulation();
	public static final Map<String, CCModel> models;
	public static Icon wireIcon;
	public static Icon insulationIcon;
	public static Icon breakIcon;
	public static FloatBuffer location = BufferUtils.createFloatBuffer(4);
	public static FloatBuffer specular = BufferUtils.createFloatBuffer(4);
	public static FloatBuffer zero = BufferUtils.createFloatBuffer(4);

    static
    {
        models = CCModel.parseObjModels(new ResourceLocation("resonantinduction", "models/wire.obj"), 7, new InvertX());
        for (CCModel c : models.values()) {
            c.apply(new Translation(.5, 0, .5));
            c.computeLighting(LightModel.standardLightModel);
            c.shrinkUVs(0.0005);
        }
        
		loadBuffer(location, 0,0,0,1);
		loadBuffer(specular, 2,2,2,1);
		loadBuffer(zero, 0,0,0,0);
		
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

    public void renderShine(PartWire part, double x, double y, double z, float f)
	{
		if (part != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_LIGHT0);
			GL11.glDisable(GL11.GL_LIGHT1);
			GL11.glEnable(GL11.GL_LIGHT3);
			
			GL11.glLight(GL11.GL_LIGHT3, GL11.GL_POSITION, location);
			
			GL11.glTranslatef((float) x+0.5F, (float) y+1.5F, (float) z+0.5F);
			GL11.glScalef(1.01F, -1.01F, -1.01F);
			GL11.glTranslatef((float) 0.F, (float) -0.01F, (float) 0.F);

			// Texture file
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(WIRE_SHINE);

			part.adjacentConnections = null;
			TileEntity[] adjacentConnections = part.getAdjacentConnections();

			if (adjacentConnections != null)
			{
				if (adjacentConnections[0] != null)
				{
					WIRE_MODEL.renderBottom();
				}

				if (adjacentConnections[1] != null)
				{
					WIRE_MODEL.renderTop();
				}

				if (adjacentConnections[2] != null)
				{
					WIRE_MODEL.renderBack();
				}

				if (adjacentConnections[3] != null)
				{
					WIRE_MODEL.renderFront();
				}

				if (adjacentConnections[4] != null)
				{
					WIRE_MODEL.renderLeft();
				}

				if (adjacentConnections[5] != null)
				{
					WIRE_MODEL.renderRight();
				}
			}

			WIRE_MODEL.renderMiddle();

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_LIGHT0);
			GL11.glEnable(GL11.GL_LIGHT1);
			GL11.glEnable(GL11.GL_LIGHT3);

			GL11.glPopMatrix();
		}
	}
	
	public static void registerIcons(IconRegister iconReg)
	{
		wireIcon = iconReg.registerIcon("resonantinduction:models/wire");
		insulationIcon = iconReg.registerIcon("resonantinduction:models/insulation" + (ResonantInduction.LO_FI_INSULATION ? "tiny" : ""));
		breakIcon = iconReg.registerIcon("resonantinduction:wire");
	}
	
	public void renderStatic(PartWire wire)
	{
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.useModelColours(true);
		CCRenderState.setBrightness(wire.world(), wire.x(), wire.y(), wire.z());
		renderSide(ForgeDirection.UNKNOWN, wire);
		wire.adjacentConnections = null;
		TileEntity[] adjacentTiles = wire.getAdjacentConnections();
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if (adjacentTiles[side.ordinal()] != null)
				renderSide(side, wire);			
		}
	}
	
	public void renderSide(ForgeDirection side, PartWire wire)
	{
		String name = side.name().toLowerCase();
		name = name.equals("unknown") ? "center" : name;
		Vector3 materialColour = wire.getMaterial().color;
		Colour colour = new ColourRGBA(materialColour.x, materialColour.y, materialColour.z, 1);
		renderPart(wireIcon, models.get(name), wire.x(), wire.y(), wire.z(), colour);
		if (wire.isInsulated())
		{
			Vector3 vecColour = ResonantInduction.DYE_COLORS[wire.dyeID]; 
			Colour insulationColour = new ColourRGBA(vecColour.x, vecColour.y, vecColour.z, 1);
			renderPart(insulationIcon, models.get(name+"Insulation"), wire.x(), wire.y(), wire.z(), insulationColour);
		}
	}
	
    public void renderPart(Icon icon, CCModel cc, double x, double y, double z, Colour colour) {
        cc.render(0, cc.verts.length,
                Rotation.sideOrientation(0, Rotation.rotationTo(0, 2)).at(codechicken.lib.vec.Vector3.center)
                .with(new Translation(x, y, z)), new IconTransformation(icon), new ColourMultiplier(colour));
    }


}