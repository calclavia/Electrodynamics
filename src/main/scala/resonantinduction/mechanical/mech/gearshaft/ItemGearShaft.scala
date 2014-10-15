package resonantinduction.mechanical.mech.gearshaft

import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.multipart.{JItemMultiPart, MultiPartRegistry, TMultiPart}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.prefab.part.IHighlight

class ItemGearShaft extends JItemMultiPart with IHighlight
{
    //Constructor
    setHasSubtypes(true)

    override def getUnlocalizedName(itemStack: ItemStack): String =
    {
        return super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage
    }

    def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, side: Int, hit: Vector3): TMultiPart =
    {
        val part: PartGearShaft = MultiPartRegistry.createPart("resonant_induction_gear_shaft", false).asInstanceOf[PartGearShaft]
        if (part != null)
        {
            part.preparePlacement(side, itemStack.getItemDamage)
        }
        return part
    }

    override def getSubItems(itemID: Item, tab: CreativeTabs, listToAddTo: List[_])
    {
        for (i <- 0 until 3)
        {
            listToAddTo.add(new ItemStack(itemID, 1, i))
        }
    }

    def getHighlightType: Int =
    {
        return 0
    }
}