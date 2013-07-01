package mffs.event;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.api.ForceManipulator.ISpecialForceManipulation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * Removes the TileEntity
 * 
 * @author Calclavia
 * 
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
				if (newTile instanceof ISpecialForceManipulation)
				{
					((ISpecialForceManipulation) newTile).postMove();
				}

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

				if (Loader.isModLoaded("ThermalExpansion"))
				{
					/**
					 * Special conduit compatibility code
					 */
					try
					{
						Class clazz = Class.forName("thermalexpansion.block.conduit.TileConduitRoot");

						if (clazz.isInstance(newTile))
						{
							clazz.getMethod("checkConnections").invoke(newTile);
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
