package com.calclavia.edx.mechanical


class Watch {
	var oldTime = System.currentTimeMillis()

	def update(): Long = {
		val tmp = oldTime
		oldTime = System.currentTimeMillis()
		oldTime - tmp
	}
}
