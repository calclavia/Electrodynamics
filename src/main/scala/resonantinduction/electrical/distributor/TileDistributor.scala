package resonantinduction.electrical.distributor

import java.util
import java.util.Collections

import net.minecraft.block.material.Material
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.ForgeDirection
import resonant.lib.content.module.TileBase
import resonant.lib.content.prefab.TraitElectrical
import resonant.lib.content.prefab.TraitInventory
import resonant.lib.utility.inventory.InventoryUtility
import universalelectricity.api.vector.Vector3

/**
 * A Block that interacts with connected inventories
 *
 * @since 22/03/14
 * @author tgame14
 */
class TileDistributor extends TileBase(Material.rock) with TraitInventory with TraitElectrical
{
  var state: EnumDistributorMode = EnumDistributorMode.PUSH
  var targetNode: Vector3 = new Vector3(this)

  override def updateEntity(): Unit =
  {
    super.updateEntity()
    val prevNode = targetNode.clone()

    val shuffledDirs = util.Arrays.asList(ForgeDirection.VALID_DIRECTIONS)
    Collections.shuffle(shuffledDirs)

    var hasInventoriesAround = false

    scala.util.control.Breaks.breakable
    {
      var index: Int = 0
      while (index < shuffledDirs.toArray().size)
      {
        targetNode = prevNode.clone().translate(ForgeDirection.getOrientation(index))
        val tile: TileEntity = targetNode.getTileEntity(world())
        if (tile.isInstanceOf[IInventory])
        {
          hasInventoriesAround = true
          scala.util.control.Breaks.break()
        }
        index += 1
      }
    }
    if (!targetNode.equals(prevNode) && hasInventoriesAround)
    {
      val inv: IInventory = targetNode.getTileEntity(world()).asInstanceOf[IInventory]
      callAction(inv)
    }

    else
      targetNode = new Vector3(this)




  }

  protected def callAction(inv: IInventory)
  {
    state match
    {
      case EnumDistributorMode.PUSH =>
      {
        InventoryUtility.putStackInInventory(inv, getStackInSlot(0), false)
      }

      case EnumDistributorMode.PULL =>
      {
        val filterStack: ItemStack = getStackInSlot(1)
        if (filterStack == null)
        {
          InventoryUtility.putStackInInventory(this, InventoryUtility.takeTopItemFromInventory(inv, ForgeDirection.UP.ordinal()), false)
          return
        }
        var index = 0
        while (index < inv.getSizeInventory)
        {
          if (inv.getStackInSlot(index) != null && inv.getStackInSlot(index).isItemEqual(filterStack))
          {
            var removeAmount = 0

            if (getStackInSlot(0) != null && getStackInSlot(0).isItemEqual(filterStack))
            {
              removeAmount = getStackInSlot(0).getItem.getItemStackLimit - getStackInSlot(0).stackSize
            }

            inv.getStackInSlot(index).stackSize -= removeAmount
            InventoryUtility.putStackInInventory(this, inv.getStackInSlot(index), false)
          }
          index += 1
        }


      }
    }
  }
}
