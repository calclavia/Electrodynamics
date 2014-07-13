package mffs.security

import java.util
import java.util.{Set => JSet}

import com.mojang.authlib.GameProfile
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.base.TileFrequency
import mffs.item.card.ItemCardFrequency
import mffs.security.card.ItemCardAccess
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.item.ItemStack
import resonant.api.mffs.card.ICardIdentification
import resonant.api.mffs.security.IBiometricIdentifier
import resonant.lib.access.java.Permission
import resonant.lib.content.prefab.TRotatable
import universalelectricity.core.transform.vector.Vector3

object TileBiometricIdentifier
{
  val SLOT_COPY = 12
}

class TileBiometricIdentifier extends TileFrequency with IBiometricIdentifier with TRotatable
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

    return getCards map (stack => stack.getItem.asInstanceOf[ItemCardAccess].getAccess(stack)) filter (_ != null) exists (_.hasPermission(profile.getName, permission))
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0)
      return itemStack.getItem.isInstanceOf[ItemCardFrequency]

    return itemStack.getItem.isInstanceOf[ItemCardAccess]
  }

  override def getCards: Set[ItemStack] = (getInventory().getContainedItems filter (_.getItem.isInstanceOf[ItemCardAccess])).toSet

  override def getInventoryStackLimit: Int = 1

  /**
   * Gets the current card that is being edited.
   **/
  def getManipulatingCard: ItemStack =
  {
    if (this.getStackInSlot(1) != null)
    {
      if (this.getStackInSlot(1).getItem.isInstanceOf[ICardIdentification])
      {
        return this.getStackInSlot(1)
      }
    }
    return null
  }

  override def getBiometricIdentifiers: JSet[IBiometricIdentifier] =
  {
    val set = new util.HashSet[IBiometricIdentifier]()
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
    RenderBiometricIdentifier.render(this, pos.x, pos.y, pos.z, frame, isActive)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    RenderBiometricIdentifier.render(this, -0.5, -0.5, -0.5, 0, true)
  }
}