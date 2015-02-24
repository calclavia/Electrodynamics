package mffs.util

import mffs.api.fortron.IFortronFrequency
import mffs.api.modules.Module
import mffs.render.FieldColor
import mffs.util.TransferMode._
import mffs.{Content, ModularForceFieldSystem, Settings}
import nova.core.block.Block
import nova.core.game.Game

import scala.collection.mutable

/**
 * A class with useful functions related to Fortron.
 *
 * @author Calclavia
 */
object FortronUtility {
	def transferFortron(source: IFortronFrequency, frequencyTiles: mutable.Set[IFortronFrequency], transferMode: TransferMode, limit: Int) {
		if (frequencyTiles.size > 1 && Settings.allowFortronTeleport) {
			var totalFortron = 0
			var totalCapacity = 0

			for (machine <- frequencyTiles) {
				if (machine != null) {
					totalFortron += machine.getFortron
					totalCapacity += machine.getFortronCapacity
				}
			}
			if (totalFortron > 0 && totalCapacity > 0) {
				transferMode match {
					case TransferMode.`equalize` => {
						for (machine <- frequencyTiles) {
							if (machine != null) {
								val capacityPercentage: Double = machine.getFortronCapacity.asInstanceOf[Double] / totalCapacity.asInstanceOf[Double]
								val amountToSet: Int = (totalFortron * capacityPercentage).asInstanceOf[Int]
								doTransferFortron(source, machine, amountToSet - machine.getFortron, limit)
							}
						}
					}
					case TransferMode.`distribute` => {
						val amountToSet: Int = totalFortron / frequencyTiles.size
						for (machine <- frequencyTiles) {
							if (machine != null) {
								doTransferFortron(source, machine, amountToSet - machine.getFortron, limit)
							}
						}
					}
					case TransferMode.drain => {
						frequencyTiles.remove(source)

						for (machine <- frequencyTiles) {
							if (machine != null) {
								val capacityPercentage: Double = machine.getFortronCapacity.asInstanceOf[Double] / totalCapacity.asInstanceOf[Double]
								val amountToSet: Int = (totalFortron * capacityPercentage).asInstanceOf[Int]

								if (amountToSet - machine.getFortron > 0) {
									doTransferFortron(source, machine, amountToSet - machine.getFortron, limit)
								}
							}
						}
					}
					case TransferMode.`fill` => {
						if (source.getFortron < source.getFortronCapacity) {
							frequencyTiles.remove(source)
							val requiredFortron: Int = source.getFortronCapacity - source.getFortron

							for (machine <- frequencyTiles) {
								if (machine != null) {
									val amountToConsume: Int = Math.min(requiredFortron, machine.getFortron)
									val amountToSet: Int = -machine.getFortron - amountToConsume
									if (amountToConsume > 0) {
										doTransferFortron(source, machine, amountToSet - machine.getFortron, limit)
									}
								}
							}
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
	def doTransferFortron(transferer: IFortronFrequency, receiver: IFortronFrequency, joules: Int, limit: Int) {
		if (transferer != null && receiver != null) {
			val block = transferer.asInstanceOf[Block]
			val world = block.world()

			val isCamo = {
				if (transferer.isInstanceOf[IModuleProvider]) {
					transferer.asInstanceOf[IModuleProvider].getModuleCount(Content.moduleCamouflage.asInstanceOf[Module]) > 0
				}
				else {
					false
				}
			}

			if (joules > 0) {
				val transferEnergy = Math.min(joules, limit)
				var toBeInjected: Int = receiver.removeFortron(transferer.addFortron(transferEnergy, false), false)
				toBeInjected = transferer.addFortron(receiver.removeFortron(toBeInjected, true), true)
				if (Game.instance.networkManager.isClient && toBeInjected > 0 && !isCamo) {
					ModularForceFieldSystem.proxy.renderBeam(world, new Vector3d(block) + 0.5, new Vector3d(receiver.asInstanceOf[TileEntity]) + 0.5, FieldColor.blue, 20)
				}
			}
			else {
				val transferEnergy = Math.min(Math.abs(joules), limit)
				var toBeEjected: Int = transferer.removeFortron(receiver.addFortron(transferEnergy, false), false)
				toBeEjected = receiver.addFortron(transferer.removeFortron(toBeEjected, true), true)
				if (Game.instance.networkManager.isClient && toBeEjected > 0 && !isCamo) {
					ModularForceFieldSystem.proxy.renderBeam(world, new Vector3d(receiver.asInstanceOf[TileEntity]) + 0.5, new Vector3d(block) + 0.5, FieldColor.blue, 20)
				}
			}
		}
	}
}
