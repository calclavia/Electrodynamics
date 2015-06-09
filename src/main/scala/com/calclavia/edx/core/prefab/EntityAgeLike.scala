package com.calclavia.edx.core.prefab

import nova.core.component.Updater
import nova.core.entity.Entity

/**
 * Entity that has a limited age
 * @author Calclavia
 */
trait EntityAgeLike extends Entity with Updater {

	var time = 0d

	override def update(deltaTime: Double) {
		//TODO: Wait for Scala 12
		time += deltaTime

		//super.update(deltaTime)
		if (time >= maxAge) {
			world.removeEntity(this)
		}
	}

	/**
	 * @return The maximum age of the entity in seconds
	 */
	def maxAge: Double
}
