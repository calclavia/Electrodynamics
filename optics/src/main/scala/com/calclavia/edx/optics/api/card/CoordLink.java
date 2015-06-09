package com.calclavia.edx.optics.api.card;

import nova.core.util.collection.Tuple2;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import nova.core.world.World;

public interface CoordLink {
	public void setLink(World world, Vector3D position);

	public Tuple2<World, Vector3D> getLink();
}
