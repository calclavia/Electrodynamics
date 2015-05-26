package com.calclavia.edx.api.energy

import com.calclavia.graph.api.Node

/**
 * Applied to any node that will act as a mechanical object
 *
 * @author Calclavia, Darkguardsman
 */
trait TNodeMechanical extends Node[TNodeMechanical] {
	/**
	 * Gets the angular velocity of the mechanical device from a specific side
	 *
	 * @return Angular velocity in meters per second
	 */
	def angularVelocity: Double

	/**
	 * Gets the torque of the mechanical device from a specific side
	 *
	 * @return force
	 */
	def torque: Double

	/**
	 * The mechanical resistance of this node.
	 * Consider the moment of inertia, which equals mass * radius * radius
	 *
	 * Torque = Moment of Intertia * angular velocity
	 *
	 * A higher resistance or moment of inertia means that it is more difficult for this mechanical node to accelerate.
	 *
	 * @return Moment of intertia
	 */
	def inertia = 10D

	/**
	 * Friction is a factor that decelerates the mechanical system based on angular velocity.
	 */
	def friction = 1D

	/**
	 * The radius of rotation
	 */
	def radius(other: TNodeMechanical) = 0.5

	/**
	 * Does the direction flip on this side for rotation
	 *
	 * @param prev - The other mechanical node
	 * @return boolean, true = flipped, false = not
	 */
	def inverseRotation(prev: TNodeMechanical): Boolean = true

	/**
	 * Does this node flip the next node's rotation?
	 * @param next - The next node
	 * @return True to flip the next node
	 */
	def inverseNext(next: TNodeMechanical): Boolean = true

	/**
	 * Applies rotational force and velocity to this node increasing its current rotation value
	 *
	 * @param torque          - force at an angle
	 */
	def accelerate(torque: Double)
}