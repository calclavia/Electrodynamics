package resonantinduction.electrical.battery

import java.util.List
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import universalelectricity.api.UnitDisplay
import universalelectricity.api.UniversalClass
import universalelectricity.api.item.IEnergyItem
import universalelectricity.compatibility.Compatibility
import resonant.lib.wrapper.WrapList._

@UniversalClass object ItemBlockBattery
{
    def setTier(itemStack: ItemStack, tier: Byte): ItemStack =
    {
        if (itemStack.getTagCompound == null)
        {
            itemStack.setTagCompound(new NBTTagCompound)
        }
        itemStack.getTagCompound.setByte("tier", tier)
        return itemStack
    }

    def getTier(itemStack: ItemStack): Byte =
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

    def setEnergy(itemStack: ItemStack, joules: Double)
    {
        if (itemStack.getTagCompound == null)
        {
            itemStack.setTagCompound(new NBTTagCompound)
        }
        val electricityStored: Double = Math.max(Math.min(joules, this.getEnergyCapacity(itemStack)), 0)
        itemStack.getTagCompound.setDouble("electricity", electricityStored)
    }

    def getTransfer(itemStack: ItemStack): Double =
    {
        return this.getEnergyCapacity(itemStack) - this.getEnergy(itemStack)
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

    override def getDisplayDamage(stack: ItemStack): Int =
    {
        return (100 - (this.getEnergy(stack).asInstanceOf[Double] / getEnergyCapacity(stack).asInstanceOf[Double]) * 100).asInstanceOf[Int]
    }

    def getEnergyCapacity(theItem: ItemStack): Double =
    {
        return TileBattery.getEnergyForTier(ItemBlockBattery.getTier(theItem))
    }

    def getTransferRate(itemStack: ItemStack): Double =
    {
        return this.getEnergyCapacity(itemStack) / 100
    }

    @SuppressWarnings(Array("unchecked")) override def getSubItems(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
    {
        {
            var tier: Byte = 0
            while (tier <= TileBattery.MAX_TIER)
            {
                {
                    par3List.add(Compatibility.getItemWithCharge(ItemBlockBattery.setTier(new ItemStack(this), tier), 0))
                    par3List.add(Compatibility.getItemWithCharge(ItemBlockBattery.setTier(new ItemStack(this), tier), TileBattery.getEnergyForTier(tier)))
                }
                ({
                    tier += 1; tier - 1
                })
            }
        }
    }
}