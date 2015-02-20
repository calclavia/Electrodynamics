package mffs

import java.util.UUID

import mffs.api.Blacklist
import mffs.security.MFFSPermissions

@NovaMod(id = Reference.id, name = Reference.name, version = Reference.version, dependencies = Array("ResonantEngine"))
object ModularForceFieldSystem extends Loadable {
	/**
	 * General constants
	 */
	val damageFieldShock = new DamageSource("fieldShock").setDamageBypassesArmor()
	val fakeProfile = new GameProfile(UUID.randomUUID, "mffs")

	override def preInit() {
		/**
		 * Registration
		 */
		Modstats.instance.getReporter.registerMod(this)
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
		MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
		MinecraftForge.EVENT_BUS.register(Settings)

		ConfigHandler.sync(Settings, Settings.config)

		loadables.applyModule(proxy)
		loadables.applyModule(packetHandler)
		loadables.applyModule(Content)

		Settings.config.load

		loadables.preInit()

		MinecraftForge.EVENT_BUS.register(Content.remoteController)

		/**
		 * Fluid Instantiation
		 */
		FortronUtility.fluidFortron.setGaseous(true)
		FluidRegistry.registerFluid(FortronUtility.fluidFortron)

		Content.preInit()
	}

	override def load(evt: FMLInitializationEvent) {
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