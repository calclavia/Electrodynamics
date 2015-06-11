package com.calclavia.edx.optics.content

import com.calclavia.edx.core.Reference
import nova.core.render.texture.{BlockTexture, EntityTexture}
import nova.scala.modcontent.ContentLoader

/**
 * Textures
 * @author Calclavia
 */
object OpticsTextures extends ContentLoader {

	val machine = new BlockTexture(Reference.domain, "machine")
	val fortron = new BlockTexture(Reference.domain, "fortron")
	val hologram = new BlockTexture(Reference.domain, "hologram")
	val forceField = new BlockTexture(Reference.domain, "forceField")
	val coercionDeriverOn = new BlockTexture(Reference.domain, "coercionDeriver_on")
	val coercionDeriverOff = new BlockTexture(Reference.domain, "coercionDeriver_off")
	val fortronCapacitorOn = new BlockTexture(Reference.domain, "fortronCapacitor_on")
	val fortronCapacitorOff = new BlockTexture(Reference.domain, "fortronCapacitor_off")
	val projectorOn = new BlockTexture(Reference.domain, "electromagneticProjector_on")
	val projectorOff = new BlockTexture(Reference.domain, "electromagneticProjector_off")
	val biometricOn = new BlockTexture(Reference.domain, "biometricIdentifier_on")
	val biometricOff = new BlockTexture(Reference.domain, "biometricIdentifier_off")
	val mobilizerOn = new BlockTexture(Reference.domain, "forceMobilizer_on")
	val mobilizerOff = new BlockTexture(Reference.domain, "forceMobilizer_off")

	val laserEmitter = new BlockTexture(Reference.domain, "laserEmitter")
	val laserReceiver = new BlockTexture(Reference.domain, "laserReceiver")
	val mirror = new BlockTexture(Reference.domain, "mirror")
	val crystal = new BlockTexture(Reference.domain, "crystal")

	/**
	 * Particle FX
	 */
	val laserStartTexture = new EntityTexture(Reference.domain, "laserStart")
	val laserMiddleTexture = new EntityTexture(Reference.domain, "laserMiddle")
	val laserEndTexture = new EntityTexture(Reference.domain, "laserEnd")
	val noiseTexture = new EntityTexture(Reference.domain, "noise")
	val scorchTexture = new EntityTexture(Reference.domain, "scorch")

	override def id: String = Reference.opticsID
}
