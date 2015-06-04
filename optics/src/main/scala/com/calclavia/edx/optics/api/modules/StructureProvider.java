package com.calclavia.edx.optics.api.modules;

import com.calclavia.edx.optics.api.machine.Projector;
import com.resonant.core.structure.Structure;
import nova.core.render.model.Model;

public interface StructureProvider extends FortronCost {

	Structure getStructure();

	/**
	 * Called to render an object in front of the projection.
	 */
	void render(Projector projector, Model model);
}
