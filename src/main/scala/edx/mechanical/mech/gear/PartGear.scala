package edx.mechanical.mech.gear

import java.util

import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.FaceMicroClass
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import edx.core.prefab.part.CuboidShapes
import edx.mechanical.MechanicalContent
import edx.mechanical.mech.PartMechanical
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.tile.node.INode
import resonantengine.lib.prefab.tile.multiblock.reference.IMultiBlockStructure
import resonantengine.lib.utility.WrenchUtility

/**
 * We assume all the force acting on the gear is 90 degrees.
 *
 * @author Calclavia
 */
class PartGear extends PartMechanical with IMultiBlockStructure[PartGear]
{
  val multiBlock = new GearMultiBlockHandler(this)
  var multiBlockRadius = 1

  //Constructor
  mechanicalNode = new NodeGear(this)

  mechanicalNode.onVelocityChanged = () =>
  {
    if (getMultiBlock.isPrimary)
      if (world != null) sendPacket(1)

    if (mechanicalNode.angularVelocity == 0)
      if (world != null) sendPacket(2)
  }

  mechanicalNode.onGridReconstruct = () =>
  {
    if (world != null)
    {
      sendPacket(1)
      sendPacket(2)
    }
  }

  //TODO: Can we not have update ticks here?
  override def update()
  {
    super.update()
    getMultiBlock.update()
  }

  override def activate(player: EntityPlayer, hit: MovingObjectPosition, itemStack: ItemStack): Boolean =
  {
    if (itemStack != null && itemStack.getItem.isInstanceOf[ItemHandCrank])
    {
      mechanicalNode.accelerate((if (player.isSneaking) 1 else -1) * 1000)
      world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.prefix + "gearCrank", 0.5f, 0.9f + world.rand.nextFloat * 0.2f)
      player.addExhaustion(0.02f)
      return true
    }

    if (WrenchUtility.isWrench(itemStack))
    {
      getMultiBlock.toggleConstruct()
      return true
    }

    return super.activate(player, hit, itemStack)
  }

  override def preRemove()
  {
    super.preRemove()
    getMultiBlock.deconstruct()
  }

  /**
   * Is this gear block the one in the center-edge of the multiblock that can interact with other gears?
   * @return Returning true implies that this gear is able to connect to other ones side-by-side.
   */
  def isCenterMultiBlock: Boolean =
  {
    if (!getMultiBlock.isConstructed)
    {
      return true
    }
    val primaryPos = getMultiBlock.getPrimary.getPosition
    return (primaryPos.xi == x && placementSide.offsetX == 0) || (primaryPos.yi == y && placementSide.offsetY == 0) || (primaryPos.zi == z && placementSide.offsetZ == 0)
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    if (pass == 0)
      RenderGear.renderDynamic(this, pos.x, pos.y, pos.z, tier)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    getMultiBlock.load(nbt)
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    getMultiBlock.save(nbt)
  }

  override def getMultiBlock: GearMultiBlockHandler = multiBlock

  override def getMultiBlockVectors: java.util.List[resonantengine.lib.transform.vector.Vector3] = new resonantengine.lib.transform.vector.Vector3().getAround(this.world, placementSide, 1)

  def getWorld: World =
  {
    return world
  }

  def onMultiBlockChanged()
  {
    if (world != null)
    {
      tile.notifyPartChange(this)

      if (!world.isRemote)
      {
        mechanicalNode.reconstruct()
        sendDescUpdate()
      }
    }
  }

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N =
  {
    if (nodeType.isAssignableFrom(mechanicalNode.getClass) && from == placementSide)
      return getMultiBlock.get.mechanicalNode.asInstanceOf[N]

    return null.asInstanceOf[N]
  }

  /** Multipart Bounds */
  def getOcclusionBoxes: java.lang.Iterable[Cuboid6] =
  {
    val list: java.util.List[Cuboid6] = new util.ArrayList[Cuboid6];
    for (v <- CuboidShapes.panel(this.placementSide.ordinal))
    {
      list.add(v)
    }
    return list
  }

  def getBounds: Cuboid6 =
  {
    return FaceMicroClass.aBounds(0x10 | this.placementSide.ordinal)
  }

  @SideOnly(Side.CLIENT)
  override def getRenderBounds: Cuboid6 = Cuboid6.full.copy.expand(multiBlockRadius)

  override def toString = "[PartGear]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "

  def getSlotMask: Int =
  {
    return 1 << this.placementSide.ordinal
  }

  protected def getItem: ItemStack =
  {
    return new ItemStack(MechanicalContent.itemGear, 1, tier)
  }
}