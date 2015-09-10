package com.calclavia.edx.mechanical

import scala.concurrent.duration._

class Watch(val updateInterval: Option[Duration]) {

	var oldTime: Long = System.currentTimeMillis

	def timeDiff = (System.currentTimeMillis() - oldTime).millisecond
	def shouldUpdate = updateInterval.exists(_ > timeDiff )

	def update(): Long = {
		val tmp = oldTime
		oldTime = System.currentTimeMillis()
		oldTime - tmp
	}
}
