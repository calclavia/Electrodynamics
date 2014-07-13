package mffs.base

import java.util.{Set => JSet}

import com.mojang.authlib.GameProfile
import mffs.Reference
import mffs.item.card.ItemCardFrequency
import mffs.security.{MFFSPermissions, TileBiometricIdentifier}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import resonant.api.blocks.IBlockFrequency
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.fortron.FrequencyGridRegistry
import resonant.lib.access.java.Permission
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._

abstract class TileFrequency extends TileMFFSInventory with IBlockFrequency
{
  val frequencySlot = 0

  override def validate()
  {
    FrequencyGridRegistry.instance.add(this)
    super.validate()
  }

  override def invalidate()
  {
    FrequencyGridRegistry.instance.remove(this)
    super.invalidate()
  }

  override def getFrequency: Int =
  {
    val frequencyCard = getFrequencyCard

    if (frequencyCard != null)
    {
      return frequencyCard.getItem.asInstanceOf[ItemCardFrequency].getFrequency(frequencyCard)
    }

    return 0
  }

  override def setFrequency(frequency: Int)
  {

  }

  def getFrequencyCard: ItemStack =
  {
    val stack = getStackInSlot(frequencySlot)

    if (stack != null && stack.getItem.isInstanceOf[ItemCardFrequency])
      return stack

    return null
  }

  /**
   * Permissions
   */
  override protected def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!hasPermission(player.getGameProfile, MFFSPermissions.configure))
    {
      player.addChatMessage(new ChatComponentText("[" + Reference.name + "]" + " Access denied!"))
      return false
    }

    return super.configure(player, side, hit)
  }

  def hasPermission(profile: GameProfile, permission: Permission): Boolean = !isActive || getBiometricIdentifiers.forall(_.hasPermission(profile, permission))

  def hasPermission(profile: GameProfile, permissions: Permission*): Boolean = permissions.forall(hasPermission(profile, _))

  /**
   * Gets the first linked biometric identifier, based on the card slots and frequency.
   */
  def getBiometricIdentifier: TileBiometricIdentifier = if (getBiometricIdentifiers.size > 0) getBiometricIdentifiers.head else null

  def getBiometricIdentifiers: JSet[TileBiometricIdentifier] =
  {
    val cardLinks = (getCards.view
            .filter(itemStack => itemStack != null && itemStack.getItem.isInstanceOf[ICoordLink])
            .map(itemStack => itemStack.getItem.asInstanceOf[ICoordLink].getLink(itemStack))
            .filter(_ != null)
            .map(_.getTileEntity)
            .filter(_.isInstanceOf[TileBiometricIdentifier])
            .map(_.asInstanceOf[TileBiometricIdentifier]))
            .force.toSet

    val frequencyLinks = FrequencyGridRegistry.instance.getNodes(classOf[TileBiometricIdentifier], getFrequency).toSet

    return frequencyLinks ++ cardLinks
  }
}