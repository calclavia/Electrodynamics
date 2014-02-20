package resonantinduction.electrical.charger;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderItemOverlayTile;
import resonantinduction.electrical.levitator.TileLevitator;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.client.FMLClientHandler;

/**
 * Renderer for electric item charger
 * 
 * @author DarkGuardsman
 */
public class RenderCharger implements ISimpleItemRenderer
{
	public static final RenderCharger INSTANCE = new RenderCharger();

	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "charger.tcn");
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "charger.png");

	public void render(PartCharger part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

		RenderUtility.rotateFaceToSideNoTranslate(part.placementSide);

		RenderUtility.bind(TEXTURE);
		MODEL.renderAll();

		if (part.getStackInSlot(0) != null)
		{
			RenderItemOverlayTile.renderItem(part.world(), part.placementSide, part.getStackInSlot(0), new Vector3(0.09, -0.4, -0.09), 0, 4);
			
			/**
			 * Render item and tool tip
			 */
			if (CompatibilityModule.getMaxEnergyItem(part.getStackInSlot(0)) > 0)
			{
				long energy = CompatibilityModule.getEnergyItem(part.getStackInSlot(0));
				long maxEnergy = CompatibilityModule.getMaxEnergyItem(part.getStackInSlot(0));
				GL11.glTranslatef(0, 0.1F, 0);
				GL11.glRotatef(90, 1, 0, 0);
				RenderUtility.renderText(UnitDisplay.getDisplay(energy, Unit.JOULES, 2, true) + "/" + UnitDisplay.getDisplay(maxEnergy, Unit.JOULES, 2, true), 1, 1);
			}
		}

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		RenderUtility.bind(TEXTURE);
		MODEL.renderAll();
	}
}
