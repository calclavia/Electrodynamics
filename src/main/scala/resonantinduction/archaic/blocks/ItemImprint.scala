package resonantinduction.archaic.blocks

import java.util.{ArrayList, HashSet, List, Set}

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{Entity, EntityList, EntityLivingBase, IProjectile}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonantinduction.core.{Reference, ResonantTab}
import resonant.lib.wrapper.WrapList._

object ItemImprint
{
    /**
     * Saves the list of items to filter out inside.
     */
    def setFilters(itemStack: ItemStack, filterStacks: Set[ItemStack])
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
            val checkStacks: Set[ItemStack] = getFilters(filter)
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

    def getFilters(itemStack: ItemStack): HashSet[ItemStack] =
    {
        val filterStacks: HashSet[ItemStack] = new HashSet[ItemStack]
        val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
        val tagList: NBTTagList = nbt.getTagList("Items", 0)

        for (i <- 0 to tagList.tagCount)
        {
            val var4: NBTTagCompound = tagList.getCompoundTagAt(i).asInstanceOf[NBTTagCompound]
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
            val var4: NBTTagCompound = tagList.getCompoundTagAt(i).asInstanceOf[NBTTagCompound]
            filterStacks.add(ItemStack.loadItemStackFromNBT(var4))

        }
        return filterStacks
    }
}

class ItemImprint extends Item
{
    //Constructor
    this.setUnlocalizedName(Reference.prefix + "imprint")
    this.setTextureName(Reference.prefix + "imprint")
    this.setCreativeTab(ResonantTab.tab)
    this.setHasSubtypes(true)
    this.setMaxStackSize(1)

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
        val filterItems: Set[ItemStack] = ItemImprint.getFilters(itemStack)
        if (filterItems.size > 0)
        {
            import scala.collection.JavaConversions._
            for (filterItem <- filterItems)
            {
                list.add(filterItem.getDisplayName)
            }
        }
        else
        {
            list.add(LanguageUtility.getLocal("tooltip.noImprint"))
        }
    }
}