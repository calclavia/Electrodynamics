package mffs.api.card;

import nova.core.util.collection.Pair;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

public interface CoordLink {
	public void setLink(World world, Vector3i position);

	public Pair<World, Vector3i> getLink();
}
