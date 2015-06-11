package com.resonant.core.access

import nova.core.entity.component.Player
import nova.core.retention.Store

class AccessUser(@Store var username: String) extends AbstractAccess {

	def this(player: Player) {
		this(player.getDisplayName)
	}

	override def hasPermission(username: String, permission: Permission): Boolean = hasPermission(permission)

	def hasPermission(permission: Permission): Boolean = permissions.contains(permission)
}