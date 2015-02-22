package mffs

import mffs.api.Blacklist
import mffs.security.MFFSPermissions
import nova.core.loader.{Loadable, NovaMod}

@NovaMod(id = Reference.id, name = Reference.name, version = Reference.version, dependencies = Array("resonantengine"))
object ModularForceFieldSystem extends Loadable {

	override def preInit() {
		/**
		 * Registration
		 */
		//		MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
		//		MinecraftForge.EVENT_BUS.register(Settings)
		//		MinecraftForge.EVENT_BUS.register(Content.remoteController)

		Content.preInit()
	}

	override def load() {
		Content.init()
	}

	override def postInit() {
		/**
		 * Add to black lists
		 */
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

		//Inititate MFFS Permissions
		MFFSPermissions

		Content.postInit()
	}

}