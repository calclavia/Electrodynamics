package resonantinduction.electrical

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.registry.GameRegistry
import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.util.{ResourceLocation, EnumChatFormatting}
import net.minecraftforge.oredict.ShapelessOreRecipe
import resonant.content.loader.ContentHolder
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.electrical.battery.ItemBlockBattery
import resonantinduction.electrical.laser.emitter.BlockLaserEmitter
import resonantinduction.electrical.laser.focus.ItemFocusingMatrix
import resonantinduction.electrical.laser.focus.crystal.BlockFocusCrystal
import resonantinduction.electrical.laser.focus.mirror.BlockMirror
import resonantinduction.electrical.laser.receiver.BlockLaserReceiver
import resonantinduction.electrical.wire.EnumWireMaterial

/**
 * Created by robert on 8/11/2014.
 */
object ElectricalContent extends ContentHolder {


  val particleTextures = new ResourceLocation("textures/particle/particles.png")

  val tierOneBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0.asInstanceOf[Byte])
  val tierTwoBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 1.asInstanceOf[Byte])
  val tierThreeBattery: ItemStack = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 2.asInstanceOf[Byte])

  var itemWire: Item = null
  var itemMultimeter: Item = null
  var itemTransformer: Item = null
  var itemCharger: Item = null
  var blockTesla: Block = null
  var blockBattery: Block = null
  var blockEncoder: Block = null
  var itemRailing: Item = null
  var blockSolarPanel: Block = null
  var blockMotor: Block = null
  var blockThermopile: Block = null
  var itemLevitator: Item = null
  var itemDisk: Item = null
  var itemInsulation: Item = null
  var blockQuantumGate: Block = null
  var itemQuantumGlyph: Item = null
  var blockLaserEmitter: BlockLaserEmitter = null
  var blockLaserReceiver: BlockLaserReceiver = null
  var blockMirror: BlockMirror = null
  var blockFocusCrystal: BlockFocusCrystal = null

  var itemFocusingMatrix: ItemFocusingMatrix = null

  var guideBook: ItemStack = createGuide

  override def postInit
  {
    recipes += shaped(ElectricalContent.blockTesla, "WEW", " C ", "DID", 'W', "wire", 'E', Items.ender_eye, 'C', UniversalRecipe.BATTERY.get, 'D', Items.diamond, 'I', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(ElectricalContent.itemMultimeter, "WWW", "ICI", 'W', "wire", 'C', UniversalRecipe.BATTERY.get, 'I', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(tierOneBattery, "III", "IRI", "III", 'R', Blocks.redstone_block, 'I', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(tierTwoBattery, "RRR", "RIR", "RRR", 'R', tierOneBattery, 'I', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(tierThreeBattery, "RRR", "RIR", "RRR", 'R', tierTwoBattery, 'I', Blocks.diamond_block)
    recipes += shaped(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper")
    recipes += shaped(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin")
    recipes += shaped(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Items.iron_ingot)
    recipes += shaped(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum")
    recipes += shaped(EnumWireMaterial.SILVER.getWire, "MMM", 'M', "ingotSilver")
    recipes += shaped(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", 'M', "ingotSuperconductor")
    recipes += shaped(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", "MEM", "MMM", 'M', Items.gold_ingot, 'E', Items.ender_eye)
    recipes += shaped(ElectricalContent.itemCharger, "WWW", "ICI", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get, 'C', UniversalRecipe.CIRCUIT_T1.get)
    recipes += shaped(ElectricalContent.itemTransformer, "WWW", "WWW", "III", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 0), " CT", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 1), "TCT", "LBL", " CT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 2), "TC ", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 3), "TCT", "LBL", "TC ", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
    recipes += shaped(ElectricalContent.blockSolarPanel, "CCC", "WWW", "III", 'W', "wire", 'C', Items.coal, 'I', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(ElectricalContent.blockMotor, "SRS", "SMS", "SWS", 'W', "wire", 'R', Items.redstone, 'M', Blocks.iron_block, 'S', UniversalRecipe.PRIMARY_METAL.get)
    recipes += shaped(ElectricalContent.blockThermopile, "ORO", "OWO", "OOO", 'W', "wire", 'O', Blocks.obsidian, 'R', Items.redstone)

    recipes += shaped(blockLaserEmitter, "IGI", "IDI", "III", 'G, Blocks.glass, 'I', Items.iron_ingot, 'D', Items.diamond)
    recipes += shaped(blockLaserReceiver, "IGI", "IRI", "III", 'G', Blocks.glass, 'I', Items.iron_ingot, 'R', Blocks.redstone_block)
    recipes += shaped(blockMirror, "GGG", "III", "GGG", 'G', Blocks.glass, 'I', Items.iron_ingot)
    recipes += shaped(blockFocusCrystal, "GGG", "GDG", "GGG", 'G', Blocks.glass, 'D', Items.diamond)
    recipes += shaped(itemFocusingMatrix, "GGG", "GNG", "GGG", 'G', Items.redstone, 'N', Items.quartz)


    if (Loader.isModLoaded("IC2"))
    {
      recipes += shapeless(EnumWireMaterial.COPPER.getWire, IC2Items.getItem("copperCableItem"))
      recipes += shapeless(EnumWireMaterial.TIN.getWire, IC2Items.getItem("tinCableItem"))
      recipes += shapeless(EnumWireMaterial.IRON.getWire, IC2Items.getItem("ironCableItem"))
      recipes += shapeless(IC2Items.getItem("copperCableItem"), EnumWireMaterial.COPPER.getWire)
      recipes += shapeless(IC2Items.getItem("tinCableItem"), EnumWireMaterial.TIN.getWire)
      recipes += shapeless(IC2Items.getItem("ironCableItem"), EnumWireMaterial.IRON.getWire)
    }
    if (Loader.isModLoaded("Mekanism"))
    {
      GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire, "universalCable"))
    }

  }

  def createGuide: ItemStack = {

    val guideBook = new ItemStack(Items.written_book)
    val bookNBT = new NBTTagCompound()
    bookNBT.setString("title", "Electromagnetic Coherence Guide")
    bookNBT.setString("author", "Calclavia")

    val pages = new NBTTagList()
    pages.appendTag(new NBTTagString(EnumChatFormatting.RED + "Guidebook:\n\n" + EnumChatFormatting.BLACK + "Electromagnetic Coherence is a mod all about lasers.\n\nYou can find all the blocks in the mod's creative tab."))
    pages.appendTag(new NBTTagString("A laser can be focused through a  " + EnumChatFormatting.RED + "laser emitter" + EnumChatFormatting.BLACK + ". By default, the color of the laser is white. The color can be changed by placing stained glass in front of it. Different combinations of glass would result in mixed colors."))
    pages.appendTag(new NBTTagString("To create a laser beam, provide a redstone pulse to the laser emitter. The intensity of the redstone would determine the intensity of the laser. Lasers with high intensities can burn and melt through blocks, hurting entities."))
    pages.appendTag(new NBTTagString("A laser beam can also be reflected using a  " + EnumChatFormatting.RED + "mirror" + EnumChatFormatting.BLACK + " with reduced intensity. Mirrors can be rotated by right clicking on it. Shift-right clicking a mirror focuses it to a side. Mirrors can also be auto-rotated with a redstone signal based on the direction of the signal propagation."))
    pages.appendTag(new NBTTagString("A " + EnumChatFormatting.RED + "laser receiver" + EnumChatFormatting.BLACK + " outputs a redstone signal with a strength based on the laser incident on its front. Using this, laser trip-wires can be made as entities walking through a laser will block its path."))
    pages.appendTag(new NBTTagString("The " + EnumChatFormatting.RED + "focusing matrix" + EnumChatFormatting.BLACK + " allows the player to focus mirrors and focus crystals. First, right click on a mirror/crystal to select it. Then, right click on a point to focus. Clicking the point twice will aim the laser at that point instead of making the device look at the point."))
    pages.appendTag(new NBTTagString("The " + EnumChatFormatting.RED + "Focus Crystal" + EnumChatFormatting.BLACK + " allows you to focus multiple laser beams into a single one, adding their strength together. All beams aiming at the crystal will be sent in the direction the crystal is facing. Focus Crystals can be rotated the same way as mirrors can."))
    pages.appendTag(new NBTTagString(EnumChatFormatting.RED + "Usages\n\n" + EnumChatFormatting.BLACK + "- Light Shows\n- Mining\n- Killing\n- Burning\n- Redstone Detection\n- Smelting (Aim strong laser at furnace)\n\nComing Soon:\n- Energy Transfer\n- Crafting"))

    bookNBT.setTag("pages", pages)
    guideBook.setTagCompound(bookNBT)

    return guideBook
  }

}
