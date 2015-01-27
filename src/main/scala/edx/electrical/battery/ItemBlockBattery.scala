package edx.electrical.battery

import java.util.List

import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import resonantengine.lib.utility.LanguageUtility
import resonantengine.lib.wrapper.CollectionWrapper._
import resonantengine.prefab.item.TEnergyItem

object ItemBlockBattery
{
  def setTier(itemStack: ItemStack, tier: Int): ItemStack =
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    itemStack.getTagCompound.setByte("tier", tier.toByte)
    return itemStack
  }

  def getTier(itemStack: ItemStack): Int =
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    return itemStack.getTagCompound.getByte("tier")
  }
}

class ItemBlockBattery(block: Block) extends ItemBlock(block) with TEnergyItem
{
  this.setMaxStackSize(1)
  this.setMaxDamage(100)
  this.setNoRepair

  override def addInformation(itemStack: ItemStack, entityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    list.add(LanguageUtility.getLocal("tooltip.tier") + ": " + (ItemBlockBattery.getTier(itemStack) + 1))
    super.addInformation(itemStack, entityPlayer, list, par4)
  }

  /**
   * Makes sure the item is uncharged when it is crafted and not charged. Change this if you do
   * not want this to happen!
   */
  override def onCreated(itemStack: ItemStack, par2World: World, par3EntityPlayer: EntityPlayer)
  {
    this.setEnergy(itemStack, 0)
  }

  override def recharge(itemStack: ItemStack, energy: Double, doReceive: Boolean): Double =
  {
    val rejectedElectricity: Double = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0)
    val energyToReceive: Double = Math.min(energy - rejectedElectricity, getTransferRate(itemStack))
    if (doReceive)
    {
      this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive)
    }
    return energyToReceive
  }

  override def discharge(itemStack: ItemStack, energy: Double, doTransfer: Boolean): Double =
  {
    val energyToExtract: Double = Math.min(Math.min(this.getEnergy(itemStack), energy), getTransferRate(itemStack))
    if (doTransfer)
    {
      setEnergy(itemStack, this.getEnergy(itemStack) - energyToExtract)
    }
    return energyToExtract
  }

  override def getDisplayDamage(stack: ItemStack): Int =
  {
    return (100 - (getEnergy(stack) / getEnergyCapacity(stack)) * 100).toInt
  }

  override def getSubItems(par1: Item, par2CreativeTabs: CreativeTabs, list: List[_])
  {
    for (tier <- 0 to TileBattery.maxTier)
    {
      list.add(setEnergy(ItemBlockBattery.setTier(new ItemStack(this), tier), 0))
      list.add(setEnergy(ItemBlockBattery.setTier(new ItemStack(this), tier), TileBattery.getEnergyForTier(tier)))
    }
  }

  override def setEnergy(itemStack: ItemStack, joules: Double): ItemStack =
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    val energy: Double = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0)
    itemStack.getTagCompound.setDouble("energy", energy)
    return itemStack
  }

  def getEnergyCapacity(theItem: ItemStack): Double =
  {
    return TileBattery.getEnergyForTier(ItemBlockBattery.getTier(theItem))
  }
}