package resonantinduction.atomic.machine.fulmination

import java.util.{ArrayList, List}

/**
 * Atomic Science Event Handling.
 */
object FulminationHandler
{
    final val INSTANCE: FulminationHandler = new FulminationHandler
    final val list: List[TileFulmination] = new ArrayList[TileFulmination]
}

class FulminationHandler
{
    def register(tileEntity: TileFulmination)
    {
        if (!FulminationHandler.list.contains(tileEntity))
        {
            FulminationHandler.list.add(tileEntity)
        }
    }

    def unregister(tileEntity: TileFulmination)
    {
        FulminationHandler.list.remove(tileEntity)
    }

  /**
    //@SubscribeEvent def BaoZha(evt: ExplosionEvent.DoExplosionEvent)
    {
        if (evt.iExplosion != null)
        {
            if (evt.iExplosion.getRadius > 0 && evt.iExplosion.getEnergy > 0)
            {
                val avaliableGenerators: HashSet[TileFulmination] = new HashSet[TileFulmination]
                import scala.collection.JavaConversions._
                for (tileEntity <- FulminationHandler.list)
                {
                    if (tileEntity != null)
                    {
                        if (!tileEntity.isInvalid)
                        {
                            val tileDiDian: Vector3 = tileEntity.toVector3
                            tileDiDian.add(0.5f)
                            val juLi: Double = tileDiDian.distance(new Vector3(evt.x, evt.y, evt.z))
                            if (juLi <= evt.iExplosion.getRadius && juLi > 0)
                            {
                                val miDu: Float = evt.world.getBlockDensity(Vec3.createVectorHelper(evt.x, evt.y, evt.z), AtomicContent.blockFulmination.getCollisionBoundingBoxFromPool(evt.world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord))
                                if (miDu < 1)
                                {
                                    avaliableGenerators.add(tileEntity)
                                }
                            }
                        }
                    }
                }
                val totalEnergy: Float = evt.iExplosion.getEnergy
                val maxEnergyPerGenerator: Float = totalEnergy / avaliableGenerators.size
                import scala.collection.JavaConversions._
                for (tileEntity <- avaliableGenerators)
                {
                    val density: Float = evt.world.getBlockDensity(Vec3.createVectorHelper(evt.x, evt.y, evt.z), AtomicContent.blockFulmination.getCollisionBoundingBoxFromPool(evt.world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord))
                    val juLi: Double = tileEntity.toVector3.distance(new Vector3(evt.x, evt.y, evt.z))
                    var energy: Long = Math.min(maxEnergyPerGenerator, maxEnergyPerGenerator / (juLi / evt.iExplosion.getRadius)).asInstanceOf[Long]
                    energy = Math.max((1 - density) * energy, 0).asInstanceOf[Long]
                    tileEntity.energy.receiveEnergy(energy, true)
                }
            }
        }
    } */
}