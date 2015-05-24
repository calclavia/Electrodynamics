package mffs.api.modules;

import com.resonant.core.structure.Structure;
import mffs.api.machine.Projector;
import nova.core.render.model.Model;

public interface StructureProvider extends FortronCost {

	Structure getStructure();

	/**
	 * Called to render an object in front of the projection.
	 */
	void render(Projector projector, Model model);
}
