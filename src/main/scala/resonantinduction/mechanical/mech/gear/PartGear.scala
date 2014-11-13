package resonantinduction.mechanical.mech.gear

import java.util

import codechicken.lib.vec.{Cuboid6, Vector3}
import codechicken.microblock.FaceMicroClass
import codechicken.multipart.ControlKeyModifer
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.INode
import resonant.lib.multiblock.reference.IMultiBlockStructure
import resonant.lib.transform.vector.VectorWorld
import resonant.lib.utility.WrenchUtility
import resonantinduction.core.Reference
import resonantinduction.core.prefab.part.CuboidShapes
import resonantinduction.mechanical.MechanicalContent
import resonantinduction.mechanical.mech.PartMechanical

/**
 * We assume all the force acting on the gear is 90 degrees.
 *
 * @author Calclavia
 */
class PartGear extends PartMechanical with IMultiBlockStructure[PartGear]
{
  var isClockwiseCrank: Boolean = true
  var manualCrankTime = 0D
  var multiBlockRadius: Int = 1
  /** Multiblock */
  val multiBlock = new GearMultiBlockHandler(this)

  //Constructor
  mechanicalNode = new NodeGear(this)
  mechanicalNode.onVelocityChanged = () =>
  {
    if (getMultiBlock.isPrimary)
      markVelocityUpdate = true
  }

  mechanicalNode.onGridReconstruct = () => if (world != null && !world.isRemote) sendPacket(2)

  //TODO: Can we not have update ticks here?
  override def update()
  {
    super.update()

    if (!this.world.isRemote)
    {
      if (manualCrankTime > 0)
      {
        mechanicalNode.rotate(if (isClockwiseCrank) 50 else -50)
        manualCrankTime -= 0.1
      }
    }

    getMultiBlock.update()
  }

  override def activate(player: EntityPlayer, hit: MovingObjectPosition, itemStack: ItemStack): Boolean =
  {
    if (!world.isRemote)
      println(mechanicalNode.connections)
    if (itemStack != null && itemStack.getItem.isInstanceOf[ItemHandCrank])
    {
      if (!world.isRemote && ControlKeyModifer.isControlDown(player))
      {
        getMultiBlock.get.mechanicalNode.torque = -getMultiBlock.get.mechanicalNode.torque
        getMultiBlock.get.mechanicalNode.angularVelocity = -getMultiBlock.get.mechanicalNode.angularVelocity
        return true
      }

      isClockwiseCrank = player.isSneaking

      getMultiBlock.get.manualCrankTime = 2
      world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, Reference.prefix + "gearCrank", 0.5f, 0.9f + world.rand.nextFloat * 0.2f)
      player.addExhaustion(0.01f)
      return true
    }

    if (WrenchUtility.isWrench(itemStack))
    {
      getMultiBlock.toggleConstruct()
      return true
    }

    return super.activate(player, hit, itemStack)
  }

  override def preRemove
  {
    super.preRemove
    getMultiBlock.deconstruct
  }

  /** Is this gear block the one in the center-edge of the multiblock that can interact with other
    * gears?
    *
    * @return */
  def isCenterMultiBlock: Boolean =
  {
    if (!getMultiBlock.isConstructed)
    {
      return true
    }
    val primaryPos: VectorWorld = getMultiBlock.getPrimary.getPosition
    if (primaryPos.xi == x && placementSide.offsetX == 0)
    {
      return true
    }
    if (primaryPos.yi == y && placementSide.offsetY == 0)
    {
      return true
    }
    if (primaryPos.zi == z && placementSide.offsetZ == 0)
    {
      return true
    }
    return false
  }

  protected def getItem: ItemStack =
  {
    return new ItemStack(MechanicalContent.itemGear, 1, tier)
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

  override def getMultiBlockVectors: java.util.List[resonant.lib.transform.vector.Vector3] = new resonant.lib.transform.vector.Vector3().getAround(this.world, placementSide, 1)

  def getWorld: World =
  {
    return world
  }

  def onMultiBlockChanged
  {
    if (world != null)
    {
      tile.notifyPartChange(this)
      if (!world.isRemote)
      {
        sendDescUpdate
      }
    }
  }

  override def getMultiBlock: GearMultiBlockHandler = multiBlock

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N =
  {
    if (nodeType.isAssignableFrom(mechanicalNode.getClass))
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

  def getSlotMask: Int =
  {
    return 1 << this.placementSide.ordinal
  }

  def getBounds: Cuboid6 =
  {
    return FaceMicroClass.aBounds(0x10 | this.placementSide.ordinal)
  }

  @SideOnly(Side.CLIENT)
  override def getRenderBounds: Cuboid6 = Cuboid6.full.copy.expand(multiBlockRadius)

  override def toString = "[PartGear]" + x + "x " + y + "y " + z + "z " + getSlotMask + "s "
}