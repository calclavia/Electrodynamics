package com.calclavia.edx.optics.api;

import nova.core.event.CancelableEvent;
import nova.core.event.EventBus;
import nova.core.item.Item;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

/**
 * @author Calclavia
 */
public class MFFSEvent {

	public static final MFFSEvent instance = new MFFSEvent();

	public EventBus<EventStabilize> stabilizeEventBus = new EventBus<>();

	public EventBus<EventForceMobilize> checkMobilize = new EventBus<>();
	public EventBus<EventForceMobilize> preMobilize = new EventBus<>();
	public EventBus<EventForceMobilize> postMobilize = new EventBus<>();

	public static class EventStabilize {
		public final Item item;
		public final World world;
		public final Vector3i pos;

		public EventStabilize(Item item, World world, Vector3i pos) {
			this.item = item;
			this.world = world;
			this.pos = pos;
		}
	}

	@CancelableEvent.Cancelable
	public static class EventForceMobilize extends CancelableEvent {
		public final World worldBefore;
		public final Vector3i before;
		public final World worldAfter;
		public final Vector3i after;

		public EventForceMobilize(World worldBefore, Vector3i before, World worldAfter, Vector3i after) {
			this.worldBefore = worldBefore;
			this.before = before;
			this.worldAfter = worldAfter;
			this.after = after;
		}
	}

}