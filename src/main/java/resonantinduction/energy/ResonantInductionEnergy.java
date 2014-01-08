package resonantinduction.energy;

import ic2.api.item.Items;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.modstats.Modstats;

import resonantinduction.Reference;
import resonantinduction.core.MultipartRI;
import resonantinduction.core.PacketMultiPart;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import resonantinduction.core.multimeter.ItemMultimeter;
import resonantinduction.energy.battery.BlockBattery;
import resonantinduction.energy.battery.ItemBlockBattery;
import resonantinduction.energy.battery.TileBattery;
import resonantinduction.energy.tesla.BlockTesla;
import resonantinduction.energy.tesla.TileTesla;
import resonantinduction.energy.transformer.ItemTransformer;
import resonantinduction.energy.wire.EnumWireMaterial;
import resonantinduction.energy.wire.ItemWire;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.network.PacketTile;
import calclavia.lib.recipe.UniversalRecipe;
import calclavia.lib.utility.LanguageUtility;
import codechicken.lib.colour.ColourRGBA;
import cpw.mods.fml.common.FMLLog;
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
 * The core module of Resonant Induction
 * 
 * @author Calclavia
 * 
 */
@Mod(modid = ResonantInductionEnergy.ID, name = Reference.NAME, version = Reference.VERSION, dependencies = "required-after:ResonantInduction|Core")
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class ResonantInductionEnergy
{
	/**
	 * Mod Information
	 */
	public static final String ID = "ResonantInduction|Energy";

	@Instance(ID)
	public static ResonantInductionEnergy INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.energy.ClientProxy", serverSide = "resonantinduction.energy.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	// Items
	private static Item itemPartWire;
	public static Item itemMultimeter;
	public static Item itemTransformer;

	// Blocks
	public static Block blockTesla, blockBattery;

	/**
	 * Packets
	 */
	public static final PacketTile PACKET_TILE = new PacketTile(Reference.CHANNEL);
	public static final PacketMultiPart PACKET_MULTIPART = new PacketMultiPart(Reference.CHANNEL);
	public static final ColourRGBA[] DYE_COLORS = new ColourRGBA[] { new ColourRGBA(255, 255, 255, 255), new ColourRGBA(1, 0, 0, 1d), new ColourRGBA(0, 0.608, 0.232, 1d), new ColourRGBA(0.588, 0.294, 0, 1d), new ColourRGBA(0, 0, 1, 1d), new ColourRGBA(0.5, 0, 05, 1d), new ColourRGBA(0, 1, 1, 1d), new ColourRGBA(0.8, 0.8, 0.8, 1d), new ColourRGBA(0.3, 0.3, 0.3, 1d), new ColourRGBA(1, 0.412, 0.706, 1d), new ColourRGBA(0.616, 1, 0, 1d), new ColourRGBA(1, 1, 0, 1d), new ColourRGBA(0.46f, 0.932, 1, 1d), new ColourRGBA(0.5, 0.2, 0.5, 1d), new ColourRGBA(0.7, 0.5, 0.1, 1d), new ColourRGBA(1, 1, 1, 1d) };

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		ResonantInduction.LOGGER.setParent(FMLLog.getLogger());
		NetworkRegistry.instance().registerGuiHandler(this, ResonantInductionEnergy.proxy);
		Modstats.instance().getReporter().registerMod(this);
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

		ResonantInductionEnergy.proxy.preInit();

		/**
		 * Set reference itemstacks
		 */
		ResonantInductionTabs.ITEMSTACK = new ItemStack(blockBattery);

		for (EnumWireMaterial material : EnumWireMaterial.values())
		{
			material.setWire(itemPartWire);
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		metadata.modId = ID;
		metadata.name = Reference.NAME;
		metadata.description = LanguageUtility.getLocal("meta.resonantinduction.description");
		metadata.url = "http://calclavia.com/resonant-induction";
		metadata.logoFile = "ri_logo.png";
		metadata.version = Reference.VERSION + "." + Reference.BUILD_VERSION;
		metadata.authorList = Arrays.asList(new String[] { "Calclavia", "DarkCow" });
		metadata.credits = LanguageUtility.getLocal("meta.resonantinduction.credits");
		metadata.autogenerated = false;

		MultipartRI.INSTANCE = new MultipartRI();
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
		ResonantInductionEnergy.proxy.postInit();
	}
}
