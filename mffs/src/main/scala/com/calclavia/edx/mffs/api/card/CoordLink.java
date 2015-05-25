package com.calclavia.edx.mffs.api.card;

import nova.core.util.collection.Tuple2;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

public interface CoordLink {
	public void setLink(World world, Vector3i position);

	public Tuple2<World, Vector3i> getLink();
}
