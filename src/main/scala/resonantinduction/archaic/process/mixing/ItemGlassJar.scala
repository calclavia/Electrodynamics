package resonantinduction.archaic.process.mixing

import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.lib.prefab.tile.item.ItemBlockSaved
import resonant.lib.transform.vector.{IVector3, Vector3}
import resonant.lib.utility.nbt.NBTUtility
import resonantinduction.core.resource.alloy.{Alloy, TAlloyItem}

/**
 * The ItemBlock for the glass jar
 * @author Calclavia
 */
class ItemGlassJar(block: Block) extends ItemBlockSaved(block) with TAlloyItem
{
  var lastLook: Vector3 = null

  override def onUpdate(stack: ItemStack, world: World, entity: Entity, p_77663_4_ : Int, p_77663_5_ : Boolean): Unit =
  {
    val player = entity.asInstanceOf[EntityPlayer]

    if (!world.isRemote)
    {
      val currLook = new Vector3(player.getLookVec).asInstanceOf[IVector3]

      if (lastLook == null)
      {
        lastLook = currLook.asInstanceOf[Vector3]
      }

      val nbt = NBTUtility.getNBTTagCompound(stack)
      val alloy = new Alloy(nbt)

      if (alloy.size > 0)
      {
        if (lastLook.distance(currLook) > 1)
        {
          if (world.rand.nextFloat() < 0.05)
          {
            //Set mixed
            nbt.setBoolean("mixed", true)
          }
        }
      }

      lastLook = currLook.asInstanceOf[Vector3]
    }
  }
}