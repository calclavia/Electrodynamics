package mffs.event;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

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
			this.world.notifyBlocksOfNeighborChange(this.position.intX(), this.position.intY(), this.position.intZ(), this.position.getBlockID(this.world));

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
