package mffs.api;

import nova.core.event.EventBus;
import nova.core.item.Item;
import nova.core.util.transform.Vector3i;
import nova.core.world.World;

/**
 * @author Calclavia
 */
public class MFFSEvent {

	public static final MFFSEvent instance = new MFFSEvent();

	public EventBus<EventStabilize> stabilizeEventBus = new EventBus<>();

	public class EventStabilize {
		public final Item item;
		public final World world;
		public final Vector3i pos;

		public EventStabilize(Item item, World world, Vector3i pos) {
			this.item = item;
			this.world = world;
			this.pos = pos;
		}
	}
}
