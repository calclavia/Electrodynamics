package edx.electrical.battery

import java.util.List

import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import resonant.api.items.IEnergyItem
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.science.UnitDisplay
import resonant.lib.wrapper.CollectionWrapper._

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

class ItemBlockBattery(block: Block) extends ItemBlock(block) with IEnergyItem
{
  this.setMaxStackSize(1)
  this.setMaxDamage(100)
  this.setNoRepair

  override def addInformation(itemStack: ItemStack, entityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    list.add(LanguageUtility.getLocal("tooltip.tier") + ": " + (ItemBlockBattery.getTier(itemStack) + 1))
    var color: String = ""
    val joules: Double = this.getEnergy(itemStack)
    if (joules <= this.getEnergyCapacity(itemStack) / 3)
    {
      color = "\u00a74"
    }
    else if (joules > this.getEnergyCapacity(itemStack) * 2 / 3)
    {
      color = "\u00a72"
    }
    else
    {
      color = "\u00a76"
    }
    itemStack.getItemDamageForDisplay
    list.add(LanguageUtility.getLocal("tooltip.battery.energy").replace("%0", color).replace("%1", EnumColor.GREY.toString).replace("%v0", new UnitDisplay(UnitDisplay.Unit.JOULES, joules).symbol.toString).replace("%v1", new UnitDisplay(UnitDisplay.Unit.JOULES, this.getEnergyCapacity(itemStack), true).symbol.toString))
  }

  /**
   * Makes sure the item is uncharged when it is crafted and not charged. Change this if you do
   * not want this to happen!
   */
  override def onCreated(itemStack: ItemStack, par2World: World, par3EntityPlayer: EntityPlayer)
  {
    this.setEnergy(itemStack, 0)
  }

  def recharge(itemStack: ItemStack, energy: Double, doReceive: Boolean): Double =
  {
    val rejectedElectricity: Double = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0)
    val energyToReceive: Double = Math.min(energy - rejectedElectricity, getTransferRate(itemStack))
    if (doReceive)
    {
      this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive)
    }
    return energyToReceive
  }

  def discharge(itemStack: ItemStack, energy: Double, doTransfer: Boolean): Double =
  {
    val energyToExtract: Double = Math.min(Math.min(this.getEnergy(itemStack), energy), getTransferRate(itemStack))
    if (doTransfer)
    {
      setEnergy(itemStack, this.getEnergy(itemStack) - energyToExtract)
    }
    return energyToExtract
  }

  def setEnergy(itemStack: ItemStack, joules: Double): ItemStack =
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    val energy: Double = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0)
    itemStack.getTagCompound.setDouble("energy", energy)
    return itemStack
  }

  def getTransferRate(itemStack: ItemStack): Double =
  {
    return this.getEnergyCapacity(itemStack) / 100
  }

  def getEnergyCapacity(theItem: ItemStack): Double =
  {
    return TileBattery.getEnergyForTier(ItemBlockBattery.getTier(theItem))
  }

  /** Gets the energy stored in the item. Energy is stored using item NBT */
  def getEnergy(itemStack: ItemStack): Double =
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    val energyStored = itemStack.getTagCompound.getDouble("energy")
    return energyStored
  }

  def getTransfer(itemStack: ItemStack): Double =
  {
    return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack)
  }

  override def getDisplayDamage(stack: ItemStack): Int =
  {
    return (100 - (getEnergy(stack) / getEnergyCapacity(stack)) * 100).toInt
  }

  @SuppressWarnings(Array("unchecked"))
  override def getSubItems(par1: Item, par2CreativeTabs: CreativeTabs, list: List[_])
  {
    for (tier <- 0 to TileBattery.maxTier)
    {
      //TODO: Make traits for this
      list.add(setEnergy(ItemBlockBattery.setTier(new ItemStack(this), tier), 0))
      list.add(setEnergy(ItemBlockBattery.setTier(new ItemStack(this), tier), TileBattery.getEnergyForTier(tier)))
    }
  }
}