package resonantinduction.mechanical.mech.gearshaft

import java.util.{Collection, HashSet, Set}

import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.multipart.PartMap
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.mechanical.MechanicalContent
import resonantinduction.mechanical.mech.PartMechanical

/**
 * We assume all the force acting on the gear is 90 degrees.
 *
 * @author Calclavia
 */
object PartGearShaft
{
  var sides: Array[IndexedCuboid6] = new Array[IndexedCuboid6](7)

  //Bound boxes for each side Sides
  sides(0) = new IndexedCuboid6(0, new Cuboid6(0.36, 0.000, 0.36, 0.64, 0.36, 0.64))
  sides(1) = new IndexedCuboid6(1, new Cuboid6(0.36, 0.64, 0.36, 0.64, 1.000, 0.64))
  sides(2) = new IndexedCuboid6(2, new Cuboid6(0.36, 0.36, 0.000, 0.64, 0.64, 0.36))
  sides(3) = new IndexedCuboid6(3, new Cuboid6(0.36, 0.36, 0.64, 0.64, 0.64, 1.000))
  sides(4) = new IndexedCuboid6(4, new Cuboid6(0.000, 0.36, 0.36, 0.36, 0.64, 0.64))
  sides(5) = new IndexedCuboid6(5, new Cuboid6(0.64, 0.36, 0.36, 1.000, 0.64, 0.64))
  sides(6) = new IndexedCuboid6(6, new Cuboid6(0.36, 0.36, 0.36, 0.64, 0.64, 0.64))
}

class PartGearShaft extends PartMechanical
{
  mechanicalNode = new NodeGearShaft(this)

  override def preparePlacement(side: Int, itemDamage: Int)
  {
    val dir = ForgeDirection.getOrientation(side ^ 1)
    this.placementSide = ForgeDirection.getOrientation(if (!(dir.ordinal % 2 == 0)) dir.ordinal - 1 else dir.ordinal)
    tier = itemDamage
  }

  protected def getItem: ItemStack = new ItemStack(MechanicalContent.itemGearShaft, 1, tier)

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    if (pass == 0)
      RenderGearShaft.renderDynamic(this, pos.x, pos.y, pos.z, frame)
  }

  /**
   * Multipart Bounds
   */
  def getSlotMask: Int = PartMap.CENTER.mask

  def getOcclusionBoxes: java.lang.Iterable[Cuboid6] = getCollisionBoxes

  override def getCollisionBoxes: java.lang.Iterable[Cuboid6] =
  {
    val collisionBoxes: Set[Cuboid6] = new HashSet[Cuboid6]
    collisionBoxes.addAll(getSubParts.asInstanceOf[Collection[_ <: Cuboid6]])
    return collisionBoxes
  }

  override def getSubParts: java.lang.Iterable[IndexedCuboid6] =
  {
    val subParts: Set[IndexedCuboid6] = new HashSet[IndexedCuboid6]
    val currentSides: Array[IndexedCuboid6] = PartGearShaft.sides
    if (tile != null)
    {
      for (side <- ForgeDirection.VALID_DIRECTIONS)
      {
        if (side == placementSide || side == placementSide.getOpposite)
        {
          subParts.add(currentSides(side.ordinal))
        }
      }
    }
    subParts.add(currentSides(6))
    return subParts
  }

  def getBounds: Cuboid6 = new Cuboid6(0.375, 0.375, 0.375, 0.625, 0.625, 0.625)
}