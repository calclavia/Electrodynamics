package com.calclavia.edx.electrical.circuit.wire

import java.awt.Color
import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.multipart._
import com.calclavia.edx.electrical.circuit.wire.base.WireMaterial
import com.calclavia.edx.electrical.circuit.wire.flat.BlockFlatWire$
import com.calclavia.edx.electrical.circuit.wire.framed.PartFramedWire
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.util.MultipartUtil
import edx.core.{Reference, ResonantPartFactory}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import nova.core.block.BlockFactory
import nova.core.item.ItemBlock
import nova.core.util.Direction
import org.lwjgl.input.Keyboard
import resonantengine.lib.render.EnumColor
import resonantengine.lib.utility.LanguageUtility
import resonantengine.lib.utility.science.UnitDisplay
import resonantengine.lib.wrapper.CollectionWrapper._

class ItemWire(blockFactory: BlockFactory) extends ItemBlock(blockFactory)
{
  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, side: Int, hit: Vector3): TMultiPart =
  {
    val onPos: BlockCoord = pos.copy.offset(side ^ 1)

    if (!MultipartUtil.canPlaceWireOnSide(world, onPos.x, onPos.y, onPos.z, Direction.getOrientation(side), false))
    {
      return null
    }

	  val wire = if (player.isSneaking) ResonantPartFactory.create(classOf[PartFramedWire]) else ResonantPartFactory.create(classOf[BlockFlatWire])

    if (wire != null)
    {
      wire.preparePlacement(side, itemStack.getItemDamage)
    }

    return wire
  }

  override def getMetadata(damage: Int): Int = damage

  override def getUnlocalizedName(itemStack: ItemStack): String =
  {
    return super.getUnlocalizedName(itemStack) + "." + WireMaterial.values()(itemStack.getItemDamage).name.toLowerCase
  }

  @SuppressWarnings(Array("unchecked"))
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: List[_], par4: Boolean)
  {
    if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
    {
      list.add(LanguageUtility.getLocal("tooltip.noShift").replace("#0", EnumColor.AQUA.toString).replace("#1", EnumColor.GREY.toString))
    }
    else
    {
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.resistance").replace("#v", "" + EnumColor.ORANGE + new UnitDisplay(UnitDisplay.Unit.RESISTANCE, WireMaterial.values()(itemStack.getItemDamage).resistance)))
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.current").replace("#v", "" + EnumColor.ORANGE + new UnitDisplay(UnitDisplay.Unit.AMPERE, WireMaterial.values()(itemStack.getItemDamage).maxCurrent)))
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.damage").replace("#v", "" + EnumColor.ORANGE + WireMaterial.values()(itemStack.getItemDamage).damage))
      list.addAll(LanguageUtility.splitStringPerWord(LanguageUtility.getLocal("tooltip.wire.helpText"), 5))
    }
  }

  @SideOnly(Side.CLIENT)
  override def getColorFromItemStack(itemStack: ItemStack, par2: Int): Int =
  {
    return new Color(WireMaterial.values()(itemStack.getItemDamage).color).darker.getRGB
  }

  override def getSubItems(itemID: Item, tab: CreativeTabs, listToAddTo: List[_])
  {
    for (mat <- WireMaterial.values)
    {
      listToAddTo.add(new ItemStack(itemID, 1, mat.ordinal))
    }
  }
}