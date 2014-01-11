package resonantinduction.core;

import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import resonantinduction.core.handler.FluidEventHandler;
import resonantinduction.core.handler.LinkEventHandler;
import resonantinduction.core.prefab.part.PacketMultiPart;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.fluid.BlockFluidMixture;
import resonantinduction.core.resource.fluid.TileFluidMixture;
import resonantinduction.core.resource.item.ItemDust;
import calclavia.lib.multiblock.link.BlockMulti;
import calclavia.lib.multiblock.link.TileMultiBlockPart;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.network.PacketTile;
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

	/**
	 * Blocks and Items
	 */
	public static BlockMulti blockMulti;

	public static Block blockOre;
	public static ItemDust itemDust;
	public static Block blockFluidMixture;
	public static Block blockGas;

	public static Fluid MIXTURE = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		ResonantInduction.LOGGER.setParent(FMLLog.getLogger());
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		Modstats.instance().getReporter().registerMod(this);
		Settings.load();

		// Register Forge Events
		MinecraftForge.EVENT_BUS.register(ResourceGenerator.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new LinkEventHandler());
		MinecraftForge.EVENT_BUS.register(new FluidEventHandler());

		blockMulti = new BlockMulti(Settings.getNextBlockID()).setPacketType(PACKET_TILE);

		MIXTURE = new Fluid("mixture");
		FluidRegistry.registerFluid(MIXTURE);
		blockFluidMixture = new BlockFluidMixture(Settings.getNextBlockID(), MIXTURE);

		// Items
		itemDust = new ItemDust(Settings.getNextItemID());
		GameRegistry.registerItem(itemDust, itemDust.getUnlocalizedName());

		GameRegistry.registerTileEntity(TileMultiBlockPart.class, "TileEntityMultiBlockPart");
		GameRegistry.registerBlock(blockMulti, "blockMulti");

		GameRegistry.registerBlock(blockFluidMixture, blockFluidMixture.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileFluidMixture.class, blockFluidMixture.getUnlocalizedName());

		
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
		// Generate Dusts
		ResourceGenerator.generateDusts();
		Settings.save();
	}

}
