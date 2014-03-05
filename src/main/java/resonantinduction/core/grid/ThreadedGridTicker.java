package resonantinduction.core.grid;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import universalelectricity.api.net.IUpdate;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A ticker to update all grids. This is multithreaded.
 * 
 * @author Calclavia
 */
public class ThreadedGridTicker extends Thread
{
	public static final ThreadedGridTicker INSTANCE = new ThreadedGridTicker();

	/** For updaters to be ticked. */
	private final Set<IUpdate> updaters = Collections.newSetFromMap(new WeakHashMap<IUpdate, Boolean>());

	/** For queuing Forge events to be invoked the next tick. */
	private final Queue<Event> queuedEvents = new ConcurrentLinkedQueue<Event>();

	private ThreadedGridTicker()
	{
		setName("Universal Electricity");
		setPriority(MIN_PRIORITY);
	}

	public static void addNetwork(IUpdate updater)
	{
		synchronized (INSTANCE.updaters)
		{
			INSTANCE.updaters.add(updater);
		}
	}

	public static synchronized void queueEvent(Event event)
	{
		synchronized (INSTANCE.queuedEvents)
		{
			INSTANCE.queuedEvents.add(event);
		}
	}

	@Override
	public void run()
	{
		try
		{
			long last = System.currentTimeMillis();

			while (true)
			{
				long current = System.currentTimeMillis();
				long delta = current - last;

				/** Tick all updaters. */
				synchronized (updaters)
				{
					Set<IUpdate> removeUpdaters = Collections.newSetFromMap(new WeakHashMap<IUpdate, Boolean>());

					Iterator<IUpdate> updaterIt = new HashSet<IUpdate>(updaters).iterator();

					try
					{
						while (updaterIt.hasNext())
						{
							IUpdate updater = updaterIt.next();

							if (updater.canUpdate())
							{
								updater.update();
							}

							if (!updater.continueUpdate())
							{
								removeUpdaters.add(updater);
							}
						}

						updaters.removeAll(removeUpdaters);
					}
					catch (Exception e)
					{
						System.out.println("Universal Electricity Threaded Ticker: Failed while tcking updater. This is a bug! Clearing all tickers for self repair.");
						updaters.clear();
						e.printStackTrace();
					}
				}

				/** Perform all queued events */
				synchronized (queuedEvents)
				{
					while (!queuedEvents.isEmpty())
					{
						MinecraftForge.EVENT_BUS.post(queuedEvents.poll());
					}
				}

				Thread.sleep(50L);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
