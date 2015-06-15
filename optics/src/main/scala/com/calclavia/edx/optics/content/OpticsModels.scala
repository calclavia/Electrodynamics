package com.calclavia.edx.optics.content

import com.calclavia.edx.core.Reference
import nova.core.render.model.TechneModelProvider
import nova.scala.modcontent.ContentLoader

/**
 * Textures
 * @author Calclavia
 */
object OpticsModels extends ContentLoader {

	val fortronCapacitor = new TechneModelProvider(Reference.domain, "fortronCapacitor")
	val projector = new TechneModelProvider(Reference.domain, "electromagneticProjector")
	val biometric = new TechneModelProvider(Reference.domain, "biometricIdentifier")
	val mobilizer = new TechneModelProvider(Reference.domain, "forceMobilizer")
	val deriver = new TechneModelProvider(Reference.domain, "coercionDeriver")

	val laserReceiver = new TechneModelProvider(Reference.domain, "laserReceiver")
	val laserEmitter = new TechneModelProvider(Reference.domain, "laserEmitter")
	val mirror = new TechneModelProvider(Reference.domain, "mirror")
	val crystal = new TechneModelProvider(Reference.domain, "crystal")

	override def id: String = Reference.id
}
