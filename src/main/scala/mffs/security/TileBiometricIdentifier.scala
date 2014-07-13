package mffs.security

import java.util
import java.util.{Set => JSet}

import com.mojang.authlib.GameProfile
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.base.TileFrequency
import mffs.item.card.ItemCardFrequency
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.item.ItemStack
import resonant.api.mffs.card.IAccessCard
import resonant.lib.access.java.Permission
import resonant.lib.content.prefab.TRotatable
import universalelectricity.core.transform.vector.Vector3

object TileBiometricIdentifier
{
  val SLOT_COPY = 12
}

class TileBiometricIdentifier extends TileFrequency with TRotatable
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
    if (!isActive || ModularForceFieldSystem.proxy.isOp(profile) && Settings.allowOpOverride)
      return true

    return getCards map (stack => stack.getItem.asInstanceOf[IAccessCard].getAccess(stack)) filter (_ != null) exists (_.hasPermission(profile.getName, permission))
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0)
      return itemStack.getItem.isInstanceOf[ItemCardFrequency]

    return itemStack.getItem.isInstanceOf[IAccessCard]
  }

  override def getCards: Set[ItemStack] = (getInventory().getContainedItems filter (_.getItem.isInstanceOf[IAccessCard])).toSet

  override def getInventoryStackLimit: Int = 1

  override def getBiometricIdentifiers: JSet[TileBiometricIdentifier] =
  {
    val set = new util.HashSet[TileBiometricIdentifier]()
    set.add(this)
    return set
  }

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3, pass: Int): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderBiometricIdentifier.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    RenderBiometricIdentifier.render(this, -0.5, -0.5, -0.5, 0, true, true)
  }
}