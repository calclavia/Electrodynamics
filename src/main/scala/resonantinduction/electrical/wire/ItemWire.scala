package resonantinduction.electrical.wire

import java.awt.Color
import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.multipart._
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.input.Keyboard
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonantinduction.core.util.MultipartUtil
import resonantinduction.core.{Reference, ResonantTab}
import resonantinduction.electrical.wire.base.{WireMaterial, TWire}
import resonantinduction.electrical.wire.flat.{PartFlatWire, RenderFlatWire}
import resonantinduction.electrical.wire.framed.PartFramedWire
import universalelectricity.api.UnitDisplay
import resonant.lib.wrapper.WrapList._

class ItemWire extends TItemMultiPart
{
  setUnlocalizedName(Reference.prefix + "wire")
  setTextureName(Reference.prefix + "wire")
  setCreativeTab(ResonantTab.tab)
  setHasSubtypes(true)
  setMaxDamage(0)

  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, side: Int, hit: Vector3): TMultiPart =
  {
    val onPos: BlockCoord = pos.copy.offset(side ^ 1)

    if (!MultipartUtil.canPlaceWireOnSide(world, onPos.x, onPos.y, onPos.z, ForgeDirection.getOrientation(side), false))
    {
      return null
    }

    val wire = (if(ControlKeyHandler.isPressed) MultiPartRegistry.createPart("resonant_induction_framed_wire", false) else  MultiPartRegistry.createPart("resonant_induction_flat_wire", false)).asInstanceOf[TWire]

    if (wire != null)
    {
      wire.preparePlacement(side, itemStack.getItemDamage)
    }

    return wire
  }

  override def getMetadata(damage: Int): Int =
  {
    return damage
  }

  override def getUnlocalizedName(itemStack: ItemStack): String =
  {
    return super.getUnlocalizedName(itemStack) + "." + WireMaterial.values()(itemStack.getItemDamage).name.toLowerCase
  }

  @SuppressWarnings(Array("unchecked")) override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: List[_], par4: Boolean)
  {
    if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
    {
      list.add(LanguageUtility.getLocal("tooltip.noShift").replace("%0", EnumColor.AQUA.toString).replace("%1", EnumColor.GREY.toString))
    }
    else
    {
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.resistance").replace("%v", "" + EnumColor.ORANGE + new UnitDisplay(UnitDisplay.Unit.RESISTANCE, WireMaterial.values()(itemStack.getItemDamage).resistance)))
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.current").replace("%v", "" + EnumColor.ORANGE + new UnitDisplay(UnitDisplay.Unit.AMPERE, WireMaterial.values()(itemStack.getItemDamage).maxCurrent)))
      list.add(EnumColor.AQUA + LanguageUtility.getLocal("tooltip.wire.damage").replace("%v", "" + EnumColor.ORANGE + WireMaterial.values()(itemStack.getItemDamage).damage))
      list.addAll(LanguageUtility.splitStringPerWord(LanguageUtility.getLocal("tooltip.wire.helpText"), 5))
    }
  }

  @SideOnly(Side.CLIENT) override def getSpriteNumber: Int =
  {
    return 0
  }

  @SideOnly(Side.CLIENT) override def registerIcons(register: IIconRegister)
  {
    RenderFlatWire.flatWireTexture = register.registerIcon(Reference.prefix + "models/flatWire")
    super.registerIcons(register)
  }

  @SideOnly(Side.CLIENT) override def getColorFromItemStack(itemStack: ItemStack, par2: Int): Int =
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