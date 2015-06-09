package com.calclavia.edx.optics.api;

import nova.core.event.CancelableEvent;
import nova.core.event.EventBus;
import nova.core.item.Item;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
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
		public final Vector3D pos;

		public EventStabilize(Item item, World world, Vector3D pos) {
			this.item = item;
			this.world = world;
			this.pos = pos;
		}
	}

	@CancelableEvent.Cancelable
	public static class EventForceMobilize extends CancelableEvent {
		public final World worldBefore;
		public final Vector3D before;
		public final World worldAfter;
		public final Vector3D after;

		public EventForceMobilize(World worldBefore, Vector3D before, World worldAfter, Vector3D after) {
			this.worldBefore = worldBefore;
			this.before = before;
			this.worldAfter = worldAfter;
			this.after = after;
		}
	}

}