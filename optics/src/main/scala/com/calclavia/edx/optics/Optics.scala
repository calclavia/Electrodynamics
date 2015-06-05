package com.calclavia.edx.optics

import com.calclavia.edx.core.Reference
import com.calclavia.edx.electric.circuit.component.laser.WaveGrid.WaveGridPacket
import com.calclavia.edx.optics.api.fortron.Fortron
import com.calclavia.edx.optics.content.{OpticsContent, Models, OpticsTextures}
import com.calclavia.edx.optics.security.MFFSPermissions
import com.resonant.lib.MovementManager
import com.resonant.lib.WrapFunctions._
import nova.core.event.Event
import nova.core.event.GlobalEvents.BlockChangeEvent
import nova.core.fluid.Fluid
import nova.core.game.Game
import nova.core.loader.{Loadable, NovaMod}

@NovaMod(id = Reference.opticsID, name = Reference.name + ": Optics", version = Reference.version, novaVersion = "0.0.1", dependencies = Array("resonantengine", "nodeAPI"))
object Optics extends Loadable {
	//TODO: Remove tempID
	val tempID = ""
	var movementManager: MovementManager = null

	override def preInit() {
		//Register WaveGrid packets
		Game.network.register(new WaveGridPacket)

		//Hook block change event
		Game.events.blockChange.add((evt: BlockChangeEvent) => EventHandler.onBlockChange(evt))

		//Init frequency grid1
		Game.events.serverStarting.add((evt: Event) => {
			GraphFrequency.client = new GraphFrequency
			GraphFrequency.server = new GraphFrequency
		})

		Game.fluids.register((args: Array[AnyRef]) => new Fluid(Fortron.fortronID))

		OpticsContent.preInit()
		Models.preInit()
		OpticsTextures.preInit()
	}

	override def init() {
		OpticsContent.init()
		Models.init()
		OpticsTextures.init()
	}

	override def postInit() {
		/**
		 * Add to black lists

		Blacklist.stabilizationBlacklist.add(Blocks.water)
		Blacklist.stabilizationBlacklist.add(Blocks.flowing_water)
		Blacklist.stabilizationBlacklist.add(Blocks.lava)
		Blacklist.stabilizationBlacklist.add(Blocks.flowing_lava)

		Blacklist.disintegrationBlacklist.add(Blocks.water)
		Blacklist.disintegrationBlacklist.add(Blocks.flowing_water)
		Blacklist.disintegrationBlacklist.add(Blocks.lava)
		Blacklist.disintegrationBlacklist.add(Blocks.flowing_lava)

		Blacklist.mobilizerBlacklist.add(Blocks.bedrock)
		Blacklist.mobilizerBlacklist.add(Content.forceField)

		try {
			val clazz = Class.forName("ic2.api.block.ExplosionWhitelist")
			clazz.getMethod("addWhitelistedBlock", classOf[Block]).invoke(null, Content.forceField)
		}
		catch {
			case _: Throwable => Reference.logger.info("IC2 Explosion white list API not found. Ignoring...")
		}
		 */

		//Initiate MFFS Permissions
		MFFSPermissions

		OpticsContent.postInit()
		Models.postInit()
		OpticsTextures.postInit()
	}

}