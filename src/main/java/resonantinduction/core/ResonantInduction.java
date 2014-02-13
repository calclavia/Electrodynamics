package resonantinduction.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import resonantinduction.core.handler.TextureHookHandler;
import resonantinduction.core.prefab.part.PacketMultiPart;
import resonantinduction.core.resource.BlockDust;
import resonantinduction.core.resource.BlockMachineMaterial;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.TileMaterial;
import resonantinduction.core.resource.fluid.TileFluidMixture;
import resonantinduction.core.resource.item.ItemOreResource;
import calclavia.components.tool.ToolMode;
import calclavia.components.tool.ToolModeLink;
import calclavia.lib.content.ContentRegistry;
import calclavia.lib.network.PacketAnnotation;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.network.PacketTile;
import calclavia.lib.prefab.item.ItemBlockMetadata;
import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.common.FMLLog;
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
 */
@Mod(modid = ResonantInduction.ID, name = ResonantInduction.NAME, version = Reference.VERSION, dependencies = "required-after:CalclaviaCore;before:IC2")
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
@ModstatInfo(prefix = "resonantin")
public class ResonantInduction
{
	/** Mod Information */
	public static final String ID = "ResonantInduction|Core";
	public static final String NAME = Reference.NAME + " Core";

	@Instance(ID)
	public static ResonantInduction INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.core.ClientProxy", serverSide = "resonantinduction.core.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final Logger LOGGER = Logger.getLogger(Reference.NAME);

	/** Packets */
	public static final PacketTile PACKET_TILE = new PacketTile(Reference.CHANNEL);
	public static final PacketMultiPart PACKET_MULTIPART = new PacketMultiPart(Reference.CHANNEL);
	public static final PacketAnnotation PACKET_ANNOTATION = new PacketAnnotation(Reference.CHANNEL);

	/** Blocks and Items */
	public static Block blockOre;
	public static ItemOreResource itemRubble;
	public static ItemOreResource itemDust;
	public static ItemOreResource itemRefinedDust;
	public static Block blockDust;
	public static final List<Block> blockFluidMixtures = new ArrayList<Block>();
	public static final List<Block> blockFluidMaterials = new ArrayList<Block>();
	public static Block blockMachinePart;

	public static List<Fluid> fluidMixtures = new ArrayList<Fluid>();
	public static List<Fluid> fluidMaterials = new ArrayList<Fluid>();

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		ResonantInduction.LOGGER.setParent(FMLLog.getLogger());
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		Modstats.instance().getReporter().registerMod(this);
		ToolMode.REGISTRY.add(new ToolModeLink());

		Settings.load();

		// Register Forge Events
		MinecraftForge.EVENT_BUS.register(ResourceGenerator.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new TextureHookHandler());

		blockMachinePart = contentRegistry.createBlock(BlockMachineMaterial.class, ItemBlockMetadata.class);

		/**
		 * Melting dusts
		 */
		blockDust = contentRegistry.createTile(BlockDust.class, TileMaterial.class);

		// Items
		itemRubble = new ItemOreResource(Settings.getNextItemID("oreRubble"), "oreRubble");
		itemDust = new ItemOreResource(Settings.getNextItemID("oreDust"), "oreDust");
		itemRefinedDust = new ItemOreResource(Settings.getNextItemID("oreRefinedDust"), "oreRefinedDust");

		GameRegistry.registerItem(itemRubble, itemRubble.getUnlocalizedName());
		GameRegistry.registerItem(itemDust, itemDust.getUnlocalizedName());
		GameRegistry.registerItem(itemRefinedDust, itemRefinedDust.getUnlocalizedName());

		// Already registered wih ContentRegistry
		// GameRegistry.registerTileEntity(TileMaterial.class, "ri_material");
		GameRegistry.registerTileEntity(TileFluidMixture.class, "ri_fluid_mixture");
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		// Load Languages
		ResonantInduction.LOGGER.fine("Languages Loaded:" + LanguageUtility.loadLanguages(Reference.LANGUAGE_DIRECTORY, Reference.LANGUAGES));
		// Set Mod Metadata
		Settings.setModMetadata(metadata, ID, NAME);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		// Generate Resources
		ResourceGenerator.generateOreResources();
		Settings.save();
	}
}
