package mffs;

import java.util.Arrays;
import java.util.logging.Logger;

import mffs.base.BlockBase;
import mffs.base.BlockMachine;
import mffs.base.ItemBase;
import mffs.block.BlockCoercionDeriver;
import mffs.block.BlockForceField;
import mffs.block.BlockForceFieldProjector;
import mffs.block.BlockFortronCapacitor;
import mffs.card.ItemCard;
import mffs.fortron.FortronHelper;
import mffs.item.card.ItemCardFrequency;
import mffs.item.card.ItemCardInfinite;
import mffs.item.mode.ItemMode;
import mffs.item.mode.ItemModeCube;
import mffs.item.module.ItemModule;
import mffs.item.module.ItemModuleRotate;
import mffs.item.module.ItemModuleScale;
import mffs.item.module.ItemModuleTranslate;
import mffs.tileentity.TileEntityForceField;
import mffs.tileentity.TileEntityForceFieldProjector;
import mffs.tileentity.TileEntityFortronCapacitor;
import net.minecraft.item.Item;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import universalelectricity.prefab.TranslationHelper;
import universalelectricity.prefab.network.PacketManager;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = ModularForceFieldSystem.ID, name = ModularForceFieldSystem.NAME, version = ModularForceFieldSystem.VERSION, useMetadata = true)
@NetworkMod(clientSideRequired = true, channels = { ModularForceFieldSystem.CHANNEL }, packetHandler = PacketManager.class)
@ModstatInfo(prefix = "mffs")
public class ModularForceFieldSystem
{
	/**
	 * General Variable Definition
	 */
	public static final String CHANNEL = "MFFS";
	public static final String ID = "ModularForceFieldSystem";
	public static final String NAME = "Modular Force Field System";
	public static final String PREFIX = "mffs:";
	public static final String MAJOR_VERSION = "@MAJOR@";
	public static final String MINOR_VERSION = "@MINOR@";
	public static final String REVISION_VERSION = "@REVIS@";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;
	public static final String BUILD_VERSION = "@BUILD@";

	@Instance(ModularForceFieldSystem.ID)
	public static ModularForceFieldSystem instance;
	@Mod.Metadata(ModularForceFieldSystem.ID)
	public static ModMetadata metadata;
	@SidedProxy(clientSide = "mffs.ClientProxy", serverSide = "mffs.CommonProxy")
	public static CommonProxy proxy;

	public static final Logger LOGGER = Logger.getLogger(NAME);

	/**
	 * Directories Definition
	 */
	public static final String RESOURCE_DIRECTORY = "/mods/mffs/";
	public static final String LANGUAGE_DIRECTORY = RESOURCE_DIRECTORY + "languages/";
	public static final String TEXTURE_DIRECTORY = RESOURCE_DIRECTORY + "textures/";
	public static final String BLOCK_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	public static final String ITEM_DIRECTORY = TEXTURE_DIRECTORY + "items/";
	public static final String MODEL_DIRECTORY = TEXTURE_DIRECTORY + "models/";
	public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
	public static final String GUI_BASE_DIRECTORY = GUI_DIRECTORY + "gui_base.png";
	public static final String GUI_COMPONENTS = GUI_DIRECTORY + "gui_components.png";
	public static final String GUI_BUTTON = GUI_DIRECTORY + "gui_button.png";

	/**
	 * Machines
	 */
	public static BlockMachine blockCoercionDeriver, blockFortronCapacitor,
			blockForceFieldProjector, blockBiometricIdentifier, blockDefenseStation;

	public static BlockBase blockForceField;

	/**
	 * Items
	 */
	public static Item itemFortron;

	/**
	 * Cards
	 */
	public static ItemCard itemCardBlank, itemCardInfinite, itemCardFrequency, itemCardID,
			itemCardLink;

	/**
	 * Modes
	 */
	public static ItemMode itemModeCube, itemModeSphere, itemModeTube, itemModePyramid;
	/**
	 * Modules
	 */
	// General Modules
	public static ItemModule itemModule, itemModuleSpeed, itemModuleCapacity, itemModuleTranslate,
			itemModuleScale, itemModuleRotate;

	// Projector Modules
	public static ItemModule itemModuleShock, itemModuleSponge, itemModuleManipulator,
			itemModuleDisintegration, itemModuleJammer, itemModuleFusion, itemModuleGlow,
			itemModuleStablize, itemModuleCamouflage;

	// Defense Station Modules
	public static ItemModule itemModuleAntiHostile, itemModuleAntiFriendly,
			itemModuleAntiPersonnel, itemModuleConfiscate, itemModuleWarn, itemModuleBlockAccess,
			itemModuleBlockAlter;

	@PreInit
	public void preInit(FMLPreInitializationEvent event)
	{
		/**
		 * General Registery
		 */
		LOGGER.setParent(FMLLog.getLogger());
		Modstats.instance().getReporter().registerMod(this);
		NetworkRegistry.instance().registerGuiHandler(this, ModularForceFieldSystem.proxy);

		Settings.load();

		/**
		 * Start instantiating blocks and items.
		 */
		Settings.CONFIGURATION.load();

		/**
		 * Blocks
		 */
		blockForceField = new BlockForceField(Settings.getNextBlockID());
		blockCoercionDeriver = new BlockCoercionDeriver(Settings.getNextBlockID());
		blockFortronCapacitor = new BlockFortronCapacitor(Settings.getNextBlockID());
		blockForceFieldProjector = new BlockForceFieldProjector(Settings.getNextBlockID());

		/**
		 * Modes
		 */
		itemModeCube = new ItemModeCube(Settings.getNextItemID());

		/**
		 * Modules
		 */
		itemModuleTranslate = new ItemModuleTranslate(Settings.getNextItemID());
		itemModuleScale = new ItemModuleScale(Settings.getNextItemID());
		itemModuleRotate = new ItemModuleRotate(Settings.getNextItemID());

		/**
		 * Cards
		 */
		itemCardFrequency = new ItemCardFrequency(Settings.getNextItemID());

		/**
		 * The Fortron Liquid
		 */
		itemFortron = new ItemBase(Settings.getNextItemID(), "fortron").setCreativeTab(null);
		FortronHelper.LIQUID_FORTRON = LiquidDictionary.getOrCreateLiquid("Fortron", new LiquidStack(itemFortron, 0));

		itemCardBlank = new ItemCard(Settings.getNextItemID(), "cardBlank");
		itemCardInfinite = new ItemCardInfinite(Settings.getNextItemID());

		Settings.CONFIGURATION.save();

		GameRegistry.registerBlock(blockForceField, blockForceField.getUnlocalizedName2());
		GameRegistry.registerBlock(blockFortronCapacitor, blockFortronCapacitor.getUnlocalizedName2());
		GameRegistry.registerBlock(blockForceFieldProjector, blockForceFieldProjector.getUnlocalizedName2());

		GameRegistry.registerTileEntity(TileEntityForceField.class, blockForceField.getUnlocalizedName2());
		GameRegistry.registerTileEntity(TileEntityFortronCapacitor.class, blockFortronCapacitor.getUnlocalizedName2());
		GameRegistry.registerTileEntity(TileEntityForceFieldProjector.class, blockForceFieldProjector.getUnlocalizedName2());

		proxy.preInit();
	}

	@Init
	public void load(FMLInitializationEvent evt)
	{
		/**
		 * Load language file(s)
		 */
		LOGGER.fine("Language(s) Loaded: " + TranslationHelper.loadLanguages(LANGUAGE_DIRECTORY, new String[] { "en_US" }));

		/**
		 * Write metadata information
		 */
		metadata.modId = ID;
		metadata.name = NAME;
		metadata.description = "Modular Force Field System is a mod that adds force fields, high tech machinery and defensive measures to Minecraft.";
		metadata.url = "http://www.universalelectricity.com/mffs/";
		metadata.logoFile = "/mffs_logo.png";
		metadata.version = VERSION + "." + BUILD_VERSION;
		metadata.authorList = Arrays.asList(new String[] { "Calclavia" });
		metadata.credits = "Please visit the website.";
		metadata.autogenerated = false;

		proxy.init();
	}
}
