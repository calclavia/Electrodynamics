package resonantinduction.atomic.machine.fulmination;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.Vec3;
import resonant.api.explosion.ExplosionEvent.DoExplosionEvent;
import resonantinduction.atomic.AtomicContent;
import universalelectricity.core.transform.vector.Vector3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Atomic Science Event Handling.
 */
public class FulminationHandler
{
	public static final FulminationHandler INSTANCE = new FulminationHandler();

	public static final List<TileFulmination> list = new ArrayList<TileFulmination>();

	public void register(TileFulmination tileEntity)
	{
		if (!list.contains(tileEntity))
		{
			list.add(tileEntity);
		}
	}

	public void unregister(TileFulmination tileEntity)
	{
		list.remove(tileEntity);
	}

	@SubscribeEvent
	public void BaoZha(DoExplosionEvent evt)
	{
		if (evt.iExplosion != null)
		{
			if (evt.iExplosion.getRadius() > 0 && evt.iExplosion.getEnergy() > 0)
			{
				HashSet<TileFulmination> avaliableGenerators = new HashSet<TileFulmination>();

				for (TileFulmination tileEntity : FulminationHandler.list)
				{
					if (tileEntity != null)
					{
						if (!tileEntity.isInvalid())
						{
							Vector3 tileDiDian = new Vector3(tileEntity);
							tileDiDian.add(0.5f);
							double juLi = tileDiDian.distance(new Vector3(evt.x, evt.y, evt.z));

							if (juLi <= evt.iExplosion.getRadius() && juLi > 0)
							{
								float miDu = evt.world.getBlockDensity(Vec3.createVectorHelper(evt.x, evt.y, evt.z), AtomicContent.blockFulmination().getCollisionBoundingBoxFromPool(evt.world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));

								if (miDu < 1)
								{
									avaliableGenerators.add(tileEntity);
								}
							}
						}
					}
				}

				final float totalEnergy = evt.iExplosion.getEnergy();
				final float maxEnergyPerGenerator = totalEnergy / avaliableGenerators.size();

				for (TileFulmination tileEntity : avaliableGenerators)
				{
					float density = evt.world.getBlockDensity(Vec3.createVectorHelper(evt.x, evt.y, evt.z), AtomicContent.blockFulmination().getCollisionBoundingBoxFromPool(evt.world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
					double juLi = new Vector3(tileEntity).distance(new Vector3(evt.x, evt.y, evt.z));

					long energy = (long) Math.min(maxEnergyPerGenerator, maxEnergyPerGenerator / (juLi / evt.iExplosion.getRadius()));
					energy = (long) Math.max((1 - density) * energy, 0);
					tileEntity.energy().receiveEnergy(energy, true);
				}
			}
		}
	}
}
