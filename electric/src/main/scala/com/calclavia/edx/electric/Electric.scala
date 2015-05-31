package com.calclavia.edx.electric

import com.calclavia.edx.core.Reference
import com.calclavia.edx.electric.graph.NodeElectricComponent
import nova.core.block.Block
import nova.core.game.Game
import nova.core.loader.{Loadable, NovaMod}

@NovaMod(
	id = Reference.electricID,
	name = "Electrodynamics: Electric",
	version = Reference.version,
	novaVersion = Reference.novaVersion,
	dependencies = Array("microblock", "resonantengine")
)
object Electric extends Loadable {
	/*
	var itemWire = new ItemWire
	var itemMultimeter = new ItemMultimeter
	var itemTransformer = new ItemElectricTransformer
	@ExplicitContentName("insulation")
	var itemInsulation = new Item
	//var itemQuantumGlyph = new ItemQuantumGlyph

	var itemFocusingMatrix: ItemFocusingMatrix = new ItemFocusingMatrix

	var blockTesla: Block = new TileTesla
	var blockBattery: Block = new TileBattery
	var blockSolarPanel: Block = new TileSolarPanel
	var blockMotor: Block = new TileMotor
	var blockThermopile: Block = new TileThermopile

	var blockLaserEmitter: Block = new TileLaserEmitter
	var blockLaserReceiver: Block = new TileLaserReceiver
	var blockMirror: Block = new TileMirror
	var blockFocusCrystal: Block = new TileFocusCrystal
	var blockSiren: Block = new TileSiren

	var tierOneBattery: ItemStack = null
	var tierTwoBattery: ItemStack = null
	var tierThreeBattery: ItemStack = null*/

	override def preInit() {
		ElectricContent.preInit()

		/*
		//TODO: -1000 style points
		Game.instance.componentManager.register(args -> args.length > 0 ? new NodeElectricComponent((Block) args[ 0] ): new NodeElectricComponent(new Block() {
			override def getID: String = "dummy"
		}))

		Game.instance.componentManager.register(args -> args.length > 0 ? new NodeElectricJunction((Block) args[ 0] ): new NodeElectricJunction(new Block() {
			@Override
			public String getID() {
				return "dummy";
			}
		}))*/

		/*
		tierOneBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 0)
		tierTwoBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 1)
		tierThreeBattery = ItemBlockBattery.setTier(new ItemStack(ElectricalContent.blockBattery, 1, 0), 2)

		/** Register all parts */
		ResonantPartFactory.register(classOf[PartFramedWire])
		ResonantPartFactory.register(classOf[PartFlatWire])
		ResonantPartFactory.register(classOf[PartMultimeter])
		ResonantPartFactory.register(classOf[PartElectricTransformer])
		ResonantPartFactory.register(classOf[PartQuantumGlyph])

		MinecraftForge.EVENT_BUS.register(this)
		*/
	}

	override def init() {

		/*
		OreDictionary.registerOre("wire", Electric.itemWire)
		OreDictionary.registerOre("motor", Electric.blockMotor)
		OreDictionary.registerOre("battery", ItemBlockBattery.setTier(new ItemStack(Electric.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
		OreDictionary.registerOre("batteryBox", ItemBlockBattery.setTier(new ItemStack(Electric.blockBattery, 1, 0), 0.asInstanceOf[Byte]))
		*/
	}

	override def postInit() {
		/*
		recipes += shaped(new ItemStack(blockSiren, 2), "NPN", 'N', Blocks.noteblock, 'P', UniversalRecipe.SECONDARY_PLATE.get)
		recipes += shaped(blockTesla, "WEW", " C ", "DID", 'W', "wire", 'E', Items.ender_eye, 'C', UniversalRecipe.BATTERY.get, 'D', Items.diamond, 'I', UniversalRecipe.PRIMARY_PLATE.get)
		recipes += shaped(itemMultimeter, "WWW", "ICI", 'W', "wire", 'C', UniversalRecipe.BATTERY.get, 'I', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(tierOneBattery, "III", "IRI", "III", 'R', Blocks.redstone_block, 'I', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(tierTwoBattery, "RRR", "RIR", "RRR", 'R', tierOneBattery, 'I', UniversalRecipe.PRIMARY_PLATE.get)
		recipes += shaped(tierThreeBattery, "RRR", "RIR", "RRR", 'R', tierTwoBattery, 'I', Blocks.diamond_block)
		recipes += shaped(getWire(WireMaterial.COPPER, 3), "MMM", 'M', "ingotCopper")
		recipes += shaped(getWire(WireMaterial.TIN, 3), "MMM", 'M', "ingotTin")
		recipes += shaped(getWire(WireMaterial.IRON, 3), "MMM", 'M', Items.iron_ingot)
		recipes += shaped(getWire(WireMaterial.ALUMINUM, 3), "MMM", 'M', "ingotAluminum")
		recipes += shaped(getWire(WireMaterial.SILVER, 3), "MMM", 'M', "ingotSilver")
		recipes += shaped(getWire(WireMaterial.SUPERCONDUCTOR, 3), "MMM", 'M', "ingotSuperconductor")
		recipes += shaped(getWire(WireMaterial.SUPERCONDUCTOR, 3), "MMM", "MEM", "MMM", 'M', Items.gold_ingot, 'E', Items.ender_eye)
		//recipes += shaped(ElectricalContent.itemCharger, "WWW", "ICI", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get, 'C', UniversalRecipe.CIRCUIT_T1.get)
		recipes += shaped(itemTransformer, "WWW", "WWW", "III", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get)
		//recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 0), " CT", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
		//recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 1), "TCT", "LBL", " CT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
		//recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 2), "TC ", "LBL", "TCT", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
		//recipes += shaped(new ItemStack(ElectricalContent.itemQuantumGlyph, 1, 3), "TCT", "LBL", "TC ", 'B', Blocks.diamond_block, 'L', ElectricalContent.itemLevitator, 'C', ElectricalContent.itemCharger, 'T', ElectricalContent.blockTesla)
		recipes += shaped(blockSolarPanel, "CCC", "WWW", "III", 'W', "wire", 'C', Items.coal, 'I', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(blockMotor, "SRS", "SMS", "SWS", 'W', "wire", 'R', Items.redstone, 'M', Blocks.iron_block, 'S', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(blockThermopile, "ORO", "OWO", "OOO", 'W', "wire", 'O', Blocks.obsidian, 'R', Items.redstone)

		recipes += shaped(blockLaserEmitter, "IGI", "IDI", "III", 'G', Blocks.glass, 'I', Items.iron_ingot, 'D', Items.diamond)
		recipes += shaped(blockLaserReceiver, "IGI", "IRI", "III", 'G', Blocks.glass, 'I', Items.iron_ingot, 'R', Blocks.redstone_block)
		recipes += shaped(blockMirror, "GGG", "III", "GGG", 'G', Blocks.glass, 'I', Items.iron_ingot)
		recipes += shaped(blockFocusCrystal, "GGG", "GDG", "GGG", 'G', Blocks.glass, 'D', Items.diamond)
		recipes += shaped(itemFocusingMatrix, "GGG", "GNG", "GGG", 'G', Items.redstone, 'N', Items.quartz)

		if (Loader.isModLoaded("IC2"))
		{
			recipes += shapeless(getWire(WireMaterial.COPPER, 1), IC2Items.getItem("copperCableItem"))
			recipes += shapeless(getWire(WireMaterial.TIN, 1), IC2Items.getItem("tinCableItem"))
			recipes += shapeless(getWire(WireMaterial.IRON, 1), IC2Items.getItem("ironCableItem"))
			recipes += shapeless(IC2Items.getItem("copperCableItem"), getWire(WireMaterial.COPPER, 1))
			recipes += shapeless(IC2Items.getItem("tinCableItem"), getWire(WireMaterial.TIN, 1))
			recipes += shapeless(IC2Items.getItem("ironCableItem"), getWire(WireMaterial.IRON, 1))
		}
		if (Loader.isModLoaded("Mekanism"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(getWire(WireMaterial.COPPER, 1), "universalCable"))
		}*/
	}

	//def getWire(t: WireMaterial, count: Int): ItemStack = new ItemStack(itemWire, count, t.ordinal())
	/*
		/**
		 * Handle wire texture
		 */
		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		def preTextureHook(event: TextureStitchEvent.Pre)
		{
			if (event.map.getTextureType == 0)
			{
				RenderFlatWire.wireIcon = event.map.registerIcon(Reference.prefix + "models/flatWire")
				RenderFramedWire.wireIcon = event.map.registerIcon(Reference.prefix + "models/wire")
				RenderFramedWire.insulationIcon = event.map.registerIcon(Reference.prefix + "models/insulation")
			}
		}*/
}
