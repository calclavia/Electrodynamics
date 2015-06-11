package com.resonant.core.access

import java.util.{Set => JSet}

import nova.core.retention.Data

import scala.collection.JavaConversions
import scala.collection.convert.wrapAll._

/**
 * An access group is a set of users.
 */
class AccessGroup extends AbstractAccess {
	var users = Set.empty[AccessUser]

	override def save(data: Data) {
		super.save(data)
		users = data.get[JSet[AccessUser]]("users").toSet
	}

	override def load(data: Data) {
		super.load(data)
		data.put("users", JavaConversions.setAsJavaSet(users))
	}

	override def hasPermission(username: String, permission: Permission): Boolean = {
		return users.exists(_.username.equals(username)) && (permissions.contains(permission) || users.exists(_.hasPermission(permission)))
	}
}