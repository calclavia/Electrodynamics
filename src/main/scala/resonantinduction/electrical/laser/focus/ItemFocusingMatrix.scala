package resonantinduction.electrical.laser.focus

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ChatComponentText, EnumChatFormatting}
import net.minecraft.world.World
import resonantinduction.core.ResonantTab
import resonantinduction.electrical.em.ElectromagneticCoherence
import universalelectricity.core.transform.rotation.Quaternion
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAsScala._
import scala.util.Random

/**
 * @author Calclavia
 */
class ItemFocusingMatrix extends Item
{
  setUnlocalizedName(ElectromagneticCoherence.PREFIX + "focusingMatrix")
  setTextureName(ElectromagneticCoherence.PREFIX + "focusingMatrix")
  setCreativeTab(ResonantTab)

  /**
   * allows items to add custom lines of information to the mouseover description
   */
  @SideOnly(Side.CLIENT)
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: java.util.List[_], par4: Boolean)
  {
    def add[T](list: java.util.List[T], value: Any) = list.add(value.asInstanceOf[T])
    add(list, EnumChatFormatting.BLUE + "Right click to select device.")
    add(list, EnumChatFormatting.BLUE + "Shift right click to focus device.")

    add(list, "Focusing:")

    val vec : Vector3 = getControlCoordinate(itemStack)

    if (vec != null)
      add(list, "[" + vec.xi + ", " + vec.yi + ", " + vec.z.toInt + "]")
    else
      add(list, "None")
  }

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
    val tile = world.getTileEntity(x, y, z)

    if (tile.isInstanceOf[IFocus] && !player.isSneaking)
    {
      if (!world.isRemote)
      {
        setControlCoordinate(itemStack, new Vector3(tile))
        player.addChatMessage(new ChatComponentText("Focusing matrix control block set."))
      }
      return true
    }
    else
    {
      if (!world.isRemote)
      {
        val controlVec = getControlCoordinate(itemStack)

        if (controlVec != null)
        {
          val controlTile = world.getTileEntity(controlVec.x.toInt, controlVec.y.toInt, controlVec.z.toInt)

          if (controlTile.isInstanceOf[IFocus])
          {
            val focusDevice = controlTile.asInstanceOf[IFocus]
            val clickPos = new Vector3(x, y, z) + 0.5

            if ((focusDevice.getFocus - ((clickPos - controlVec) - 0.5).normalize).magnitude < 0.1)
            {
              val cachedHits = focusDevice.getCacheDirections

              if (cachedHits != null && cachedHits.size > 0)
              {
                /**
                Pick random cached laser hit and allow it to focus on a block
                  */
                val rand = new Random(System.currentTimeMillis())
                val random_index = rand.nextInt(cachedHits.size)
                val incident = cachedHits(random_index).normalize

                val targetDirection : Vector3 = (new Vector3(x, y, z) - controlVec).normalize

                if (targetDirection.magnitude > 0)
                {
                  val angle = Math.acos(incident $ targetDirection)
                  val axis : Vector3 = incident.cross(targetDirection)
                  var focusDirection = incident.clone.transform(new Quaternion(-90 - Math.toDegrees(angle / 2), axis)).normalize

                  if (focusDirection.magnitude == 0 || focusDirection.magnitude.equals(Double.NaN))
                  {
                    focusDirection = targetDirection
                  }

                  controlTile.asInstanceOf[IFocus].focus(controlVec + 0.5 + focusDirection)
                }

              }
            }
            else
            {
              if (clickPos != (controlVec + 0.5))
                controlTile.asInstanceOf[IFocus].focus(clickPos)
            }
          }
        }
      }

      return true
    }

    return false
  }

  def setControlCoordinate(stack: ItemStack, vec: Vector3)
  {
    val nbt = if (stack.getTagCompound != null) stack.getTagCompound else new NBTTagCompound()
    vec.writeNBT(nbt)
    stack.setTagCompound(nbt)
  }

  def getControlCoordinate(stack: ItemStack): Vector3 =
  {
    val nbt = stack.getTagCompound

    if (nbt != null)
    {
      return new Vector3(nbt)
    }

    return null
  }

}
