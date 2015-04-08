package mffs.base

import java.util.{Collections, Set => JSet}

import com.resonant.core.prefab.block.Updater
import mffs.GraphFrequency
import mffs.api.fortron.FortronFrequency
import mffs.util.{FortronUtility, TransferMode}
import nova.core.block.Block
import nova.core.fluid.{Fluid, SidedTankProvider, Tank, TankSimple}
import nova.core.game.Game
import nova.core.network.Sync
import nova.core.retention.Stored
import nova.core.util.Direction

/**
 * A TileEntity that is powered by FortronHelper.
 *
 * @author Calclavia
 */
abstract class BlockFortron extends BlockFrequency with SidedTankProvider with FortronFrequency with Updater {
	var markSendFortron = true

	@Sync(ids = Array(PacketBlock.fortron))
	@Stored
	protected var fortronTank = new TankSimple(Fluid.bucketVolume)

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (Game.instance.networkManager.isServer && ticks % 60 == 0) {
			Game.instance.networkManager.sync(PacketBlock.fortron, this)
		}
	}

	override def unload() {
		//Use this to "spread" Fortron out when this block is destroyed.
		if (markSendFortron) {
			FortronUtility.transferFortron(
				this,
				GraphFrequency.instance.get(getFrequency)
					.collect { case f: FortronFrequency with Block => f }
					.filter(_.world() == world())
					.filter(_.position().distance(position()) < 100)
					.map(_.asInstanceOf[FortronFrequency]),
				TransferMode.drain,
				Integer.MAX_VALUE
			)
		}

		super.unload()
	}

	override def getFortronTank: Tank = fortronTank

	override def getTank(dir: Direction): JSet[Tank] = Collections.singleton(fortronTank)
}