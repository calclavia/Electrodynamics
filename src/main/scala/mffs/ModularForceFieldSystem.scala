package mffs

import com.resonant.lib.misc.MovementManager
import mffs.content.Content
import mffs.security.MFFSPermissions
import nova.core.event.EventListener
import nova.core.event.EventManager.{BlockChangeEvent, EmptyEvent}
import nova.core.game.Game
import nova.core.loader.{Loadable, NovaMod}

@NovaMod(id = Reference.id, name = Reference.name, version = Reference.version, novaVersion = "0.0.1", dependencies = Array("resonantengine"))
object ModularForceFieldSystem extends Loadable {

	val tempID = ""
	var movementManager: MovementManager = null

	override def preInit() {
		/**
		 * Registration
		 */
		Game.instance.eventManager.blockChange.add(new EventListener[BlockChangeEvent] {
			override def onEvent(event: BlockChangeEvent)
			{
				EventHandler.onBlockChange(event)
			}
		})
		//		MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
		//		MinecraftForge.EVENT_BUS.register(Settings)
		//		MinecraftForge.EVENT_BUS.register(Content.remoteController)

		Game.instance.eventManager.serverStarting.add(new EventListener[EmptyEvent] {
			override def onEvent(event: EmptyEvent) {
				GraphFrequency.client = new GraphFrequency
				GraphFrequency.server = new GraphFrequency
			}
		})

		Content.preInit()

	}

	override def init() {
		Content.init()
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

		//Inititate MFFS Permissions
		MFFSPermissions

		Content.postInit()
	}

}