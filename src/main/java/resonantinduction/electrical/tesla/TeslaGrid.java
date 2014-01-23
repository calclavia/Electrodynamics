/**
 * 
 */
package resonantinduction.electrical.tesla;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mffs.api.fortron.IServerThread;
import net.minecraft.server.ServerListenThread;
import net.minecraft.server.ThreadMinecraftServer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Calclavia
 * 
 */
public class TeslaGrid
{
	private static final TeslaGrid INSTANCE_CLIENT = new TeslaGrid();
	private static final TeslaGrid INSTANCE_SERVER = new TeslaGrid();

	private final Set<ITesla> tileEntities = new HashSet<ITesla>();

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

	public static TeslaGrid instance()
	{
		Thread thr = Thread.currentThread();

		if ((thr instanceof ThreadMinecraftServer) || (thr instanceof ServerListenThread) || (thr instanceof IServerThread))
		{
			return INSTANCE_SERVER;
		}

		return INSTANCE_CLIENT;
	}
}
