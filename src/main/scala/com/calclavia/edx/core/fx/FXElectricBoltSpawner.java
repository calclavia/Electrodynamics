package com.calclavia.edx.core.fx;

import nova.core.component.Updater;
import nova.core.entity.Entity;
import nova.core.util.transform.vector.Vector3d;

import java.util.Random;

/**
 * A spawner used to spawn in multiple electrical bolts for a specific duration.
 */
public class FXElectricBoltSpawner extends Entity implements Updater {
	private final float maxAge;
	private Vector3d start;
	private Vector3d end;
	private Random rand;
	private float age;

	public FXElectricBoltSpawner(Vector3d targetVec, long seed, int duration) {
		if (seed == 0) {
			rand = new Random();
		} else {
			rand = new Random(seed);
		}

		end = targetVec;
		maxAge = duration;
	}

	@Override
	public void update(double deltaTime) {
		age += deltaTime;

		if (age > deltaTime) {
			world().removeEntity(this);
		}
	}

	@Override
	public String getID() {
		return "electricBoltSpawner";
	}
}
