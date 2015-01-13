package resonantinduction.archaic.process.mixing

import java.util

import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import org.lwjgl.input.Keyboard
import resonant.lib.prefab.tile.item.ItemBlockSaved
import resonant.lib.render.EnumColor
import resonant.lib.transform.vector.{IVector3, Vector3}
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.StringWrapper._
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.resource.Alloy

/**
 * The ItemBlock for the glass jar
 * @author Calclavia
 */
class ItemGlassJar(block: Block) extends ItemBlockSaved(block)
{
  var lastLook: Vector3 = null

  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean)
  {
    super.addInformation(itemStack, player, list, par4)

    if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
    {
      list.add("tooltip.noShift".getLocal.replace("#0", EnumColor.AQUA.toString).replace("#1", EnumColor.GREY.toString))
    }
    else
    {
      val nbt = NBTUtility.getNBTTagCompound(itemStack)
      val alloy = new Alloy(nbt)
      alloy.content.map(c => EnumColor.ORANGE + c._1.capitalizeFirst + EnumColor.DARK_RED + " " + Math.round(alloy.percentage(c._1) * 100) + "%").foreach(m => list.add(m))
    }
  }

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

      if (lastLook.distance(currLook) > 1.0D)
      {
        if (world.rand.nextFloat() < 0.05)
        {
          //Set mixed
          NBTUtility.getNBTTagCompound(stack).setBoolean("mixed", true)
        }
      }

      lastLook = currLook.asInstanceOf[Vector3]
    }
  }
}