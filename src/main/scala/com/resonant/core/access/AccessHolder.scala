package com.resonant.core.access

import java.util.{Set => JSet}

import nova.core.retention.{Data, Storable}

import scala.collection.JavaConversions
import scala.collection.convert.wrapAll._

/**
 * An access holder that holds a set of groups, with each group containing users.
 * @author Calclavia
 */

class AccessHolder extends Storable {
	var groups = Set.empty[AccessGroup]

	override def save(data: Data) {
		super.save(data)
		groups = data.get[JSet[AccessGroup]]("groups").toSet
	}

	override def load(data: Data) {
		super.load(data)
		data.put("groups", JavaConversions.setAsJavaSet(groups))
	}

	def hasPermission(username: String, permission: Permission): Boolean = groups.exists(_.hasPermission(username, permission))
}






