package resonantinduction.core

import codechicken.multipart.MultiPartRegistry.IPartFactory
import codechicken.multipart.{MultipartGenerator, MultiPartRegistry, TMultiPart}

import scala.collection.mutable

/**
 * @author Calclavia
 */
object ResonantPartFactory extends IPartFactory
{
  val prefix = Reference.prefix
  private val partMap = mutable.Map.empty[String, Class[_ <: TMultiPart]]

  def register(part: Class[_ <: TMultiPart])
  {
    partMap += (prefix + part.getSimpleName -> part)
  }

  def create[C <: TMultiPart](part: Class[C]): C = MultiPartRegistry.createPart((partMap map (_.swap)).get(part).get, false).asInstanceOf[C]

  def init()
  {
    MultiPartRegistry.registerParts(this, partMap.keys.toArray)

    MultipartGenerator.registerTrait("universalelectricity.api.core.grid.INodeProvider", "resonantinduction.core.prefab.TNodeProvider")
  }

  def createPart(name: String, client: Boolean): TMultiPart = partMap(name).newInstance()
}
