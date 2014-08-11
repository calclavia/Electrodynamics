package resonantinduction.electrical

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.registry.GameRegistry
import ic2.api.item.IC2Items
import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.oredict.ShapelessOreRecipe
import resonant.content.loader.ContentHolder
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.electrical.battery.ItemBlockBattery
import resonantinduction.electrical.wire.EnumWireMaterial

/**
 * Created by robert on 8/11/2014.
 */
object ElectricalContent extends ContentHolder {

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

}
