package resonantinduction.electrical.distributor

import calclavia.lib.content.prefab.{TraitElectrical, TraitInventory}
import net.minecraft.block.material.Material
import calclavia.lib.content.module.TileBase
import universalelectricity.api.vector.Vector3
import net.minecraft.inventory.IInventory
import net.minecraftforge.common.ForgeDirection
import calclavia.lib.utility.inventory.InventoryUtility
import net.minecraft.item.ItemStack
import java.util.Collections
import java.util

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
    for (dir: ForgeDirection <- shuffledDirs)
    {
      targetNode = prevNode.clone().translate(ForgeDirection.getOrientation(world().rand.nextInt(6)))
      if (!(targetNode.getTileEntity(world()) isInstanceOf IInventory))
      {
        hasInventoriesAround = true
        break
      }
    }
    if (!targetNode.equals(prevNode) && hasInventoriesAround)
    {
      callAction(targetNode.getTileEntity())
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

        for (index: Int <- inv.getSizeInventory)
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
        }


      }
    }
  }
}
