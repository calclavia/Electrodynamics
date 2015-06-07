package com.calclavia.edx.electric.circuit.source

import com.resonant.core.energy.EnergyStorage
import com.resonant.lib.WrapFunctions._
import nova.core.block.BlockFactory
import com.calclavia.edx.core.EDX
import nova.core.item.Item.TooltipEvent
import nova.core.item.ItemBlock
import nova.core.retention.{Storable, Store}

class ItemBlockBattery(blockFactory: BlockFactory) extends ItemBlock(blockFactory) with Storable {
  @Store
	var tier = 0

  @Store
	var energy = add(new EnergyStorage().setMax(BlockBattery.getEnergyForTier(tier)))

	tooltipEvent.add(
		eventListener((evt: TooltipEvent) => {
          evt.tooltips.add(EDX.language.translate("tooltip.tier") + ": " + (tier + 1))
		})
	)

	override def getMaxCount: Int = 1

	/*
  /**
   * Makes sure the item is uncharged when it is crafted and not charged. Change this if you do
   * not want this to happen!
   */
  override def onCreated(itemStack: ItemStack, par2World: World, par3EntityPlayer: EntityPlayer)
  {
    this.setEnergy(itemStack, 0)
  }

	override def recharge(itemStack: ItemStack, energy: Double, doReceive: Boolean): Double = {
		val rejectedElectricity: Double = Math.max((this.getEnergy(itemStack) + energy) - this.getEnergyCapacity(itemStack), 0)
		val energyToReceive: Double = Math.min(energy - rejectedElectricity, getTransferRate(itemStack))
		if (doReceive) {
			this.setEnergy(itemStack, this.getEnergy(itemStack) + energyToReceive)
		}
		return energyToReceive
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
  }*/
}