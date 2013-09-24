package resonantinduction.render;

import java.util.Map;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

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
	private static final ResourceLocation WIRE_TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "wire.png");
	private static final ResourceLocation INSULATION_TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "insulation.png");
	public static final ModelWire WIRE_MODEL = new ModelWire();
	public static final ModelInsulation INSULATION_MODEL = new ModelInsulation();
	public static final Map<String, CCModel> models;
	public static Icon wireIcon;
	public static Icon insulationIcon;

    static
    {
        models = CCModel.parseObjModels(new ResourceLocation("resonantinduction", "models/wire.obj"), 7, new InvertX());
        for (CCModel c : models.values()) {
            c.apply(new Translation(.5, 0, .5));
            c.computeLighting(LightModel.standardLightModel);
            c.shrinkUVs(0.0005);
        }
    }


    public void renderModelAt(PartWire part, double x, double y, double z, float f)
	{
		if (part != null)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GL11.glScalef(1, -1, -1);

			EnumWireMaterial material = part.getMaterial();
			// Texture file
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(WIRE_TEXTURE);
			GL11.glColor4d(material.color.x, material.color.y, material.color.z, 1);

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

			if (part.isInsulated)
			{
				// Texture file
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(INSULATION_TEXTURE);
				Vector3 insulationColor = ResonantInduction.DYE_COLORS[part.dyeID];
				GL11.glColor4d(insulationColor.x, insulationColor.y, insulationColor.z, 1);

				if (adjacentConnections != null)
				{
					if (adjacentConnections[0] != null)
					{
						INSULATION_MODEL.renderBottom(0.0625f);
					}

					if (adjacentConnections[1] != null)
					{
						INSULATION_MODEL.renderTop(0.0625f);
					}

					if (adjacentConnections[2] != null)
					{
						INSULATION_MODEL.renderBack(0.0625f);
					}

					if (adjacentConnections[3] != null)
					{
						INSULATION_MODEL.renderFront(0.0625f);
					}

					if (adjacentConnections[4] != null)
					{
						INSULATION_MODEL.renderLeft(0.0625f);
					}

					if (adjacentConnections[5] != null)
					{
						INSULATION_MODEL.renderRight(0.0625f);
					}
				}

				INSULATION_MODEL.renderMiddle(0.0625f);
			}

			GL11.glPopMatrix();
		}
	}
	
	public static void registerIcons(IconRegister iconReg)
	{
		wireIcon = iconReg.registerIcon("resonantinduction:models/wire");
		insulationIcon = iconReg.registerIcon("resonantinduction:models/insulation" + (ResonantInduction.LO_FI_INSULATION ? "tiny" : ""));
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