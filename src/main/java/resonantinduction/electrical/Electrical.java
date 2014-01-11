package resonantinduction.electrical;

import ic2.api.item.Items;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import resonantinduction.core.part.BlockMachinePart;
import resonantinduction.electrical.battery.BlockBattery;
import resonantinduction.electrical.battery.ItemBlockBattery;
import resonantinduction.electrical.battery.TileBattery;
import resonantinduction.electrical.furnace.BlockAdvancedFurnace;
import resonantinduction.electrical.furnace.TileAdvancedFurnace;
import resonantinduction.electrical.generator.solar.BlockSolarPanel;
import resonantinduction.electrical.generator.solar.TileSolarPanel;
import resonantinduction.electrical.levitator.BlockLevitator;
import resonantinduction.electrical.levitator.ItemBlockContractor;
import resonantinduction.electrical.levitator.TileEMLevitator;
import resonantinduction.electrical.multimeter.ItemMultimeter;
import resonantinduction.electrical.tesla.BlockTesla;
import resonantinduction.electrical.tesla.TileTesla;
import resonantinduction.electrical.transformer.ItemTransformer;
import resonantinduction.electrical.wire.EnumWireMaterial;
import resonantinduction.electrical.wire.ItemWire;
import resonantinduction.mechanical.grinder.BlockGrinderWheel;
import resonantinduction.mechanical.grinder.TileGrinderWheel;
import resonantinduction.mechanical.grinder.TilePurifier;
import resonantinduction.old.mechanics.purifier.BlockPurifier;
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

	// Energy
	private static Item itemPartWire;
	public static Item itemMultimeter;
	public static Item itemTransformer;
	public static Block blockTesla;
	public static Block blockBattery;

	// Generators
	public static Block blockSolarPanel;

	// Machines
	public static Block blockAdvancedFurnace, blockMachinePart, blockGrinderWheel, blockPurifier;

	// Transport
	public static Block blockEMContractor;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);

		Settings.CONFIGURATION.load();

		// Energy
		itemPartWire = new ItemWire(Settings.getNextItemID());
		itemMultimeter = new ItemMultimeter(Settings.getNextItemID());
		itemTransformer = new ItemTransformer(Settings.getNextItemID());
		blockTesla = new BlockTesla(Settings.getNextBlockID());
		blockBattery = new BlockBattery(Settings.getNextBlockID());

		// Transport
		blockEMContractor = new BlockLevitator();

		// Machines
		blockMachinePart = new BlockMachinePart();
		blockGrinderWheel = new BlockGrinderWheel(Settings.getNextBlockID());
		blockPurifier = new BlockPurifier(Settings.getNextBlockID());

		// Generator
		blockSolarPanel = new BlockSolarPanel();

		if (Settings.REPLACE_FURNACE)
		{
			blockAdvancedFurnace = BlockAdvancedFurnace.createNew(false);
			GameRegistry.registerBlock(blockAdvancedFurnace, "ri_" + blockAdvancedFurnace.getUnlocalizedName());
			GameRegistry.registerTileEntity(TileAdvancedFurnace.class, "ri_" + blockAdvancedFurnace.getUnlocalizedName());
		}

		Settings.CONFIGURATION.save();

		GameRegistry.registerItem(itemMultimeter, itemMultimeter.getUnlocalizedName());
		GameRegistry.registerItem(itemTransformer, itemTransformer.getUnlocalizedName());

		GameRegistry.registerBlock(blockSolarPanel, blockSolarPanel.getUnlocalizedName());
		GameRegistry.registerBlock(blockTesla, blockTesla.getUnlocalizedName());
		GameRegistry.registerBlock(blockBattery, ItemBlockBattery.class, blockBattery.getUnlocalizedName());
		GameRegistry.registerBlock(blockGrinderWheel, blockGrinderWheel.getUnlocalizedName());
		GameRegistry.registerBlock(blockPurifier, blockPurifier.getUnlocalizedName());
		GameRegistry.registerBlock(blockMachinePart, blockMachinePart.getUnlocalizedName());
		GameRegistry.registerBlock(blockEMContractor, ItemBlockContractor.class, blockEMContractor.getUnlocalizedName());

		// Tiles
		GameRegistry.registerTileEntity(TileTesla.class, blockTesla.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileBattery.class, blockBattery.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileSolarPanel.class, blockSolarPanel.getUnlocalizedName());
		GameRegistry.registerTileEntity(TilePurifier.class, blockPurifier.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileGrinderWheel.class, blockGrinderWheel.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEMLevitator.class, blockEMContractor.getUnlocalizedName());

		/**
		 * Set reference itemstacks
		 */
		ResonantInductionTabs.ITEMSTACK = new ItemStack(blockBattery);

		for (EnumWireMaterial material : EnumWireMaterial.values())
		{
			material.setWire(itemPartWire);
		}

		proxy.preInit();
		Settings.CONFIGURATION.save();
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
		final ItemStack defaultWire = EnumWireMaterial.IRON.getWire();

		/** Tesla - by Jyzarc */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockTesla, "WEW", " C ", " I ", 'W', defaultWire, 'E', Item.eyeOfEnder, 'C', UniversalRecipe.BATTERY.get(), 'I', UniversalRecipe.PRIMARY_PLATE.get()));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(itemMultimeter, "WWW", "ICI", 'W', defaultWire, 'C', UniversalRecipe.BATTERY.get(), 'I', UniversalRecipe.PRIMARY_METAL.get()));

		/** Battery */
		GameRegistry.addRecipe(new ShapedOreRecipe(blockBattery, "III", "IRI", "III", 'R', Block.blockRedstone, 'I', UniversalRecipe.PRIMARY_METAL.get()));

		/** Wires **/
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SILVER.getWire(), "MMM", 'M', "ingotSilver"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", 'M', "ingotSuperconductor"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", "MEM", "MMM", 'M', Item.ingotGold, 'E', Item.eyeOfEnder));

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
		replaceTileEntity(TileEntityFurnace.class, TileAdvancedFurnace.class);
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
