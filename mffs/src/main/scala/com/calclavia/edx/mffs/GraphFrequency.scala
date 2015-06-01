package com.calclavia.edx.mffs

import java.util.{List => JList}

import com.calclavia.edx.mffs.api.Frequency
import com.calclavia.edx.mffs.api.fortron.IServerThread

import scala.collection.convert.wrapAll._

object GraphFrequency {
	var client = new GraphFrequency
	var server = new GraphFrequency

	def instance: GraphFrequency = {
		val thr: Thread = Thread.currentThread
		//TODO: Check if this is correct
		if ((thr.getName == "Server thread") || thr.isInstanceOf[IServerThread]) {
			return server
		}
		return client
	}
}

class GraphFrequency {

	private var nodes = Set.empty[Frequency]
	private var frequencyMap = Map.empty[Int, Set[Frequency]].withDefaultValue(Set.empty)

	def add(node: Frequency) {
		nodes += node
		build()
	}

	def build() {
		frequencyMap = Map.empty.withDefaultValue(Set.empty)

		getNodes.map(n => (n.getFrequency, n)).foreach(kv =>
			frequencyMap += (kv._1 -> (frequencyMap(kv._1) + kv._2))
		)
	}

	def getNodes: JList[Frequency] = nodes.toList

	def remove(node: Frequency) {
		nodes -= node
		build()
	}

	def get(frequency: Int) = frequencyMap(frequency)
}