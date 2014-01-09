package resonantinduction.electrical;

import ic2.api.item.Items;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import resonantinduction.electrical.battery.BlockBattery;
import resonantinduction.electrical.battery.ItemBlockBattery;
import resonantinduction.electrical.battery.TileBattery;
import resonantinduction.electrical.multimeter.ItemMultimeter;
import resonantinduction.electrical.tesla.BlockTesla;
import resonantinduction.electrical.tesla.TileTesla;
import resonantinduction.electrical.transformer.ItemTransformer;
import resonantinduction.electrical.wire.EnumWireMaterial;
import resonantinduction.electrical.wire.ItemWire;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.recipe.UniversalRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
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
@Mod(modid = Electrical.ID, name = Electrical.NAME, version = Reference.VERSION, dependencies = "required-after:" + ResonantInduction.ID)
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

	// Items
	private static Item itemPartWire;
	public static Item itemMultimeter;
	public static Item itemTransformer;

	// Blocks
	public static Block blockTesla;
	public static Block blockBattery;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);

		Settings.CONFIGURATION.load();

		// Items
		itemPartWire = new ItemWire(Settings.getNextItemID());
		itemMultimeter = new ItemMultimeter(Settings.getNextItemID());
		itemTransformer = new ItemTransformer(Settings.getNextItemID());

		// Blocks
		blockTesla = new BlockTesla(Settings.getNextBlockID());
		blockBattery = new BlockBattery(Settings.getNextBlockID());

		Settings.CONFIGURATION.save();

		GameRegistry.registerItem(itemMultimeter, itemMultimeter.getUnlocalizedName());
		GameRegistry.registerItem(itemTransformer, itemTransformer.getUnlocalizedName());
		GameRegistry.registerBlock(blockTesla, blockTesla.getUnlocalizedName());
		GameRegistry.registerBlock(blockBattery, ItemBlockBattery.class, blockBattery.getUnlocalizedName());

		// Tiles
		GameRegistry.registerTileEntity(TileTesla.class, blockTesla.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileBattery.class, blockBattery.getUnlocalizedName());

		/**
		 * Set reference itemstacks
		 */
		ResonantInductionTabs.ITEMSTACK = new ItemStack(blockBattery);

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
	}
}
