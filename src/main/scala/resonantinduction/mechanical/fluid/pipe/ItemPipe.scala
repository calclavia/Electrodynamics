package resonantinduction.mechanical.fluid.pipe

import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.multipart.{MultiPartRegistry, TItemMultiPart, TMultiPart}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import org.lwjgl.input.Keyboard
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay

class ItemPipe extends TItemMultiPart
{
  setHasSubtypes(true)
  setMaxDamage(0)

  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, side: Int, hit: Vector3): TMultiPart =
  {
    val part: PartPipe = MultiPartRegistry.createPart("resonant_induction_pipe", false).asInstanceOf[PartPipe]
    part.preparePlacement(itemStack.getItemDamage)
    return part
  }

  override def getMetadata(damage: Int): Int =
  {
    return damage
  }

  override def getUnlocalizedName(itemStack: ItemStack): String =
  {
    return super.getUnlocalizedName(itemStack) + "." + LanguageUtility.underscoreToCamel(PipeMaterials.apply(itemStack.getItemDamage).toString)
  }

  @SuppressWarnings(Array("unchecked"))
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: List[_], par4: Boolean)
  {
    if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
    {
      list.add(LanguageUtility.getLocal("tooltip.noShift").replace("%0", EnumColor.AQUA.toString).replace("%1", EnumColor.GREY.toString))
    }
    else
    {
      val material = PipeMaterials.apply(itemStack.getItemDamage).asInstanceOf[PipeMaterials.PipeMaterial]
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.pipe.rate").replace("%v", "" + EnumColor.ORANGE + new UnitDisplay(UnitDisplay.Unit.LITER, material.maxFlow * 20) + "/s"))
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.pipe.pressure").replace("%v", "" + EnumColor.ORANGE + material.maxPressure + " Pa"))
    }
  }

  override def getSubItems(itemID: Item, tab: CreativeTabs, listToAddTo: List[_])
  {
    for (material <- PipeMaterials.values)
    {
      listToAddTo.add(new ItemStack(itemID, 1, material.id))
    }
  }
}