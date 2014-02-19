package resonantinduction.electrical.charger;

import net.minecraft.tileentity.TileEntity;
import resonantinduction.core.render.RenderItemOverlayTile;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;

/**
 * Renderer for electric item charger
 * 
 * @author DarkGuardsman
 */
public class RenderCharger extends RenderItemOverlayTile
{

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
