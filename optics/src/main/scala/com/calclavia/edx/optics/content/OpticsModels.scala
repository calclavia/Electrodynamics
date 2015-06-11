package com.calclavia.edx.optics.content

import com.calclavia.edx.core.Reference
import nova.core.render.model.TechneModel
import nova.scala.modcontent.ContentLoader

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

	val laserReceiver = new TechneModel(Reference.domain, "laserReceiver")
	val laserEmitter = new TechneModel(Reference.domain, "laserEmitter")
	val mirror = new TechneModel(Reference.domain, "mirror")
	val crystal = new TechneModel(Reference.domain, "crystal")

	override def id: String = Reference.id
}
