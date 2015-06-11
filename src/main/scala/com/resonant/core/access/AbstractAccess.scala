package com.resonant.core.access

import java.util.{Set => JSet}

import nova.core.retention.{Data, Storable}

import scala.collection.convert.wrapAll._

/**
 * The abstract access class.
 * @author Calclavia
 */
abstract class AbstractAccess extends Storable {

	var permissions = Set.empty[Permission]

	override def save(data: Data) {
		super.save(data)
		data.put("permissions", permissions.map(_.toString).asInstanceOf[JSet[String]])
	}

	override def load(data: Data) {
		super.load(data)
		permissions = data.get("permissions").asInstanceOf[JSet[String]].map(i => Permissions.find(i)).toSet
	}

	def hasPermission(username: String, permission: Permission): Boolean
}
