package com.calclavia.edx.optics.content

import com.calclavia.edx.core.Reference
import com.resonant.core.prefab.modcontent.ContentLoader
import nova.core.render.model.TechneModel

/**
 * Textures
 * @author Calclavia
 */
object OpticsModels extends ContentLoader {

	val fortronCapacitor = new TechneModel(Reference.domain, "fortronCapacitor")
	val projector = new TechneModel(Reference.domain, "electromagneticProjector")
	val biometric = new TechneModel(Reference.domain, "biometricIdentifier")
	val mobilizer = new TechneModel(Reference.domain, "forceMobilizer")
	val deriver = new TechneModel(Reference.domain, "coercionDeriver")

	val laserReceiverModel = new TechneModel(Reference.domain, "laserReceiver")
	val laserEmitterModel = new TechneModel(Reference.domain, "laserEmitter")
	val mirrorModel = new TechneModel(Reference.domain, "mirror")

	override def id: String = Reference.id
}
