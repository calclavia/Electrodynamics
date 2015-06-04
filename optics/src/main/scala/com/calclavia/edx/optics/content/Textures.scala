package com.calclavia.edx.optics.content

import com.calclavia.edx.core.Reference
import com.resonant.core.prefab.modcontent.ContentLoader
import nova.core.render.texture.BlockTexture

/**
 * Textures
 * @author Calclavia
 */
object Textures extends ContentLoader {

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

	/**
	 * Particle FX
	 */
	val laserStartTexture = new BlockTexture(Reference.domain, "laserStart")
	val laserMiddleTexture = new BlockTexture(Reference.domain, "laserMiddle")
	val laserEndTexture = new BlockTexture(Reference.domain, "laserEnd")
	val laserNoiseTexture = new BlockTexture(Reference.domain, "noise")
	val scortchTexture = new BlockTexture(Reference.domain, "scorch")

	override def id: String = Reference.opticsID
}
