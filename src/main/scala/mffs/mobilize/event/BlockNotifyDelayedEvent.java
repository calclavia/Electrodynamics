package mffs.mobilize.event;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.transform.vector.Vector3;

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
public class BlockNotifyDelayedEvent extends DelayedEvent
{
	private World world;
	private Vector3 position;

	public BlockNotifyDelayedEvent(IDelayedEventHandler handler, int ticks, World world, Vector3 position)
	{
		super(handler, ticks);
		this.world = world;
		this.position = position;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			this.world.notifyBlocksOfNeighborChange(this.position.xi(), this.position.yi(), this.position.zi(), this.position.getBlockID(this.world));

			TileEntity newTile = this.position.getTileEntity(this.world);

			if (newTile != null)
			{
				if (Loader.isModLoaded("BuildCraft|Factory"))
				{
					/**
					 * Special quarry compatibility code.
					 */
					try
					{
						Class clazz = Class.forName("buildcraft.factory.TileQuarry");

						if (clazz == newTile.getClass())
						{
							ReflectionHelper.setPrivateValue(clazz, newTile, true, "isAlive");
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}
