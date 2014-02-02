package resonantinduction.electrical;

import ic2.api.item.Items;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.electrical.battery.BlockBattery;
import resonantinduction.electrical.battery.ItemBlockBattery;
import resonantinduction.electrical.battery.TileBattery;
import resonantinduction.electrical.charger.BlockCharger;
import resonantinduction.electrical.charger.TileCharger;
import resonantinduction.electrical.encoder.ItemDisk;
import resonantinduction.electrical.generator.BlockGenerator;
import resonantinduction.electrical.generator.TileGenerator;
import resonantinduction.electrical.generator.solar.BlockSolarPanel;
import resonantinduction.electrical.generator.solar.TileSolarPanel;
import resonantinduction.electrical.generator.thermopile.BlockThermopile;
import resonantinduction.electrical.generator.thermopile.TileThermopile;
import resonantinduction.electrical.levitator.BlockLevitator;
import resonantinduction.electrical.levitator.TileLevitator;
import resonantinduction.electrical.multimeter.ItemMultimeter;
import resonantinduction.electrical.tesla.BlockTesla;
import resonantinduction.electrical.tesla.TileTesla;
import resonantinduction.electrical.transformer.ItemTransformer;
import resonantinduction.electrical.wire.EnumWireMaterial;
import resonantinduction.electrical.wire.ItemWire;
import calclavia.lib.content.ContentRegistry;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.recipe.UniversalRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Resonant Induction Electrical Module
 * 
 * @author Calclavia
 * 
 */
@Mod(modid = Electrical.ID, name = Electrical.NAME, version = Reference.VERSION, dependencies = "before:ThermalExpansion;after:" + ResonantInduction.ID + "|Mechanical;required-after:" + ResonantInduction.ID)
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Electrical
{
	/** Mod Information */
	public static final String ID = "ResonantInduction|Electrical";
	public static final String NAME = Reference.NAME + " Electrical";

	@Instance(ID)
	public static Electrical INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.electrical.ClientProxy", serverSide = "resonantinduction.electrical.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, ID);

	// Energy
	private static Item itemPartWire;
	public static Item itemMultimeter;
	public static Item itemTransformer;
	public static Block blockTesla;
	public static Block blockBattery;
	public static Block blockEncoder;

	// Generators
	public static Block blockSolarPanel;
	public static Block blockGenerator;
	public static Block blockThermopile;

	// Machines
	public static Block blockItemCharger;

	// Transport
	public static Block blockEMLevitator;
	public static Block blockArmbot;
	public static Item itemDisk;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);

		Settings.load();

		// Energy
		itemPartWire = contentRegistry.createItem(ItemWire.class);
		itemMultimeter = contentRegistry.createItem(ItemMultimeter.class);
		itemTransformer = contentRegistry.createItem(ItemTransformer.class);
		blockTesla = contentRegistry.createTile(BlockTesla.class, TileTesla.class);
		blockBattery = contentRegistry.createBlock(BlockBattery.class, ItemBlockBattery.class, TileBattery.class);

		// Transport
		blockEMLevitator = contentRegistry.createTile(BlockLevitator.class, TileLevitator.class);
		// blockArmbot = contentRegistry.createTile(BlockArmbot.class, TileArmbot.class);
		// blockEncoder = contentRegistry.createTile(BlockEncoder.class, TileEncoder.class);
		itemDisk = contentRegistry.createItem(ItemDisk.class);

		// Generator
		blockSolarPanel = contentRegistry.createTile(BlockSolarPanel.class, TileSolarPanel.class);
		blockGenerator = contentRegistry.createTile(BlockGenerator.class, TileGenerator.class);
		blockThermopile = contentRegistry.createTile(BlockThermopile.class, TileThermopile.class);
		
		blockItemCharger = contentRegistry.createTile(BlockCharger.class, TileCharger.class);

		Settings.save();

		OreDictionary.registerOre("wire", itemPartWire);
		OreDictionary.registerOre("battery", ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0));
		OreDictionary.registerOre("batteryBox", ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0));

		/**
		 * Set reference itemstacks
		 */
		TabRI.ITEMSTACK = new ItemStack(itemTransformer);

		for (EnumWireMaterial material : EnumWireMaterial.values())
		{
			material.setWire(itemPartWire);
		}

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		Settings.setModMetadata(metadata, ID, NAME);
		MultipartElectrical.INSTANCE = new MultipartElectrical();
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		/**
		 * Recipes
		 */
		/** Tesla - by Jyzarc */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockTesla, "WEW", " C ", " I ", 'W', "wire", 'E', Item.eyeOfEnder, 'C', UniversalRecipe.BATTERY.get(), 'I', UniversalRecipe.PRIMARY_PLATE.get()));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(itemMultimeter, "WWW", "ICI", 'W', "wire", 'C', UniversalRecipe.BATTERY.get(), 'I', UniversalRecipe.PRIMARY_METAL.get()));

		/** Battery */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0), "III", "IRI", "III", 'R', Block.blockRedstone, 'I', UniversalRecipe.PRIMARY_METAL.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 1), "RRR", "RIR", "RRR", 'R', ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 1), 'I', UniversalRecipe.PRIMARY_PLATE.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 2), "RRR", "RIR", "RRR", 'R', ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0), 'I', Block.blockDiamond));

		/** Wires **/
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SILVER.getWire(), "MMM", 'M', "ingotSilver"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", 'M', "ingotSuperconductor"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", "MEM", "MMM", 'M', Item.ingotGold, 'E', Item.eyeOfEnder));

		GameRegistry.addRecipe(new ShapedOreRecipe(itemTransformer, "WWW", "WWW", "III", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get()));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockEMLevitator, " G ", "SDS", "SWS", 'W', "wire", 'G', Block.glass, 'D', Block.blockDiamond, 'S', UniversalRecipe.PRIMARY_METAL.get()));

		/** Generators **/
		GameRegistry.addRecipe(new ShapedOreRecipe(blockSolarPanel, "CCC", "WWW", "III", 'W', "wire", 'C', Item.coal, 'I', UniversalRecipe.PRIMARY_METAL.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockGenerator, "SRS", "SMS", "SWS", 'W', "wire", 'R', Item.redstone, 'M', UniversalRecipe.MOTOR.get(), 'S', UniversalRecipe.PRIMARY_METAL.get()));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockThermopile, "ORO", "OWO", "OOO", 'W', "wire", 'O', Block.obsidian, 'R', Item.redstone));

		/** Wire Compatiblity **/
		if (Loader.isModLoaded("IC2"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), Items.getItem("copperCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.TIN.getWire(), Items.getItem("tinCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.IRON.getWire(), Items.getItem("ironCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(), Items.getItem("glassFiberCableItem")));
		}

		if (Loader.isModLoaded("Mekanism"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), "universalCable"));
		}

		proxy.postInit();

		/** Inject new furnace tile class */
		// replaceTileEntity(TileEntityFurnace.class, TileAdvancedFurnace.class);
	}

	public static void replaceTileEntity(Class<? extends TileEntity> findTile, Class<? extends TileEntity> replaceTile)
	{
		try
		{
			Map<String, Class> nameToClassMap = ObfuscationReflectionHelper.getPrivateValue(TileEntity.class, null, "field_" + "70326_a", "nameToClassMap", "a");
			Map<Class, String> classToNameMap = ObfuscationReflectionHelper.getPrivateValue(TileEntity.class, null, "field_" + "70326_b", "classToNameMap", "b");

			String findTileID = classToNameMap.get(findTile);

			if (findTileID != null)
			{
				nameToClassMap.put(findTileID, replaceTile);
				classToNameMap.put(replaceTile, findTileID);
				classToNameMap.remove(findTile);
				ResonantInduction.LOGGER.fine("Replaced TileEntity: " + findTile);
			}
			else
			{
				ResonantInduction.LOGGER.severe("Failed to replace TileEntity: " + findTile);
			}
		}
		catch (Exception e)
		{
			ResonantInduction.LOGGER.severe("Failed to replace TileEntity: " + findTile);
			e.printStackTrace();
		}
	}
}
