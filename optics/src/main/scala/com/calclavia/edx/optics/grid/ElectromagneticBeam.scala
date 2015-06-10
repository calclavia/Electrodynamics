package com.calclavia.edx.optics.grid

import com.calclavia.edx.optics.beam.fx.{EntityBlockParticle, EntityLaserBeam, EntityScorch}
import nova.core.component.misc.Damageable
import nova.core.util.RayTracer.{RayTraceBlockResult, RayTraceEntityResult, RayTraceResult}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._

/**
 * @author Calclavia
 */
class ElectromagneticBeam extends Beam {

	override def onHitEntity(rayTrace: RayTraceEntityResult) {
		if (power > OpticGrid.minBurnEnergy) {
			val fireTime = (10 * (power / OpticGrid.maxPower)).toInt

			if (fireTime > 0) {
				//hit.entity.setFire (fireTime)
				rayTrace.entity.getOp(classOf[Damageable]).ifPresent(consumer(d => d.damage(20 * (power / OpticGrid.maxPower))))
			}
		}
	}

	override def onHitBlock(rayTrace: RayTraceBlockResult) {
		val energyUsed = timeElapsed * power

		/**
		 * Mine the block
		 */
		val hitBlock = rayTrace.block
		val energyRequiredToMineBlock = hitBlock.getHardness * OpticGrid.maxEnergyToMine

		//TODO: Render breaking effect
		//world.destroyBlockInWorldPartially(Block.blockRegistry.getIDForObject(hitBlock), hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, (accumulatedEnergy / energyRequiredToMineBlock * 10).toInt)

		if (energyUsed >= energyRequiredToMineBlock) {
			//TODO: Fix BWItem casting to ItemFactory
			//We can disintegrate the block!
			/*
			val event = new DropEvent(hitBlock)
			hitBlock.dropEvent.publish(event)
			event.drops.foreach(drop => hitBlock.world.addEntity(hitBlock.position + 0.5, drop))
			world.removeBlock(hitBlock.position)*/
		}

		//TODO: Handle furnace smelting
		//TODO: Burnable
		/**
		 * Catch Fire
		if (energyOnBlock > minBurnEnergy && hitBlock.getMaterial.getCanBurn) {
			if (hitBlock.isInstanceOf[BlockTNT]) {
				hitBlock.asInstanceOf[BlockTNT].func_150114_a(world, hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, 1, null)
			}
			world.setBlock(hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, Blocks.fire)
		}*/
	}

	/**
	 * Renders the beam
	 * @param hit Null if nothing is hit.
	 */
	override def render(hit: RayTraceResult, hasImpact: Boolean) {
		if (hit != null) {
			val renderPos = source.origin + renderOffset
			if (!renderPos.equals(hit.hit)) {
				world.addClientEntity(new EntityLaserBeam(renderPos, hit.hit, color, power))

				if (hit.isInstanceOf[RayTraceBlockResult] && hasImpact) {
					val scorch = new EntityScorch(hit.side.ordinal())
					world.addClientEntity(scorch)
					scorch.setPosition(hit.hit - source.dir * 0.02)
					val blockParticle = new EntityBlockParticle(hit.asInstanceOf[RayTraceBlockResult].block)
					world.addClientEntity(blockParticle)
					blockParticle.setPosition(hit.hit - source.dir * 0.02)
				}
			}
		}
	}
}