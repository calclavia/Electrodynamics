package edx.electrical.multimeter

import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.microblock.FacePlacementGrid
import codechicken.multipart.{JItemMultiPart, PartMap, TMultiPart, TileMultipart}
import edx.core.ResonantPartFactory
import edx.core.prefab.part.IHighlight
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import org.lwjgl.input.Keyboard
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.CollectionWrapper._

class ItemMultimeter extends JItemMultiPart with IHighlight
{
  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, vSide: Int, hit: Vector3): TMultiPart =
  {
    val side = FacePlacementGrid.getHitSlot(hit, vSide)
    val tile: TileEntity = world.getTileEntity(pos.x, pos.y, pos.z)

    if (tile.isInstanceOf[TileMultipart])
    {
      val centerPart: TMultiPart = tile.asInstanceOf[TileMultipart].partMap(PartMap.CENTER.ordinal)
      if (centerPart != null && !player.isSneaking)
      {
        pos.offset(side ^ 1)
      }
    }

    val part = ResonantPartFactory.create(classOf[PartMultimeter])

    if (part != null)
    {
      val l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3
      val facing = if (l == 0) 2 else (if (l == 1) 5 else (if (l == 2) 3 else (if (l == 3) 4 else 0)))
      part.preparePlacement(side, facing)
    }

    return part
  }

  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
    {
      list.add(LanguageUtility.getLocal("tooltip.noShift").replace("#0", EnumColor.AQUA.toString).replace("#1", EnumColor.GREY.toString))
    }
    else
    {
      list.addAll(LanguageUtility.splitStringPerWord(LanguageUtility.getLocal("item.resonantinduction:multimeter.tooltip"), 5))
    }
  }

  def getDetection(itemStack: ItemStack): Float =
  {
    if (itemStack.stackTagCompound == null || !itemStack.getTagCompound.hasKey("detection"))
    {
      return -1
    }
    return itemStack.stackTagCompound.getFloat("detection")
  }

  def setDetection(itemStack: ItemStack, detection: Float)
  {
    if (itemStack.stackTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    itemStack.stackTagCompound.setFloat("detection", detection)
  }

  def getHighlightType: Int = 0
}