package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
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
public class RenderMultimeter implements ISimpleItemRenderer
{
	public static final RenderMultimeter INSTANCE = new RenderMultimeter();
	private final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "multimeter.png");

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		render();
	}

	public static void render()
	{
		GL11.glPushMatrix();
		GL11.glRotatef(90, 1, 0, 0);
		RenderUtility.bind(TextureMap.locationBlocksTexture);
		// Render the main panel
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, ResonantInduction.blockMachinePart, RenderUtility.loadedIconMap.get(Reference.PREFIX + "multimeter_screen"));
		ForgeDirection dir = ForgeDirection.NORTH;
		final int metadata = 8;
		// Render edges
		// UP
		RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockMachinePart, null, metadata);
		// DOWN
		RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);

		for (int i = 0; i < 6; i++)
		{
			ForgeDirection check = ForgeDirection.getOrientation(i);

			if (dir.offsetX != 0 && check.offsetZ != 0)
			{
				if (dir.offsetX != check.offsetZ)
				{
					RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
				}
				else if (dir.offsetX == check.offsetZ)
				{
					RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
				}
			}
			if (dir.offsetZ != 0 && check.offsetX != 0)
			{
				if (dir.offsetZ == check.offsetX)
				{
					RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
				}
				else if (dir.offsetZ != check.offsetX)
				{
					RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
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
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, ResonantInduction.blockMachinePart, RenderUtility.loadedIconMap.get(Reference.PREFIX + "multimeter_screen"));
		final int metadata = 8;

		// Render edges
		for (int i = 0; i < 6; i++)
		{
			ForgeDirection check = ForgeDirection.getOrientation(i);

			if (!part.hasMultimeter(part.x() + check.offsetX, part.y() + check.offsetY, part.z() + check.offsetZ))
			{
				if (dir.offsetX != 0)
				{
					if (check.offsetZ != 0)
					{
						if (dir.offsetX != check.offsetZ)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
						else if (dir.offsetX == check.offsetZ)
							RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
					}
					else if (check.offsetY != 0)
					{
						if (dir.offsetX != check.offsetY)
							RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
						else if (dir.offsetX == check.offsetY)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockMachinePart, null, metadata);
					}
				}

				if (dir.offsetZ != 0)
				{
					if (check.offsetX != 0)
					{
						if (dir.offsetZ == check.offsetX)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
						else if (dir.offsetZ != check.offsetX)
							RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
					}
					else if (check.offsetY != 0)
					{
						if (dir.offsetZ != check.offsetY)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockMachinePart, null, metadata);
						else if (dir.offsetZ == check.offsetY)
							RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
					}
				}

				if (dir.offsetY != 0)
				{
					if (check.offsetX != 0)
					{
						if (dir.offsetY != check.offsetX)
							RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
						else if (dir.offsetY == check.offsetX)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
					}
					else if (check.offsetZ != 0)
					{
						if (dir.offsetY != check.offsetZ)
							RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockMachinePart, null, metadata);
						else if (dir.offsetY == check.offsetZ)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockMachinePart, null, metadata);
					}
				}

			}
		}

		GL11.glPopMatrix();

		/**
		 * Only one block renders this text.
		 * Render all the multimeter text
		 */
		if (part.getNetwork().isEnabled && part.isPrimary)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
			Vector3 centerTranslation = part.getNetwork().center.clone().subtract(part.x(), part.y(), part.z()).add(-0.5);
			GL11.glTranslated(centerTranslation.x, centerTranslation.y, centerTranslation.z);
			RenderUtility.rotateFaceBlockToSideOutwards(part.getDirection().getOpposite());
			GL11.glTranslated(0, 0.07, 0);

			List<String> information = new ArrayList<String>();

			if (part.getNetwork().energyGraph.get(0) > 0 && part.getNetwork().energyGraph.points.size() > 0)
			{
				information.add(UnitDisplay.getDisplay(part.getNetwork().energyGraph.get(0), Unit.JOULES));

				/**
				 * Compute power
				 */
				long power = 0;

				for (long point : part.getNetwork().energyGraph.points)
				{
					power += point;
				}

				power /= part.getNetwork().energyGraph.points.size();

				if (power > 0)
					information.add("Power: " + UnitDisplay.getDisplay(power * 20, Unit.WATT));
			}

			if (part.getNetwork().energyCapacityGraph.get(0) > 0)
				information.add("Max: " + UnitDisplay.getDisplay(part.getNetwork().energyCapacityGraph.get(0), Unit.JOULES));

			if (part.getNetwork().voltageGraph.get(0) > 0)
				information.add(UnitDisplay.getDisplay(part.getNetwork().voltageGraph.get(0), Unit.VOLTAGE));

			if (part.getNetwork().torqueGraph.get(0) != 0)
				information.add("Torque: " + UnitDisplay.getDisplayShort(part.getNetwork().torqueGraph.get(0), Unit.NEWTON_METER));

			if (part.getNetwork().angularVelocityGraph.get(0) != 0)
				information.add("Speed: " + UnitDisplay.roundDecimals(part.getNetwork().angularVelocityGraph.get(0)));

			if (part.getNetwork().fluidGraph.get(0) != 0)
				information.add("Fluid: " + UnitDisplay.getDisplay(part.getNetwork().fluidGraph.get(0), Unit.LITER));

			if (information.size() <= 0)
				information.add("No information");

			float displacement = 0.72f / information.size();
			float maxScale = (float) (part.getNetwork().size.x + part.getNetwork().size.z) * 0.004f;
			GL11.glTranslatef(0, 0, -displacement * (information.size() / 2f));

			for (int i = 0; i < information.size(); i++)
			{
				String info = information.get(i);

				GL11.glPushMatrix();
				GL11.glTranslatef(0, 0, displacement * i);
				if (dir.offsetX == 0)
					RenderUtility.renderText(info, (float) (part.getNetwork().size.x * 0.9f), maxScale);
				if (dir.offsetZ == 0)
					RenderUtility.renderText(info, (float) (part.getNetwork().size.z * 0.9f), maxScale);
				GL11.glPopMatrix();
			}

			GL11.glPopMatrix();
		}

	}
}