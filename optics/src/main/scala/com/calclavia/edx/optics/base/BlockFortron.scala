package com.calclavia.edx.optics.base

import java.util.{Collections, Set => JSet}

import com.calclavia.edx.optics.GraphFrequency
import com.calclavia.edx.optics.api.fortron.FortronFrequency
import com.calclavia.edx.optics.util.{FortronUtility, TransferMode}
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Stateful.UnloadEvent
import nova.core.event.EventBus
import nova.core.fluid.component.{Tank, TankSimple}
import nova.core.fluid.{Fluid, SidedTankProvider}
import nova.core.game.Game
import nova.core.network.Sync
import nova.core.retention.Stored
import nova.core.util.Direction
import nova.scala.ExtendedUpdater

/**
 * A TileEntity that is powered by FortronHelper.
 *
 * @author Calclavia
 */
abstract class BlockFortron extends BlockFrequency with SidedTankProvider with FortronFrequency with ExtendedUpdater {
	var markSendFortron = true

	@Sync(ids = Array(PacketBlock.fortron))
	@Stored
	protected var fortronTank = new TankSimple(Fluid.bucketVolume)

	unloadEvent.add((evt: UnloadEvent) => {
		//Use this to "spread" Fortron out when this block is destroyed.
		if (markSendFortron) {
			FortronUtility.transferFortron(
				this,
				GraphFrequency.instance.get(getFrequency)
					.collect { case f: FortronFrequency with Block => f }
					.filter(_.world() == world())
					.filter(_.transform.position.distance(transform.position) < 100)
					.map(_.asInstanceOf[FortronFrequency]),
				TransferMode.drain,
				Integer.MAX_VALUE
			)
		}
	}, EventBus.PRIORITY_DEFAULT + 1)

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (Game.network.isServer && ticks % 60 == 0) {
			Game.network.sync(PacketBlock.fortron, this)
		}
	}

	override def getFortronTank: Tank = fortronTank

	override def getTank(dir: Direction): JSet[Tank] = Collections.singleton(fortronTank)
}