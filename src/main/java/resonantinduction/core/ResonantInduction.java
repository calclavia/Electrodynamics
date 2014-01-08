package resonantinduction.core;

import ic2.api.item.Items;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Icon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import resonantinduction.Reference;
import resonantinduction.core.multimeter.ItemMultimeter;
import resonantinduction.energy.CommonProxy;
import resonantinduction.energy.LinkEvent;
import resonantinduction.energy.ResonantInductionEnergy;
import resonantinduction.energy.battery.BlockBattery;
import resonantinduction.energy.battery.ItemBlockBattery;
import resonantinduction.energy.battery.TileBattery;
import resonantinduction.energy.tesla.BlockTesla;
import resonantinduction.energy.tesla.TileTesla;
import resonantinduction.energy.transformer.ItemTransformer;
import resonantinduction.energy.wire.EnumWireMaterial;
import resonantinduction.energy.wire.ItemWire;
import resonantinduction.mechanics.BlockMachinePart;
import resonantinduction.mechanics.furnace.BlockAdvancedFurnace;
import resonantinduction.mechanics.furnace.TileAdvancedFurnace;
import resonantinduction.mechanics.grinder.BlockGrinderWheel;
import resonantinduction.mechanics.grinder.TileGrinderWheel;
import resonantinduction.mechanics.grinder.TilePurifier;
import resonantinduction.mechanics.item.ItemDust;
import resonantinduction.mechanics.liquid.BlockFluidMixture;
import resonantinduction.mechanics.liquid.TileFluidMixture;
import resonantinduction.mechanics.purifier.BlockPurifier;
import resonantinduction.transport.levitator.BlockLevitator;
import resonantinduction.transport.levitator.ItemBlockContractor;
import resonantinduction.transport.levitator.TileEMLevitator;
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
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The core module of Resonant Induction
 * 
 * @author Calclavia
 * 
 */
@Mod(modid = ResonantInduction.ID, name = Reference.NAME, version = Reference.VERSION, dependencies = "required-after:CalclaviaCore;before:ThermalExpansion;before:IC2")
@NetworkMod(channels = ResonantInduction.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
@ModstatInfo(prefix = "resonantin")
public class ResonantInduction
{
	/**
	 * Mod Information
	 */
	public static final String ID = "ResonantInduction|Core";
	public static final String CHANNEL = "RESIND";

	@Instance(ID)
	public static ResonantInduction INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.core.ClientProxy", serverSide = "resonantinduction.core.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final Logger LOGGER = Logger.getLogger(Reference.NAME);

	/**
	 * Packets
	 */
	public static final PacketTile PACKET_TILE = new PacketTile(CHANNEL);
	public static final PacketMultiPart PACKET_MULTIPART = new PacketMultiPart(CHANNEL);
	public static final ColourRGBA[] DYE_COLORS = new ColourRGBA[] { new ColourRGBA(255, 255, 255, 255), new ColourRGBA(1, 0, 0, 1d), new ColourRGBA(0, 0.608, 0.232, 1d), new ColourRGBA(0.588, 0.294, 0, 1d), new ColourRGBA(0, 0, 1, 1d), new ColourRGBA(0.5, 0, 05, 1d), new ColourRGBA(0, 1, 1, 1d), new ColourRGBA(0.8, 0.8, 0.8, 1d), new ColourRGBA(0.3, 0.3, 0.3, 1d), new ColourRGBA(1, 0.412, 0.706, 1d), new ColourRGBA(0.616, 1, 0, 1d), new ColourRGBA(1, 1, 0, 1d), new ColourRGBA(0.46f, 0.932, 1, 1d), new ColourRGBA(0.5, 0.2, 0.5, 1d), new ColourRGBA(0.7, 0.5, 0.1, 1d), new ColourRGBA(1, 1, 1, 1d) };

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		ResonantInduction.LOGGER.setParent(FMLLog.getLogger());
		NetworkRegistry.instance().registerGuiHandler(this, ResonantInduction.proxy);
		Modstats.instance().getReporter().registerMod(this);

		/**
		 * Set reference itemstacks
		 */
		//ResonantInductionTabs.ITEMSTACK = new ItemStack(null);
		MinecraftForge.EVENT_BUS.register(new LinkEvent());
		Settings.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		ResonantInduction.LOGGER.fine("Languages Loaded:" + LanguageUtility.loadLanguages(Reference.LANGUAGE_DIRECTORY, Reference.LANGUAGES));
		
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
}
