package com.calclavia.edx.optics.util

import com.calclavia.edx.optics.Settings
import com.calclavia.edx.optics.api.fortron.FortronFrequency
import com.calclavia.edx.optics.beam.fx.EntityMagneticBeam
import com.calclavia.edx.optics.component.CrystalHandler
import com.calclavia.edx.optics.content.OpticsContent
import com.calclavia.edx.optics.fx.FieldColor
import nova.core.block.Block
import nova.core.network.NetworkTarget.Side
import nova.scala.wrapper.VectorWrapper._

/**
 * A class with useful functions related to Fortron.
 *
 * @author Calclavia
 */
//TODO: Make this OOP FortronTransferProtocol
object FortronUtility {
	def transferFortron(source: FortronFrequency, frequencyBlocks: Set[FortronFrequency], transferMode: TransferMode, limit: Int) {
		if (frequencyBlocks.size > 1 && Settings.allowFortronTeleport) {
			val totalFortron = frequencyBlocks.foldLeft(0)(_ + _.getFortron)
			val totalCapacity = frequencyBlocks.foldLeft(0)(_ + _.getFortronCapacity)

			if (totalFortron > 0 && totalCapacity > 0) {
				transferMode match {
					case TransferMode.equalize =>
						frequencyBlocks.foreach(machine => {
							val capacityPercentage = machine.getFortronCapacity / totalCapacity
							val amountToSet = (totalFortron * capacityPercentage).toInt
							doTransferFortron(source, machine, amountToSet - machine.getFortron, limit)
						})
					case TransferMode.distribute => {
						val amountToSet: Int = totalFortron / frequencyBlocks.size
						frequencyBlocks.foreach(machine => doTransferFortron(source, machine, amountToSet - machine.getFortron, limit))
					}
					case TransferMode.drain => {
						val frequencyWithoutSource = frequencyBlocks - source

						frequencyWithoutSource.foreach(machine => {
							val capacityPercentage = machine.getFortronCapacity.toDouble / totalCapacity.toDouble
							val amountToSet = (totalFortron * capacityPercentage).toInt

							if (amountToSet - machine.getFortron > 0) {
								doTransferFortron(source, machine, amountToSet - machine.getFortron, limit)
							}
						})
					}
					case TransferMode.fill => {
						if (source.getFortron < source.getFortronCapacity) {
							val frequencyWithoutSource = frequencyBlocks - source

							val requiredFortron = source.getFortronCapacity - source.getFortron

							frequencyWithoutSource.foreach(machine => {
								val amountToConsume = Math.min(requiredFortron, machine.getFortron)
								val amountToSet = -machine.getFortron - amountToConsume
								if (amountToConsume > 0) {
									doTransferFortron(source, machine, amountToSet - machine.getFortron, limit)
								}
							})
						}
					}
				}
			}
		}
	}

	/**
	 * Tries to transfer Fortron to a specific machine from this capacitor. Renders an animation on
	 * the client side.
	 *
	 * @param receiver : The machine to be transfered to.
	 * @param joules   : The amount of energy to be transfered.
	 */
	def doTransferFortron(transferer: FortronFrequency, receiver: FortronFrequency, joules: Int, limit: Int) {
		if (transferer != null && receiver != null) {
			val block = transferer.asInstanceOf[Block]
			val world = block.world()

			val isCamo = {
				if (transferer.isInstanceOf[CrystalHandler]) {
					transferer.asInstanceOf[CrystalHandler].getModuleCount(OpticsContent.moduleCamouflage) > 0
				}
				else {
					false
				}
			}

			if (joules > 0) {
				val transferEnergy = Math.min(joules, limit)
				var toBeInjected: Int = receiver.removeFortron(transferer.addFortron(transferEnergy, false), false)
				toBeInjected = transferer.addFortron(receiver.removeFortron(toBeInjected, true), true)
				if (Side.get().isClient && toBeInjected > 0 && !isCamo) {
					val particle = world.addClientEntity(new EntityMagneticBeam(FieldColor.blue, 20))
					particle.setPosition(block.position + 0.5)
					particle.setTarget(receiver.asInstanceOf[Block].position + 0.5)
				}
			}
			else {
				val transferEnergy = Math.min(Math.abs(joules), limit)
				var toBeEjected: Int = transferer.removeFortron(receiver.addFortron(transferEnergy, false), false)
				toBeEjected = receiver.addFortron(transferer.removeFortron(toBeEjected, true), true)
				if (Side.get().isClient && toBeEjected > 0 && !isCamo) {
					val particle = world.addClientEntity(new EntityMagneticBeam(FieldColor.blue, 20))
					particle.setTarget(block.position + 0.5)
					particle.setPosition(receiver.asInstanceOf[Block].position + 0.5)
				}
			}
		}
	}
}
