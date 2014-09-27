package resonantinduction.mechanical.mech.gear

import java.util.List
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.core.prefab.part.IHighlight
import resonantinduction.mechanical.mech.gearshaft.PartGearShaft
import codechicken.lib.vec.BlockCoord
import codechicken.lib.vec.Vector3
import codechicken.microblock.FacePlacementGrid
import codechicken.multipart.JItemMultiPart
import codechicken.multipart.MultiPartRegistry
import codechicken.multipart.PartMap
import codechicken.multipart.TMultiPart
import codechicken.multipart.TileMultipart
import resonant.lib.wrapper.WrapList._

class ItemGear extends JItemMultiPart with IHighlight
{
    //Constructor
    setHasSubtypes(true)

    override def getUnlocalizedName(itemStack: ItemStack): String =
    {
        return super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage
    }

    override def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, s: Int, hit: Vector3): TMultiPart =
    {
        val part: PartGear = MultiPartRegistry.createPart("resonant_induction_gear", false).asInstanceOf[PartGear]
        var side: Int = FacePlacementGrid.getHitSlot(hit, s)
        val tile: TileEntity = world.getTileEntity(pos.x, pos.y, pos.z)
        if (tile.isInstanceOf[TileMultipart])
        {
            val occupyingPart: TMultiPart = (tile.asInstanceOf[TileMultipart]).partMap(side)
            val centerPart: TMultiPart = (tile.asInstanceOf[TileMultipart]).partMap(PartMap.CENTER.ordinal)
            val clickedCenter: Boolean = hit.mag < 0.4
            if ((clickedCenter && centerPart.isInstanceOf[PartGearShaft]))
            {
                side ^= 1
            }
        }
        part.preparePlacement(side, itemStack.getItemDamage)
        return part
    }

    override def getSubItems(itemID: Item, tab: CreativeTabs, listToAddTo: List[_])
    {
            for(i <- 0 to 3)
            {
                    listToAddTo.add(new ItemStack(itemID, 1, i))

        }
        listToAddTo.add(new ItemStack(itemID, 1, 10))
    }

    def getHighlightType: Int =
    {
        return 0
    }
}