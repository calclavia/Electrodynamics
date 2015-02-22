package mffs.security

import java.util
import java.util.{Set => JSet}

import mffs.base.TileFrequency
import mffs.item.card.ItemCardFrequency
import mffs.{ModularForceFieldSystem, Settings}

object BlockBiometric
{
  val SLOT_COPY = 12
}

class BlockBiometric extends TileFrequency with TRotatable
{
  /**
   * Rendering
   */
  var lastFlicker = 0L

  /**
   * 2 slots: Card copying
   * 9 x 4 slots: Access Cards
   * Under access cards we have a permission selector
   */
  override def getSizeInventory = 1 + 45

  override def update()
  {
    super.update()
    animation += 0.1f
  }

  override def hasPermission(profile: GameProfile, permission: Permission): Boolean =
  {
	  if (!isActive || ModularForceFieldSystem.proxy.isOp(profile) && Settings.allowOpOverride) {
		  return true
	  }

	  return getConnectionCards map (stack => stack.getItem.asInstanceOf[IAccessCard].getAccess(stack)) filter (_ != null) exists (_.hasPermission(profile.getName, permission))
  }

	override def getConnectionCards: Set[Item] = (getInventory().getContainedItems filter (_ != null) filter (_.getItem.isInstanceOf[IAccessCard])).toSet

	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean =
  {
	  if (slotID == 0) {
		  return Item.getItem.isInstanceOf[ItemCardFrequency]
	  }

	  return Item.getItem.isInstanceOf[IAccessCard]
  }

  override def getInventoryStackLimit: Int = 1

	override def getBiometricIdentifiers: JSet[BlockBiometric] =
  {
	  val set = new util.HashSet[BlockBiometric]()
    set.add(this)
    return set
  }

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3d, pass: Int): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3d, frame: Float, pass: Int)
  {
    RenderBiometricIdentifier.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(Item: Item)
  {
    RenderBiometricIdentifier.render(this, -0.5, -0.5, -0.5, 0, true, true)
  }
}