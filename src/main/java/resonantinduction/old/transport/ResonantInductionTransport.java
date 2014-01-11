package resonantinduction.old.transport;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

import org.modstats.Modstats;

import resonantinduction.archaic.blocks.BlockColorGlass;
import resonantinduction.archaic.blocks.BlockColorGlowGlass;
import resonantinduction.archaic.blocks.BlockColorSand;
import resonantinduction.archaic.blocks.BlockTurntable;
import resonantinduction.archaic.blocks.ItemBlockColored;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.debug.BlockDebug;
import resonantinduction.core.network.PacketIDTile;
import resonantinduction.core.resource.BlockGasOre;
import resonantinduction.core.resource.BlockOre;
import resonantinduction.core.resource.BlockOre.OreData;
import resonantinduction.core.resource.GasOreGenerator;
import resonantinduction.core.resource.item.ItemBlockOre;
import resonantinduction.core.resource.item.ItemParts;
import resonantinduction.core.resource.item.ItemParts.Parts;
import resonantinduction.core.tilenetwork.prefab.NetworkUpdateHandler;
import resonantinduction.electrical.armbot.BlockArmbot;
import resonantinduction.electrical.armbot.command.TaskBreak;
import resonantinduction.electrical.armbot.command.TaskDrop;
import resonantinduction.electrical.armbot.command.TaskEnd;
import resonantinduction.electrical.armbot.command.TaskFire;
import resonantinduction.electrical.armbot.command.TaskGOTO;
import resonantinduction.electrical.armbot.command.TaskGive;
import resonantinduction.electrical.armbot.command.TaskGrabEntity;
import resonantinduction.electrical.armbot.command.TaskGrabItem;
import resonantinduction.electrical.armbot.command.TaskHarvest;
import resonantinduction.electrical.armbot.command.TaskIF;
import resonantinduction.electrical.armbot.command.TaskIdle;
import resonantinduction.electrical.armbot.command.TaskPlace;
import resonantinduction.electrical.armbot.command.TaskReturn;
import resonantinduction.electrical.armbot.command.TaskRotateBy;
import resonantinduction.electrical.armbot.command.TaskRotateTo;
import resonantinduction.electrical.armbot.command.TaskStart;
import resonantinduction.electrical.armbot.command.TaskTake;
import resonantinduction.electrical.armbot.command.TaskUse;
import resonantinduction.electrical.generator.solar.BlockSolarPanel;
import resonantinduction.electrical.multimeter.ItemReadoutTools;
import resonantinduction.mechanical.belt.BlockConveyorBelt;
import resonantinduction.mechanical.fluid.BlockKitchenSink;
import resonantinduction.mechanical.fluid.BlockReleaseValve;
import resonantinduction.mechanical.fluid.EnumGas;
import resonantinduction.mechanical.fluid.ItemFluidCan;
import resonantinduction.mechanical.fluid.pipes.BlockPipe;
import resonantinduction.mechanical.fluid.pipes.FluidPartsMaterial;
import resonantinduction.mechanical.fluid.pipes.ItemBlockPipe;
import resonantinduction.mechanical.fluid.pump.BlockConstructionPump;
import resonantinduction.mechanical.fluid.pump.BlockDrain;
import resonantinduction.mechanical.fluid.pump.BlockPumpMachine;
import resonantinduction.mechanical.fluid.tank.BlockTank;
import resonantinduction.old.api.coding.TaskRegistry;
import resonantinduction.old.core.ItemOreDirv;
import resonantinduction.old.core.misc.BehaviorDispenseEgg;
import resonantinduction.old.core.misc.EntityFarmEgg;
import resonantinduction.old.core.misc.EnumBird;
import resonantinduction.old.core.misc.ItemColoredDust;
import resonantinduction.old.core.misc.ItemCommonTool;
import resonantinduction.old.core.misc.ItemFarmEgg;
import resonantinduction.old.core.recipe.RecipeLoader;
import resonantinduction.old.mechanics.processor.BlockProcessor;
import resonantinduction.old.transport.crate.BlockCrate;
import resonantinduction.old.transport.crate.ItemBlockCrate;
import resonantinduction.old.transport.encoder.BlockEncoder;
import resonantinduction.old.transport.encoder.ItemDisk;
import resonantinduction.old.transport.hopper.BlockAdvancedHopper;
import resonantinduction.old.transport.imprinter.BlockImprinter;
import resonantinduction.old.transport.imprinter.ItemImprinter;
import resonantinduction.old.transport.logistic.BlockDetector;
import resonantinduction.old.transport.logistic.BlockManipulator;
import resonantinduction.old.transport.logistic.BlockRejector;
import calclavia.lib.content.ContentRegistry;
import calclavia.lib.ore.OreGenReplaceStone;
import calclavia.lib.ore.OreGenerator;
import calclavia.lib.prefab.item.ItemBlockHolder;
import calclavia.lib.utility.FluidHelper;
import calclavia.lib.utility.PlayerKeyHandler;
import calclavia.lib.utility.SaveManager;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import dark.lib.EnumMaterial;
import dark.lib.EnumOrePart;
import dark.lib.LaserEntityDamageSource;

//@Mod(modid = ResonantInductionTransport.MOD_ID, name = ResonantInductionTransport.MOD_NAME, version = ResonantInductionTransport.VERSION, useMetadata = true)
//@NetworkMod(channels = { ResonantInductionTransport.CHANNEL }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class ResonantInductionTransport
{
	public static final String TEXTURE_DIRECTORY = "textures/";
	public static final String BLOCK_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	public static final String ITEM_DIRECTORY = TEXTURE_DIRECTORY + "items/";
	public static final String MODEL_DIRECTORY = TEXTURE_DIRECTORY + "models/";
	public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
	public static final String CHANNEL = "ALChannel";

	// @Mod
	public static final String MOD_ID = "AssemblyLine";
	public static final String MOD_NAME = "Assembly Line";

	public static final String DOMAIN = "al";
	public static final String PREFIX = DOMAIN + ":";

	public static String DIRECTORY_NO_SLASH = "assets/" + DOMAIN + "/";
	public static String DIRECTORY = "/" + DIRECTORY_NO_SLASH;
	public static String LANGUAGE_PATH = DIRECTORY + "languages/";
	public static String SOUND_PATH = DIRECTORY + "audio/";

	// @SidedProxy(clientSide = "com.builtbroken.assemblyline.client.ClientProxy", serverSide =
	// "com.builtbroken.assemblyline.CommonProxy")
	public static CommonProxy proxy;

	@Instance(ResonantInductionTransport.MOD_ID)
	public static ResonantInductionTransport instance;

	@Metadata(ResonantInductionTransport.MOD_ID)
	public static ModMetadata meta;

	private static final String[] LANGUAGES_SUPPORTED = new String[] { "en_US", "de_DE" };

	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "AssemblyLine.cfg"));

	public static Logger FMLog = Logger.getLogger(ResonantInductionTransport.MOD_NAME);

	public static boolean VINALLA_RECIPES = false;

	public static int entitiesIds = 60;

	private static PacketIDTile tilePacket;

	public static PacketIDTile getTilePacket()
	{
		if (tilePacket == null)
		{
			tilePacket = new PacketIDTile(ResonantInductionTransport.CHANNEL);
		}
		return tilePacket;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		CONFIGURATION.load();

		DarkCore.instance().preLoad();
		Modstats.instance().getReporter().registerMod(this);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new FluidHelper());
		MinecraftForge.EVENT_BUS.register(SaveManager.instance());
		TickRegistry.registerTickHandler(NetworkUpdateHandler.instance(), Side.SERVER);
		TickRegistry.registerScheduledTickHandler(new PlayerKeyHandler(ResonantInductionTransport.CHANNEL), Side.CLIENT);
		MinecraftForge.EVENT_BUS.register(new LaserEntityDamageSource(null));
		NetworkRegistry.instance().registerGuiHandler(this, proxy);

		TaskRegistry.registerCommand(new TaskDrop());
		TaskRegistry.registerCommand(new TaskGive());
		TaskRegistry.registerCommand(new TaskTake());
		TaskRegistry.registerCommand(new TaskGrabItem());
		TaskRegistry.registerCommand(new TaskGrabEntity());
		TaskRegistry.registerCommand(new TaskRotateBy());
		TaskRegistry.registerCommand(new TaskRotateTo());
		TaskRegistry.registerCommand(new TaskUse());
		TaskRegistry.registerCommand(new TaskIF());
		TaskRegistry.registerCommand(new TaskGOTO());
		TaskRegistry.registerCommand(new TaskReturn());
		TaskRegistry.registerCommand(new TaskEnd());
		TaskRegistry.registerCommand(new TaskFire());
		TaskRegistry.registerCommand(new TaskHarvest());
		TaskRegistry.registerCommand(new TaskPlace());
		TaskRegistry.registerCommand(new TaskBreak());
		TaskRegistry.registerCommand(new TaskStart());
		TaskRegistry.registerCommand(new TaskIdle());

		this.registerObjects();
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		DarkCore.instance().Load();

		FMLog.info("Loaded: " + TranslationHelper.loadLanguages(LANGUAGE_PATH, LANGUAGES_SUPPORTED) + " languages.");

		for (EnumGas gas : EnumGas.values())
		{
			FluidRegistry.registerFluid(gas.getGas());
		}
		if (RecipeLoader.blockGas != null)
		{
			EnumGas.NATURAL_GAS.getGas().setBlockID(RecipeLoader.blockGas);
		}
		if (RecipeLoader.blockGas != null)
		{
			GameRegistry.registerWorldGenerator(new GasOreGenerator());
		}
		if (RecipeLoader.blockOre != null)
		{
			for (OreData data : OreData.values())
			{
				if (data.doWorldGen)
				{
					OreGenReplaceStone gen = data.getGeneratorSettings();
					if (gen != null)
					{
						OreGenerator.addOre(gen);
					}
				}
			}
		}
		if (RecipeLoader.itemParts != null)
		{
			for (Parts part : Parts.values())
			{
				OreDictionary.registerOre(part.name, new ItemStack(RecipeLoader.itemParts, 1, part.ordinal()));
			}
		}
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		DarkCore.instance().postLoad();
		proxy.postInit();
		RecipeLoader.instance().loadRecipes();
		CONFIGURATION.save();
	}

	/** Separated method for registering & creating objects */
	public void registerObjects()
	{

		/* BLOCKS */
		RecipeLoader.blockManipulator = ContentRegistry.createNewBlock("Manipulator", ResonantInductionTransport.MOD_ID, BlockManipulator.class);
		RecipeLoader.blockCrate = (BlockCrate) ContentRegistry.createNewBlock("Crate", ResonantInductionTransport.MOD_ID, BlockCrate.class, ItemBlockCrate.class);
		RecipeLoader.blockImprinter = ContentRegistry.createNewBlock("Imprinter", ResonantInductionTransport.MOD_ID, BlockImprinter.class);
		RecipeLoader.blockDetector = ContentRegistry.createNewBlock("Detector", ResonantInductionTransport.MOD_ID, BlockDetector.class);

		RecipeLoader.blockRejector = ContentRegistry.createNewBlock("Rejector", ResonantInductionTransport.MOD_ID, BlockRejector.class);
		RecipeLoader.blockEncoder = ContentRegistry.createNewBlock("Encoder", ResonantInductionTransport.MOD_ID, BlockEncoder.class);
		RecipeLoader.blockArmbot = ContentRegistry.createNewBlock("Armbot", ResonantInductionTransport.MOD_ID, BlockArmbot.class);
		RecipeLoader.blockTurntable = ContentRegistry.createNewBlock("Turntable", ResonantInductionTransport.MOD_ID, BlockTurntable.class);
		RecipeLoader.processorMachine = ContentRegistry.createNewBlock("ALBlockProcessor", ResonantInductionTransport.MOD_ID, BlockProcessor.class, ItemBlockHolder.class);

		RecipeLoader.blockAdvancedHopper = ContentRegistry.createNewBlock("ALBlockHopper", ResonantInductionTransport.MOD_ID, BlockAdvancedHopper.class, ItemBlockHolder.class);
		RecipeLoader.blockPipe = ContentRegistry.createNewBlock("FMBlockPipe", ResonantInductionTransport.MOD_ID, BlockPipe.class, ItemBlockPipe.class);
		RecipeLoader.blockPumpMachine = ContentRegistry.createNewBlock("FMBlockPump", ResonantInductionTransport.MOD_ID, BlockPumpMachine.class, ItemBlockHolder.class);
		RecipeLoader.blockReleaseValve = ContentRegistry.createNewBlock("FMBlockReleaseValve", ResonantInductionTransport.MOD_ID, BlockReleaseValve.class, ItemBlockHolder.class);
		RecipeLoader.blockTank = ContentRegistry.createNewBlock("FMBlockTank", ResonantInductionTransport.MOD_ID, BlockTank.class, ItemBlockPipe.class);

		RecipeLoader.blockSink = ContentRegistry.createNewBlock("FMBlockSink", ResonantInductionTransport.MOD_ID, BlockKitchenSink.class, ItemBlockHolder.class);
		RecipeLoader.blockDrain = ContentRegistry.createNewBlock("FMBlockDrain", ResonantInductionTransport.MOD_ID, BlockDrain.class, ItemBlockHolder.class);
		RecipeLoader.blockConPump = ContentRegistry.createNewBlock("FMBlockConstructionPump", ResonantInductionTransport.MOD_ID, BlockConstructionPump.class, ItemBlockHolder.class);
		RecipeLoader.blockSteamGen = ContentRegistry.createNewBlock("DMBlockSteamMachine", ResonantInductionTransport.MOD_ID, BlockSmallSteamGen.class, ItemBlockHolder.class);
		RecipeLoader.blockOre = ContentRegistry.createNewBlock("DMBlockOre", ResonantInductionTransport.MOD_ID, BlockOre.class, ItemBlockOre.class);

		RecipeLoader.blockWire = ContentRegistry.createNewBlock("DMBlockWire", ResonantInductionTransport.MOD_ID, BlockWire.class, ItemBlockWire.class);
		RecipeLoader.blockDebug = ContentRegistry.createNewBlock("DMBlockDebug", ResonantInductionTransport.MOD_ID, BlockDebug.class, ItemBlockHolder.class);
		RecipeLoader.blockStainGlass = ContentRegistry.createNewBlock("DMBlockStainedGlass", ResonantInductionTransport.MOD_ID, BlockColorGlass.class, ItemBlockColored.class);
		RecipeLoader.blockColorSand = ContentRegistry.createNewBlock("DMBlockColorSand", ResonantInductionTransport.MOD_ID, BlockColorSand.class, ItemBlockColored.class);
		RecipeLoader.blockBasalt = ContentRegistry.createNewBlock("DMBlockBasalt", ResonantInductionTransport.MOD_ID, BlockBasalt.class, ItemBlockColored.class);

		RecipeLoader.blockGlowGlass = ContentRegistry.createNewBlock("DMBlockGlowGlass", ResonantInductionTransport.MOD_ID, BlockColorGlowGlass.class, ItemBlockColored.class);
		RecipeLoader.blockSolar = ContentRegistry.createNewBlock("DMBlockSolar", ResonantInductionTransport.MOD_ID, BlockSolarPanel.class, ItemBlockHolder.class);
		RecipeLoader.blockGas = ContentRegistry.createNewBlock("DMBlockGas", ResonantInductionTransport.MOD_ID, BlockGasOre.class, ItemBlockHolder.class);
		RecipeLoader.blockBatBox = ContentRegistry.createNewBlock("DMBlockBatBox", ResonantInductionTransport.MOD_ID, BlockBatteryBox.class, ItemBlockEnergyStorage.class);

		/* ITEMS */
		RecipeLoader.itemTool = ContentRegistry.createNewItem("DMReadoutTools", ResonantInductionTransport.MOD_ID, ItemReadoutTools.class, true);
		RecipeLoader.battery = ContentRegistry.createNewItem("DMItemBattery", ResonantInductionTransport.MOD_ID, ItemBattery.class, true);
		RecipeLoader.wrench = ContentRegistry.createNewItem("DMWrench", ResonantInductionTransport.MOD_ID, ItemWrench.class, true);
		RecipeLoader.itemGlowingSand = ContentRegistry.createNewItem("DMItemGlowingSand", ResonantInductionTransport.MOD_ID, ItemColoredDust.class, true);
		RecipeLoader.itemDiggingTool = ContentRegistry.createNewItem("ItemDiggingTools", ResonantInductionTransport.MOD_ID, ItemCommonTool.class, true);

		RecipeLoader.itemVehicleTest = ContentRegistry.createNewItem("ItemVehicleTest", ResonantInductionTransport.MOD_ID, ItemVehicleSpawn.class, true);
		RecipeLoader.itemImprint = new ItemImprinter(CONFIGURATION.getItem("Imprint", DarkCore.getNextItemId()).getInt());
		RecipeLoader.itemDisk = new ItemDisk(CONFIGURATION.getItem("Disk", DarkCore.getNextItemId()).getInt());
		RecipeLoader.itemFluidCan = ContentRegistry.createNewItem("ItemFluidCan", ResonantInductionTransport.MOD_ID, ItemFluidCan.class, true);
		RecipeLoader.itemParts = ContentRegistry.createNewItem("DMCraftingParts", ResonantInductionTransport.MOD_ID, ItemParts.class, true);

		RecipeLoader.itemMetals = ContentRegistry.createNewItem("DMOreDirvParts", ResonantInductionTransport.MOD_ID, ItemOreDirv.class, true);
		// ALRecipeLoader.itemMPWire = CoreRegistry.createNewItem("DMMPWire", AssemblyLine.MOD_ID,
		// ItemWire.class, true);

		TileEntityAssembly.refresh_diff = CONFIGURATION.get("TileSettings", "RefreshRandomRange", 9, "n = value of config, 1 + n, random number range from 1 to n that will be added to the lowest refresh value").getInt();
		TileEntityAssembly.refresh_min_rate = CONFIGURATION.get("TileSettings", "RefreshLowestValue", 20, "Lowest value the refresh rate of the tile network will be").getInt();

		// Entities
		if (ResonantInductionTransport.CONFIGURATION.get("Override", "Eggs", true).getBoolean(true))
		{
			Item.itemsList[Item.egg.itemID] = null;
			Item.egg = null;
			Item.egg = new ItemFarmEgg(88);
			GameRegistry.registerItem(Item.egg, "FTEgg", MOD_ID);
			EntityRegistry.registerGlobalEntityID(EntityFarmEgg.class, "FarmEgg", EntityRegistry.findGlobalUniqueEntityId());
			EntityRegistry.registerModEntity(EntityFarmEgg.class, "FarmEgg", entitiesIds++, this, 64, 1, true);
			BlockDispenser.dispenseBehaviorRegistry.putObject(Item.egg, new BehaviorDispenseEgg());
		}

		EntityRegistry.registerGlobalEntityID(EntityTestCar.class, "TestCar", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerModEntity(EntityTestCar.class, "TestCar", 60, this, 64, 1, true);

		for (EnumBird bird : EnumBird.values())
		{
			if (bird != EnumBird.VANILLA_CHICKEN && CONFIGURATION.get("Entities", "Enable_" + bird.name(), true).getBoolean(true))
			{
				bird.register();
			}
		}
		// Post object creation, normally creative tab icon setup
		if (RecipeLoader.blockPipe != null)
		{
			ResonantInductionTabs.tabHydraulic().setIconItemStack(FluidPartsMaterial.IRON.getStack());
		}
		else
		{
			ResonantInductionTabs.tabHydraulic().setIconItemStack(new ItemStack(Item.bucketWater));
		}
		if (RecipeLoader.itemMetals != null)
		{
			ResonantInductionTabs.tabIndustrial().itemStack = EnumMaterial.getStack(RecipeLoader.itemMetals, EnumMaterial.IRON, EnumOrePart.GEARS, 1);
			RecipeLoader.parseOreNames(CONFIGURATION);
		}
		else
		{

		}
		if (RecipeLoader.blockConveyorBelt != null)
		{
			ResonantInductionTabs.tabAutomation().setIconItemStack(new ItemStack(RecipeLoader.blockConveyorBelt));
		}
		else
		{
			ResonantInductionTabs.tabAutomation().setIconItemStack(new ItemStack(Block.pistonStickyBase));
		}

	}

	public void loadModMeta()
	{
		meta.modId = ResonantInductionTransport.MOD_ID;
		meta.name = ResonantInductionTransport.MOD_NAME;
		meta.version = ResonantInductionTransport.VERSION;
		meta.description = "Simi Realistic factory system for minecraft bring in conveyor belts, robotic arms, and simple machines";
		meta.url = "http://www.universalelectricity.com/coremachine";
		meta.logoFile = "/al_logo.png";

		meta.authorList = Arrays.asList(new String[] { "DarkGuardsman" });
		meta.credits = "Archadia - Developer" + "LiQuiD - Dev of BioTech\n" + "Hangcow - Ex-Dev Greater Security\n" + "Calclavia - Ex-CoDev of assembly line\n" + "Briman0094 - Ex-CoDev of assembly line\n" + "Elrath18 - Colored Glass, Sand, & Stone\n" + "Doppelgangerous - Researcher\n" + "Freesound.org - Sound effects\n" + "MineMan1(wdtod) - asset creation\n" + "AlphaToOmega - asset creation\n" + "pinksheep - asset creation\n" + "X-wing9 - asset creation\n" + "Azkhare - asset creation\n" + "Vexatos - German Translation\n" + "crafteverywhere - Chinese Translations\n" + "PancakeCandy - French & Dutch Translations\n";
		meta.autogenerated = false;

	}
}
