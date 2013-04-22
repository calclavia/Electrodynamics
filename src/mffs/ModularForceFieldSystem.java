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
import mffs.item.ItemFortron;
import mffs.item.ItemRemoteController;
import mffs.item.card.ItemCardFrequency;
import mffs.item.card.ItemCardID;
import mffs.item.card.ItemCardInfinite;
import mffs.item.card.ItemCardLink;
import mffs.item.mode.ItemMode;
import mffs.item.mode.ItemModeCube;
import mffs.item.mode.ItemModeSphere;
import mffs.item.mode.ItemModeTube;
import mffs.item.module.ItemModule;
import mffs.item.module.projector.ItemModePyramid;
import mffs.item.module.projector.ItemModuleFusion;
import mffs.item.module.projector.ItemModuleManipulator;
import mffs.item.module.projector.ItemModuleShock;
import mffs.item.module.projector.ItemModuleSponge;
import mffs.item.module.projector.ItemModuleStablize;
import mffs.tileentity.TileEntityCoercionDeriver;
import mffs.tileentity.TileEntityForceField;
import mffs.tileentity.TileEntityForceFieldProjector;
import mffs.tileentity.TileEntityFortronCapacitor;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import universalelectricity.prefab.CustomDamageSource;
import universalelectricity.prefab.TranslationHelper;
import universalelectricity.prefab.network.PacketManager;
import calclavia.lib.UniversalRecipes;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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

	@Instance(ID)
	public static ModularForceFieldSystem instance;
	@Mod.Metadata(ID)
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
	public static Item itemRemoteController;
	public static Item itemFocusMatix;

	/**
	 * Cards
	 */
	public static ItemCard itemCardBlank, itemCardInfinite, itemCardFrequency, itemCardID,
			itemCardLink;

	/**
	 * Modes
	 */
	public static ItemMode itemModeCube, itemModeSphere, itemModeTube, itemModeCylinder,
			itemModePyramid;
	/**
	 * Modules
	 */
	// General Modules
	public static ItemModule itemModule, itemModuleSpeed, itemModuleCapacity, itemModuleTranslate,
			itemModuleScale, itemModuleRotate;

	// Projector Modules
	public static ItemModule itemModuleFusion, itemModuleManipulator, itemModuleCamouflage,
			itemModuleDisintegration, itemModuleShock, itemModuleGlow, itemModuleSponge,
			itemModuleStablize;

	// Defense Station Modules
	public static ItemModule itemModuleAntiHostile, itemModuleAntiFriendly,
			itemModuleAntiPersonnel, itemModuleConfiscate, itemModuleWarn, itemModuleBlockAccess,
			itemModuleBlockAlter;

	public static DamageSource damagefieldShock = new CustomDamageSource("fieldShock").setDamageBypassesArmor();

	@PreInit
	public void preInit(FMLPreInitializationEvent event)
	{
		/**
		 * General Registery
		 */
		LOGGER.setParent(FMLLog.getLogger());
		Modstats.instance().getReporter().registerMod(this);
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		MinecraftForge.EVENT_BUS.register(this);

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
		 * Items
		 */
		itemRemoteController = new ItemRemoteController(Settings.getNextItemID());
		itemFocusMatix = new ItemBase(Settings.getNextItemID(), "focusMatrix");

		/**
		 * Modes
		 */
		itemModeCube = new ItemModeCube(Settings.getNextItemID());
		itemModeSphere = new ItemModeSphere(Settings.getNextItemID());
		itemModeTube = new ItemModeTube(Settings.getNextItemID());
		itemModePyramid = new ItemModePyramid(Settings.getNextItemID());
		// itemModeCylinder = new ItemModeCube(Settings.getNextItemID());

		/**
		 * Modules
		 */
		itemModuleTranslate = new ItemModule(Settings.getNextItemID(), "moduleTranslate").setCost(1.5f);
		itemModuleScale = new ItemModule(Settings.getNextItemID(), "moduleScale").setCost(1.2f);
		itemModuleRotate = new ItemModule(Settings.getNextItemID(), "moduleRotate").setCost(0.1f);

		itemModuleSpeed = new ItemModule(Settings.getNextItemID(), "moduleSpeed").setCost(0.2f);
		itemModuleCapacity = new ItemModule(Settings.getNextItemID(), "moduleCapacity").setCost(0.4f);

		// Force Field Projector Modules
		itemModuleFusion = new ItemModuleFusion(Settings.getNextItemID());
		itemModuleManipulator = new ItemModuleManipulator(Settings.getNextItemID());
		itemModuleCamouflage = new ItemModule(Settings.getNextItemID(), "moduleCamouflage").setCost(1.5f).setMaxStackSize(1);
		itemModuleDisintegration = new ItemModule(Settings.getNextItemID(), "moduleDisintegration").setCost(2f).setMaxStackSize(1);
		itemModuleShock = new ItemModuleShock(Settings.getNextItemID());
		itemModuleGlow = new ItemModule(Settings.getNextItemID(), "moduleGlow");
		itemModuleSponge = new ItemModuleSponge(Settings.getNextItemID());
		itemModuleStablize = new ItemModuleStablize(Settings.getNextItemID());

		/**
		 * Cards
		 */
		itemCardBlank = new ItemCard(Settings.getNextItemID(), "cardBlank");
		itemCardFrequency = new ItemCardFrequency(Settings.getNextItemID());
		itemCardLink = new ItemCardLink(Settings.getNextItemID());
		itemCardID = new ItemCardID(Settings.getNextItemID());

		/**
		 * The Fortron Liquid
		 */
		itemFortron = new ItemFortron(Settings.getNextItemID());
		FortronHelper.LIQUID_FORTRON = LiquidDictionary.getOrCreateLiquid("Fortron", new LiquidStack(itemFortron, 0));

		itemCardBlank = new ItemCard(Settings.getNextItemID(), "cardBlank");
		itemCardInfinite = new ItemCardInfinite(Settings.getNextItemID());

		Settings.CONFIGURATION.save();

		GameRegistry.registerBlock(blockForceField, blockForceField.getUnlocalizedName());
		GameRegistry.registerBlock(blockCoercionDeriver, blockCoercionDeriver.getUnlocalizedName());
		GameRegistry.registerBlock(blockFortronCapacitor, blockFortronCapacitor.getUnlocalizedName());
		GameRegistry.registerBlock(blockForceFieldProjector, blockForceFieldProjector.getUnlocalizedName());

		GameRegistry.registerTileEntity(TileEntityForceField.class, blockForceField.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityCoercionDeriver.class, blockCoercionDeriver.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityFortronCapacitor.class, blockFortronCapacitor.getUnlocalizedName());
		GameRegistry.registerTileEntity(TileEntityForceFieldProjector.class, blockForceFieldProjector.getUnlocalizedName());

		proxy.preInit();
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		if (event.map == Minecraft.getMinecraft().renderEngine.textureMapItems)
		{
			LiquidDictionary.getCanonicalLiquid("Fortron").setRenderingIcon(itemFortron.getIconFromDamage(0)).setTextureSheet("/gui/items.png");
		}
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

		/**
		 * Add recipes
		 */
		UniversalRecipes.init();

		// -- General Items --
		// Focus Matrix
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemFocusMatix, 5), "RMR", "MDM", "RMR", 'M', UniversalRecipes.PRIMARY_METAL, 'D', Item.diamond, 'R', Item.redstone));

		// Remote Controller
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemRemoteController), "WWW", "MCM", "MCM", 'W', UniversalRecipes.WIRE, 'C', UniversalRecipes.BATTERY, 'M', UniversalRecipes.PRIMARY_METAL));

		// -- Machines --
		// Coercion Deriver
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCoercionDeriver), "M M", "MFM", "MCM", 'C', UniversalRecipes.BATTERY, 'M', UniversalRecipes.PRIMARY_METAL, 'F', itemFocusMatix));
		// Fortron Capacitor
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockFortronCapacitor), "MFM", "FCF", "MFM", 'D', Item.diamond, 'C', UniversalRecipes.BATTERY, 'F', itemFocusMatix, 'M', UniversalRecipes.PRIMARY_METAL));
		// Force Field Projector
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockForceFieldProjector), " D ", "FFF", "MCM", 'D', Item.diamond, 'C', UniversalRecipes.BATTERY, 'F', itemFocusMatix, 'M', UniversalRecipes.PRIMARY_METAL));

		// -- Cards --
		// Blank
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardBlank), "PPP", "PMP", "PPP", 'P', Item.paper, 'M', UniversalRecipes.PRIMARY_METAL));
		// Link
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardLink), "BWB", 'B', itemCardBlank, 'W', UniversalRecipes.WIRE));
		// Frequency
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardFrequency), "WBW", 'B', itemCardBlank, 'W', UniversalRecipes.WIRE));
		// ID
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardID), "RBR", 'B', itemCardBlank, 'R', Item.redstone));

		// -- Modes --
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeSphere), " F ", "FFF", " F ", 'F', itemFocusMatix));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeCube), "FFF", "FFF", "FFF", 'F', itemFocusMatix));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeTube), "FFF", "   ", "FFF", 'F', itemFocusMatix));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModePyramid), "F  ", "FF ", "FFF", 'F', itemFocusMatix));

		// -- Modules --
		// -- -- General -- --
		// Speed
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSpeed), "F", "R", "F", 'F', itemFocusMatix, 'R', Item.redstone));
		// Capacity
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCapacity, 2), "FCF", 'F', itemFocusMatix, 'C', UniversalRecipes.BATTERY));
		// Shock
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleShock), "FWF", 'F', itemFocusMatix, 'W', UniversalRecipes.WIRE));
		// Sponge
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSponge), "BBB", "BFB", "BBB", 'F', itemFocusMatix, 'B', Item.bucketWater));
		// Disintegration
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleDisintegration), " W ", "FBF", " W ", 'F', itemFocusMatix, 'W', UniversalRecipes.WIRE, 'B', UniversalRecipes.BATTERY));
		// Manipulator
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleManipulator), "F", " ", "F", 'F', itemFocusMatix));
		// Camouflage
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCamouflage), "WFW", "FWF", "WFW", 'F', itemFocusMatix, 'W', Block.cloth));
		// Fusion
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleFusion), "FJF", 'F', itemFocusMatix, 'J', itemModuleShock));
		// Scale
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleScale), "FRF", 'F', itemFocusMatix));
		// Translate
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleTranslate), "FSF", 'F', itemFocusMatix, 'S', itemModuleScale));
		// Rotate
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleRotate), "F  ", " F ", "  F", 'F', itemFocusMatix));
		// Glow
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleGlow, 4), "GGG", "GFG", "GGG", 'F', itemFocusMatix, 'G', Block.glowStone));
		// Stabilizer
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleStablize), "FFF", "PSA", "FFF", 'F', itemFocusMatix, 'P', Item.pickaxeDiamond, 'S', Item.shovelDiamond, 'A', Item.axeDiamond));

		proxy.init();
	}

	@ForgeSubscribe
	public void playerInteractEvent(PlayerInteractEvent evt)
	{
		if (evt.action == Action.RIGHT_CLICK_BLOCK || evt.action == Action.LEFT_CLICK_BLOCK)
		{
			/**
			 * Disable block breaking of force fields.
			 */
			if (evt.action == Action.LEFT_CLICK_BLOCK && evt.entityPlayer.worldObj.getBlockId(evt.x, evt.y, evt.z) == blockForceField.blockID)
			{
				evt.setCanceled(true);
				return;
			}
		}
	}
}
