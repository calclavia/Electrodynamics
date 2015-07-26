package com.calclavia.edx.core.extension

import org.jgrapht.DirectedGraph

import scala.collection.convert.wrapAll._

/**
 * An implicit extension class for JGraphT
 * @author Calclavia
 */
object GraphExtension {

	implicit class DirectedGraphWrapper[V, E](underlying: DirectedGraph[V, E]) {

		/**
		 * @return The set of nodes going into the vertex
		 */
		def sourcesOf(vertex: V): Set[V] =
			underlying
				.incomingEdgesOf(vertex)
				.map(underlying.getEdgeSource)
				.toSet

		/**
		 * @return The set of nodes this vertex is pointing towards
		 */
		def targetsOf(vertex: V): Set[V] =
			underlying
				.outgoingEdgesOf(vertex)
				.map(underlying.getEdgeTarget)
				.toSet

		/**
		 * @param vertex The vertex
		 * @return The set of mutual connections (connected both ways)
		 */
		def connectionsOf(vertex: V): Set[V] = underlying.sourcesOf(vertex) & underlying.targetsOf(vertex)
	}

}
