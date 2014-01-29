package resonantinduction.electrical.multimeter;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.archaic.Archaic;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
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
		for (int i = 2; i < 6; i++)
		{
			ForgeDirection check = ForgeDirection.getOrientation(i);

			if (!part.hasMultimeter(part.x() + check.offsetX, part.y() + check.offsetY, part.z() + check.offsetZ))
			{
				if (dir.offsetX != 0 && check.offsetZ != 0)
				{
					if (dir.offsetX != check.offsetZ)
					{
						RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
					}
					else if (dir.offsetX == check.offsetZ)
					{
						RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
					}
				}
				if (dir.offsetZ != 0 && check.offsetX != 0)
				{
					if (dir.offsetZ == check.offsetX)
					{
						RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
					}
					else if (dir.offsetZ != check.offsetX)
					{
						RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
					}
				}
			}
		}

		GL11.glPopMatrix();

		/**
		 * Only one block renders this text.
		 * Render all the multimeter text
		 */
		if (part.getNetwork().center.distance(new Vector3(part.x(), part.y(), part.z()).translate(0.5)) < 0.8)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
			Vector3 centerTranslation = part.getNetwork().center.clone().subtract(part.x(), part.y(), part.z()).add(-0.5);
			GL11.glTranslated(centerTranslation.x, centerTranslation.y, centerTranslation.z);
			RenderUtility.rotateFaceBlockToSideOutwards(part.getDirection().getOpposite());
			GL11.glTranslated(0, 0.05, 0);
			String display = UnitDisplay.getDisplay(part.getNetwork().graph.get(0), Unit.JOULES);
			if (dir.offsetX == 0)
				RenderUtility.renderText(display, (float) (part.getNetwork().size.x * 0.9f), 0.5f);
			if (dir.offsetZ == 0)
				RenderUtility.renderText(display, (float) (part.getNetwork().size.z * 0.9f), 0.5f);
			GL11.glPopMatrix();
		}

	}
}