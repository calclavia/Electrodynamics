package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonant.api.items.ISimpleItemRenderer;
import resonant.lib.render.RenderUtility;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
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
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, ResonantInduction.blockIndustrialStone, RenderUtility.loadedIconMap.get(Reference.PREFIX + "multimeter_screen"));
		ForgeDirection dir = ForgeDirection.NORTH;
		final int metadata = 8;
		// Render edges
		// UP
		RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockIndustrialStone, null, metadata);
		// DOWN
		RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);

		for (int i = 0; i < 6; i++)
		{
			ForgeDirection check = ForgeDirection.getOrientation(i);

			if (dir.offsetX != 0 && check.offsetZ != 0)
			{
				if (dir.offsetX != check.offsetZ)
				{
					RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
				}
				else if (dir.offsetX == check.offsetZ)
				{
					RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
				}
			}
			if (dir.offsetZ != 0 && check.offsetX != 0)
			{
				if (dir.offsetZ == check.offsetX)
				{
					RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
				}
				else if (dir.offsetZ != check.offsetX)
				{
					RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
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
		RenderUtility.renderCube(-0.5, -0.05, -0.5, 0.5, 0.05, 0.5, ResonantInduction.blockIndustrialStone, RenderUtility.loadedIconMap.get(Reference.PREFIX + "multimeter_screen"));
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
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
						else if (dir.offsetX == check.offsetZ)
							RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
					}
					else if (check.offsetY != 0)
					{
						if (check.offsetY > 0)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockIndustrialStone, null, metadata);
						else
							RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
					}
				}

				if (dir.offsetZ != 0)
				{
					if (check.offsetX != 0)
					{
						if (dir.offsetZ == check.offsetX)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
						else if (dir.offsetZ != check.offsetX)
							RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
					}
					else if (check.offsetY != 0)
					{
						if (check.offsetY > 0)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockIndustrialStone, null, metadata);
						else
							RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
					}
				}

				if (dir.offsetY != 0)
				{
					if (check.offsetX != 0)
					{
						if (dir.offsetY != check.offsetX)
							RenderUtility.renderCube(0.44, -0.0501, -0.501, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
						else if (dir.offsetY == check.offsetX)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, -0.44, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
					}
					else if (check.offsetZ != 0)
					{
						if (dir.offsetY != check.offsetZ)
							RenderUtility.renderCube(-0.501, -0.0501, 0.44, 0.501, 0.0501, 0.501, ResonantInduction.blockIndustrialStone, null, metadata);
						else if (dir.offsetY == check.offsetZ)
							RenderUtility.renderCube(-0.501, -0.0501, -0.501, 0.501, 0.0501, -0.44, ResonantInduction.blockIndustrialStone, null, metadata);
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

			if (part.getDirection().offsetY != 0)
			{
				GL11.glRotatef(90, 0, 1, 0);
				RenderUtility.rotateBlockBasedOnDirection(part.getFacing());
			}

			GL11.glTranslated(0, 0.05, 0);

			List<String> information = new ArrayList<String>();

			for (int i = 0; i < part.getNetwork().graphs.size(); i++)
			{
				if (part.getNetwork().graphs.get(i).get() != null && !part.getNetwork().graphs.get(i).get().equals(part.getNetwork().graphs.get(i).getDefault()))
				{
					information.add(part.getNetwork().getDisplay(i));
				}
			}

			if (information.size() <= 0)
				information.add(LanguageUtility.getLocal("tooltip.noInformation"));

			float displacement = 0.72f / information.size();
			float maxScale = (float) (part.getNetwork().size.x + part.getNetwork().size.z) * 0.004f;
			GL11.glTranslatef(0, 0, -displacement * (information.size() / 2f));

			for (int i = 0; i < information.size(); i++)
			{
				String info = information.get(i);

				GL11.glPushMatrix();
				GL11.glTranslatef(0, 0, displacement * i);

				if (dir.offsetX != 0)
					RenderUtility.renderText(info, (float) (part.getNetwork().size.z * 0.9f), maxScale);
				else if (dir.offsetY != 0)
					RenderUtility.renderText(info, (float) (Math.min(part.getNetwork().size.x, part.getNetwork().size.z) * 0.9f), maxScale);
				else if (dir.offsetZ != 0)
					RenderUtility.renderText(info, (float) (part.getNetwork().size.x * 0.9f), maxScale);
				GL11.glPopMatrix();
			}

			GL11.glPopMatrix();
		}

	}
}