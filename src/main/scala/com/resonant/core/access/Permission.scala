package com.resonant.core.access

import java.util.List

import nova.core.util.collection.TreeNode

import scala.collection.JavaConversions._

/**
 * @author Calclavia
 */
class Permission(val id: String) extends TreeNode[Permission] {

	def addChild(perm: String): Permission = {
		return super.addChild(new Permission(perm))
	}

	def find(id: String): Permission = {
		import scala.collection.JavaConversions._
		for (child <- children) {
			if (child.id == id) {
				return child
			}
		}
		return null
	}

	override def equals(o: Any): Boolean = {
		return o.isInstanceOf[Permission] && (o.toString == toString)
	}

	override def toString: String = {
		val list: List[Permission] = getHierarchy
		val builder: StringBuilder = new StringBuilder
		for (perm <- list) {
			builder.append(perm.id)
			builder.append(".")
		}
		builder.append(id)
		return builder.toString
	}
}