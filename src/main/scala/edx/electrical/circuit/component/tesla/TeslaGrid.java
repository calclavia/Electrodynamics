/**
 *
 */
package edx.electrical.circuit.component.tesla;

import net.minecraft.tileentity.TileEntity;
import resonantengine.api.mffs.fortron.IServerThread;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author Calclavia
 */
@Deprecated // TODO "Should be replaced with a shared frequency grid" - from Darkguardsman
public class TeslaGrid
{
	private static final TeslaGrid INSTANCE_CLIENT = new TeslaGrid();
	private static final TeslaGrid INSTANCE_SERVER = new TeslaGrid();

	private final Set<ITesla> tileEntities = Collections.newSetFromMap(new WeakHashMap<ITesla, Boolean>());

	public static TeslaGrid instance()
	{
		Thread thr = Thread.currentThread();

		if (thr instanceof IServerThread)
		{
			return INSTANCE_SERVER;
		}

		return INSTANCE_CLIENT;
	}

	public void register(ITesla iTesla)
	{
		Iterator<ITesla> it = this.tileEntities.iterator();

		while (it.hasNext())
		{
			ITesla tesla = it.next();

			if (tesla instanceof TileEntity)
			{
				if (!((TileEntity) tesla).isInvalid())
				{
					continue;
				}
			}

			it.remove();

		}

		this.tileEntities.add(iTesla);
	}

	public void unregister(ITesla iTesla)
	{
		this.tileEntities.remove(iTesla);
	}

	public Set<ITesla> get()
	{
		return this.tileEntities;
	}
}
