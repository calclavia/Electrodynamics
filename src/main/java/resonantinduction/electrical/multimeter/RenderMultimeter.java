package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.archaic.Archaic;
import resonantinduction.core.Reference;
import resonantinduction.core.handler.TextureHookHandler;
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
	public static final RenderMultimeter INSTANCE = new RenderMultimeter();
	private final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "multimeter.png");

	public static void render()
	{
		GL11.glPushMatrix();
		GL11.glRotatef(90, 1, 0, 0);
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		// Render the main panel
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, Archaic.blockMachinePart, TextureHookHandler.loadedIconMap.get(Reference.PREFIX + "multimeter_screen"));
		ForgeDirection dir = ForgeDirection.NORTH;
		final int metadata = 8;
		// Render edges
		// UP
		RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, Archaic.blockMachinePart, null, metadata);
		// DOWN
		RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, Archaic.blockMachinePart, null, metadata);
		// LEFT
		for (int i = 2; i < 6; i++)
		{
			ForgeDirection check = ForgeDirection.getOrientation(i);

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

		GL11.glPopMatrix();
	}

	public static void render(PartMultimeter part, double x, double y, double z)
	{
		ForgeDirection dir = part.getDirection();

		/**
		 * Render the shell of the multimeter.
		 */
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
		RenderUtility.rotateFaceBlockToSideOutwards(part.getDirection().getOpposite());
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		// Render the main panel
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, Archaic.blockMachinePart, TextureHookHandler.loadedIconMap.get(Reference.PREFIX + "multimeter_screen"));
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
		if (part.getNetwork().isEnabled && part.isPrimaryRendering())
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
			Vector3 centerTranslation = part.getNetwork().center.clone().subtract(part.x(), part.y(), part.z()).add(-0.5);
			GL11.glTranslated(centerTranslation.x, centerTranslation.y, centerTranslation.z);
			RenderUtility.rotateFaceBlockToSideOutwards(part.getDirection().getOpposite());
			GL11.glTranslated(0, 0.05, 0);

			// TODO: Add other dispaly info support.
			List<String> information = new ArrayList<String>();
			information.add(UnitDisplay.getDisplay(part.getNetwork().energyGraph.get(0), Unit.JOULES));

			if (part.getNetwork().energyCapacityGraph.get(0) > 0)
			{
				information.add("Max: " + UnitDisplay.getDisplay(part.getNetwork().energyCapacityGraph.get(0), Unit.JOULES));
			}

			if (part.getNetwork().torqueGraph.get(0) != 0)
			{
				information.add("Torque: " + UnitDisplay.getDisplayShort(part.getNetwork().torqueGraph.get(0), Unit.NEWTON_METER));
			}
			if (part.getNetwork().angularVelocityGraph.get(0) != 0)
			{
				information.add("Speed: " + UnitDisplay.roundDecimals(part.getNetwork().angularVelocityGraph.get(0)));
			}

			GL11.glTranslatef(0, 0, -0.18f * (information.size() / 2));

			for (int i = 0; i < information.size(); i++)
			{
				String info = information.get(i);

				GL11.glPushMatrix();
				GL11.glTranslatef(0, 0, 0.2f * i);
				if (dir.offsetX == 0)
					RenderUtility.renderText(info, (float) (part.getNetwork().size.x * 0.9f), 0.5f);
				if (dir.offsetZ == 0)
					RenderUtility.renderText(info, (float) (part.getNetwork().size.z * 0.9f), 0.5f);
				GL11.glPopMatrix();
			}

			GL11.glPopMatrix();
		}

	}
}