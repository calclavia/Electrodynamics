package resonantinduction.archaic.blocks

import java.util
import java.util.{ArrayList, List}

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{Entity, EntityList, EntityLivingBase, IProjectile}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.{Reference, RICreativeTab}

import scala.collection.JavaConversions._

object ItemImprint
{
    /**
     * Saves the list of items to filter out inside.
     */
    def setFilters(itemStack: ItemStack, filterStacks: java.util.List[ItemStack])
    {
        if (itemStack.getTagCompound == null)
        {
            itemStack.setTagCompound(new NBTTagCompound)
        }
        val nbt: NBTTagList = new NBTTagList
        import scala.collection.JavaConversions._
        for (filterStack <- filterStacks)
        {
            val newCompound: NBTTagCompound = new NBTTagCompound
            filterStack.writeToNBT(newCompound)
            nbt.appendTag(newCompound)
        }
        itemStack.getTagCompound.setTag("Items", nbt)
    }

    def isFiltering(filter: ItemStack, itemStack: ItemStack): Boolean =
    {
        if (filter != null && itemStack != null)
        {
            val checkStacks: List[ItemStack] = getFilters(filter)
            if (checkStacks != null)
            {
                import scala.collection.JavaConversions._
                for (stack <- checkStacks)
                {
                    if (stack.isItemEqual(itemStack))
                    {
                        return true
                    }
                }
            }
        }
        return false
    }

    def getFilters(itemStack: ItemStack): List[ItemStack] =
    {
        val filterStacks: List[ItemStack] = new util.LinkedList[ItemStack]
        val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
        val tagList: NBTTagList = nbt.getTagList("Items", 0)

        for (i <- 0 to tagList.tagCount)
        {
            val var4: NBTTagCompound = tagList.getCompoundTagAt(i)
            filterStacks.add(ItemStack.loadItemStackFromNBT(var4))
        }

        return filterStacks
    }

    def getFilterList(itemStack: ItemStack): List[ItemStack] =
    {
        val filterStacks: List[ItemStack] = new ArrayList[ItemStack]
        val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
        val tagList: NBTTagList = nbt.getTagList("Items", 0)

        for (i <- 0 to tagList.tagCount)
        {
            val var4: NBTTagCompound = tagList.getCompoundTagAt(i)
            filterStacks.add(ItemStack.loadItemStackFromNBT(var4))

        }
        return filterStacks
    }
}

class ItemImprint extends Item
{
    //Constructor
    setUnlocalizedName(Reference.prefix + "imprint")
    setTextureName(Reference.prefix + "imprint")
    setCreativeTab(RICreativeTab)
    setHasSubtypes(true)
    setMaxStackSize(1)

    override def onLeftClickEntity(stack: ItemStack, player: EntityPlayer, entity: Entity): Boolean =
    {
        if (entity != null && !(entity.isInstanceOf[IProjectile]) && !(entity.isInstanceOf[EntityPlayer]))
        {
            val stringName: String = EntityList.getEntityString(entity)
            return true
        }
        return false
    }

    def itemInteractionForEntity(par1ItemStack: ItemStack, par2EntityLiving: EntityLivingBase): Boolean =
    {
        return false
    }

    override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: java.util.List[_], par4: Boolean)
    {
        val filterItems: List[ItemStack] = ItemImprint.getFilters(itemStack)

        if (filterItems.size > 0)
        {
            for (filterItem <- filterItems)
            {
              if(filterItem != null)
                list.add(filterItem.getDisplayName)
            }
        }
        else
        {
            list.add(LanguageUtility.getLocal("tooltip.noImprint"))
        }
    }
}