package resonantinduction.electrical.multimeter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import calclavia.lib.render.RenderUtility;
import resonantinduction.archaic.Archaic;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Class used to render text onto the multimeter block.
 * 
 * The more space we have, the more information and detail we render.
 * 
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderMultimeter
{
	public static final ModelMultimeter MODEL = new ModelMultimeter();
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "multimeter.png");

	public static void render(PartMultimeter part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		RenderUtility.rotateFaceBlockToSideOutwards(part.getDirection().getOpposite());
		GL11.glPushMatrix();
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		// Render the main panel
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, Archaic.blockMachinePart, ResonantInduction.loadedIconMap.get(Reference.PREFIX + "multimeter_screen"));
		ForgeDirection dir = part.getDirection();
		final int metadata = 8;
		// Render edges
		// UP
		if (!part.hasMultimeter(part.x(), part.y() + 1, part.z()))
			RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, Archaic.blockMachinePart, null, metadata);
		// DOWN
		if (!part.hasMultimeter(part.x(), part.y() - 1, part.z()))
			RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
		// LEFT
		if (((dir == ForgeDirection.WEST || dir == ForgeDirection.EAST) && !part.hasMultimeter(part.x(), part.y(), part.z() - 1)) || ((dir == ForgeDirection.SOUTH || dir == ForgeDirection.NORTH) && !part.hasMultimeter(part.x() + 1, part.y(), part.z())))
			RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
		// RIGHT
		if (((dir == ForgeDirection.WEST || dir == ForgeDirection.EAST) && !part.hasMultimeter(part.x(), part.y(), part.z() + 1)) || ((dir == ForgeDirection.SOUTH || dir == ForgeDirection.NORTH) && !part.hasMultimeter(part.x() - 1, part.y(), part.z())))
			RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
		GL11.glPopMatrix();
		GL11.glTranslated(0, 0.05, 0);
		// Render all the multimeter text
		RenderUtility.renderText("" + part.getDetectedEnergy(), 0.8f);
		GL11.glPopMatrix();
	}

}