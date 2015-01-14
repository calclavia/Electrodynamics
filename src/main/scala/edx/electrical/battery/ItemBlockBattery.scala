package edx.electrical.battery

import java.util.List

import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import resonant.api.items.IEnergyItem
import resonant.lib.mod.compat.energy.Compatibility
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
    list.add(LanguageUtility.getLocal("tooltip.battery.energy").replace("%0", color).replace("%1", EnumColor.GREY.toString).replace("%v0", new UnitDisplay(UnitDisplay.Unit.JOULES, joules).toString).replace("%v1", new UnitDisplay(UnitDisplay.Unit.JOULES, this.getEnergyCapacity(itemStack), true).toString))
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

  def setEnergy(itemStack: ItemStack, joules: Double)
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    val electricityStored: Double = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0)
    itemStack.getTagCompound.setDouble("electricity", electricityStored)
  }

  def getTransferRate(itemStack: ItemStack): Double =
  {
    return this.getEnergyCapacity(itemStack) / 100
  }

  def discharge(itemStack: ItemStack, energy: Double, doTransfer: Boolean): Double =
  {
    val energyToExtract: Double = Math.min(Math.min(this.getEnergy(itemStack), energy), getTransferRate(itemStack))
    if (doTransfer)
    {
      this.setEnergy(itemStack, this.getEnergy(itemStack) - energyToExtract)
    }
    return energyToExtract
  }

  def getVoltage(itemStack: ItemStack): Double =
  {
    return 240
  }

  def getTransfer(itemStack: ItemStack): Double =
  {
    return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack)
  }

  override def getDisplayDamage(stack: ItemStack): Int =
  {
    return (100 - (this.getEnergy(stack).asInstanceOf[Double] / getEnergyCapacity(stack).asInstanceOf[Double]) * 100).asInstanceOf[Int]
  }

  /** Gets the energy stored in the item. Energy is stored using item NBT */
  def getEnergy(itemStack: ItemStack): Double =
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    val energyStored: Long = itemStack.getTagCompound.getLong("electricity")
    return energyStored
  }

  def getEnergyCapacity(theItem: ItemStack): Double =
  {
    return TileBattery.getEnergyForTier(ItemBlockBattery.getTier(theItem))
  }

  @SuppressWarnings(Array("unchecked"))
  override def getSubItems(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    for (tier <- 0 to TileBattery.maxTier)
    {
      par3List.add(Compatibility.getItemWithCharge(ItemBlockBattery.setTier(new ItemStack(this), tier.asInstanceOf[Byte]), 0))
      par3List.add(Compatibility.getItemWithCharge(ItemBlockBattery.setTier(new ItemStack(this), tier.asInstanceOf[Byte]), TileBattery.getEnergyForTier(tier)))

    }
  }
}