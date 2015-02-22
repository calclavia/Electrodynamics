package mffs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

/**
 * The event called when stabilizing a field into solid blocks. Canceling
 * @author Calclavia
 */
@Cancelable
public class EventStabilize extends WorldEvent {
	public final Item Item;
	public final int x, y, z;

	public EventStabilize(World world, int x, int y, int z, Item Item) {
		super(world);
		this.x = x;
		this.y = y;
		this.z = z;
		this.Item = Item;
	}
}
