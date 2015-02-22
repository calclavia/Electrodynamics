package mffs.api.card;

import nova.core.util.collection.Pair;
import nova.core.util.transform.Vector3i;
import nova.core.world.World;

public interface ICoordLink {
	public void setLink(World world, Vector3i position);

	public Pair<World, Vector3i> getLink();
}
