package com.calclavia.edx.mffs.api.modules;

import com.calclavia.edx.mffs.api.machine.Projector;
import com.resonant.core.structure.Structure;
import nova.core.render.model.Model;

public interface StructureProvider extends FortronCost {

	Structure getStructure();

	/**
	 * Called to render an object in front of the projection.
	 */
	void render(Projector projector, Model model);
}
