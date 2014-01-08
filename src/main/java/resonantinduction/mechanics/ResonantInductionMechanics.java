package resonantinduction.mechanics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.Icon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.modstats.Modstats;

import resonantinduction.Reference;
import resonantinduction.core.MultipartRI;
import resonantinduction.core.PacketMultiPart;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.mechanics.furnace.BlockAdvancedFurnace;
import resonantinduction.mechanics.furnace.TileAdvancedFurnace;
import resonantinduction.mechanics.grinder.BlockGrinderWheel;
import resonantinduction.mechanics.grinder.TileGrinderWheel;
import resonantinduction.mechanics.grinder.TilePurifier;
import resonantinduction.mechanics.item.ItemDust;
import resonantinduction.mechanics.liquid.BlockFluidMixture;
import resonantinduction.mechanics.liquid.TileFluidMixture;
import resonantinduction.mechanics.purifier.BlockPurifier;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.network.PacketTile;
import calclavia.lib.utility.LanguageUtility;
import codechicken.lib.colour.ColourRGBA;
import cpw.mods.fml.common.FMLLog;
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
@Mod(modid = ResonantInductionMechanics.ID, name = Reference.NAME, version = Reference.VERSION, dependencies = "required-after:ResonantInduction|Core")
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class ResonantInductionMechanics
{
	/**
	 * Mod Information
	 */
	public static final String ID = "ResonantInduction|Mechanics";

	@Instance(ID)
	public static ResonantInductionMechanics INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.mechanics.ClientProxy", serverSide = "resonantinduction.mechanics.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	// Items

	/**
	 * Machines
	 */
	public static Item itemDust;

	// Blocks
	public static Block blockAdvancedFurnace, blockMachinePart, blockGrinderWheel, blockPurifier,
			blockFluidMixture;

	public static Fluid MIXTURE;

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
		NetworkRegistry.instance().registerGuiHandler(this, ResonantInductionMechanics.proxy);
		Modstats.instance().getReporter().registerMod(this);
		Settings.CONFIGURATION.load();

		// Items
		itemDust = new ItemDust(Settings.getNextItemID());

		// Blocks
		blockMachinePart = new BlockMachinePart(Settings.getNextBlockID());
		blockGrinderWheel = new BlockGrinderWheel(Settings.getNextBlockID());
		blockPurifier = new BlockPurifier(Settings.getNextBlockID());

		MIXTURE = new Fluid("mixture");
		FluidRegistry.registerFluid(MIXTURE);
		blockFluidMixture = new BlockFluidMixture(Settings.getNextBlockID(), MIXTURE);

		if (Settings.REPLACE_FURNACE)
		{
			blockAdvancedFurnace = BlockAdvancedFurnace.createNew(false);
			GameRegistry.registerBlock(blockAdvancedFurnace, "ri_" + blockAdvancedFurnace.getUnlocalizedName());
			GameRegistry.registerTileEntity(TileAdvancedFurnace.class, "ri_" + blockAdvancedFurnace.getUnlocalizedName());
		}

		Settings.CONFIGURATION.save();
		GameRegistry.registerItem(itemDust, itemDust.getUnlocalizedName());

		GameRegistry.registerBlock(blockGrinderWheel, blockGrinderWheel.getUnlocalizedName());
		GameRegistry.registerBlock(blockPurifier, blockPurifier.getUnlocalizedName());
		GameRegistry.registerBlock(blockFluidMixture, blockFluidMixture.getUnlocalizedName());
		GameRegistry.registerBlock(blockMachinePart, blockMachinePart.getUnlocalizedName());

		// Tiles
		GameRegistry.registerTileEntity(TilePurifier.class, blockPurifier.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileGrinderWheel.class, blockGrinderWheel.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileFluidMixture.class, blockFluidMixture.getUnlocalizedName());

		ResonantInductionMechanics.proxy.preInit();
		MinecraftForge.EVENT_BUS.register(itemDust);
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
		/** Auto-gen dusts */
		ResourceGenerator.generateDusts();
		ResonantInductionMechanics.proxy.postInit();

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

	public static final HashMap<String, Icon> fluidIconMap = new HashMap<String, Icon>();

	public void registerIcon(String name, TextureStitchEvent.Pre event)
	{
		fluidIconMap.put(name, event.map.registerIcon(name));
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void preTextureHook(TextureStitchEvent.Pre event)
	{
		if (event.map.textureType == 0)
		{
			registerIcon(Reference.PREFIX + "mixture", event);
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		MIXTURE.setIcons(fluidIconMap.get(Reference.PREFIX + "mixture"));
	}

}
