package resonantinduction.electrical.charger;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderItemOverlayTile;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.client.FMLClientHandler;

/**
 * Renderer for electric item charger
 * 
 * @author DarkGuardsman
 */
public class RenderCharger extends RenderItemOverlayTile
{
	public static final RenderCharger INSTANCE = new RenderCharger();

	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "charger.tcn");
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "charger.png");
/*
	public void render(PartCharger part, double x, double y, double z)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		RenderUtility.rotateFaceBlockToSide(part.placementSide);
		RenderUtility.rotateBlockBasedOnDirection(part.getFacing());

		RenderUtility.bind(TEXTURE);
		MODEL.renderAll();
		GL11.glPopMatrix();
	}*/

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		if (tile instanceof TileCharger)
		{
			Vector3 d = new Vector3();
			switch (((TileCharger) tile).getDirection())
			{
				case NORTH:
					d.translate(0, 0, .58);
					break;
				case SOUTH:
					d.translate(0, 0, -.58);
					break;
				case WEST:
					d.translate(.58, 0, 0);
					break;
				case EAST:
					d.translate(-.58, 0, 0);
					break;
			}
			this.renderItemSingleSide(tile, x + d.x, y + d.y, z + d.z, ((TileCharger) tile).getStackInSlot(0), ((TileCharger) tile).getDirection(), "IDLE");
			if (CompatibilityModule.getMaxEnergyItem(((TileCharger) tile).getStackInSlot(0)) > 0)
			{
				long energy = CompatibilityModule.getEnergyItem(((TileCharger) tile).getStackInSlot(0));
				long maxEnergy = CompatibilityModule.getMaxEnergyItem(((TileCharger) tile).getStackInSlot(0));
				RenderUtility.renderText(UnitDisplay.getDisplay(energy, Unit.JOULES, 2, true) + "/" + UnitDisplay.getDisplay(maxEnergy, Unit.JOULES, 2, true), ((TileCharger) tile).getDirection(), 0.02f, x + d.x, y + d.y + 0.4, z + d.z);
			}
		}
	}
}
