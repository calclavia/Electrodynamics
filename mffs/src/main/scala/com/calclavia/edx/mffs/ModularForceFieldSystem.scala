package com.calclavia.edx.mffs

import com.calclavia.edx.mffs.api.fortron.Fortron
import com.calclavia.edx.mffs.content.{Content, Models, Textures}
import com.calclavia.edx.mffs.security.MFFSPermissions
import com.resonant.lib.{WrapFunctions, MovementManager}
import WrapFunctions._
import nova.core.event.GlobalEvents.{BlockChangeEvent, EmptyEvent}
import nova.core.fluid.Fluid
import nova.core.game.Game
import nova.core.loader.{Loadable, NovaMod}

@NovaMod(id = Reference.id, name = Reference.name, version = Reference.version, novaVersion = "0.0.1", dependencies = Array("resonantengine", "nodeAPI"))
object ModularForceFieldSystem extends Loadable {
	//TODO: Remove tempID
	val tempID = ""
	var movementManager: MovementManager = null

	override def preInit() {
		/**
		 * Registration
		 */
		Game.instance.eventManager.blockChange.add((evt: BlockChangeEvent) => EventHandler.onBlockChange(evt))
		//		MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
		//		MinecraftForge.EVENT_BUS.register(Content.remoteController)

		Game.instance.eventManager.serverStarting.add((evt: EmptyEvent) => {
			GraphFrequency.client = new GraphFrequency
			GraphFrequency.server = new GraphFrequency
		})

		Game.instance.fluidManager.register((args: Array[AnyRef]) => new Fluid(Fortron.fortronID))

		Content.preInit()
		Models.preInit()
		Textures.preInit()
	}

	override def init() {
		Content.init()
		Models.init()
		Textures.init()
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

		Content.postInit()
		Models.postInit()
		Textures.postInit()
	}

}