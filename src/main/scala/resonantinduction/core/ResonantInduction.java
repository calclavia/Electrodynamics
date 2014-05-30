package resonantinduction.core;

import java.util.HashMap;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.modstats.ModstatInfo;
import org.modstats.Modstats;

import resonant.api.recipe.MachineRecipes;
import resonant.lib.config.ConfigHandler;
import resonant.lib.content.ContentRegistry;
import resonant.lib.network.PacketAnnotation;
import resonant.lib.network.PacketHandler;
import resonant.lib.network.PacketTile;
import resonant.lib.prefab.item.ItemBlockMetadata;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.core.blocks.BlockIndustrialStone;
import resonantinduction.core.blocks.IndustrialStoneBlocksRecipes;
import resonantinduction.core.handler.TextureHookHandler;
import resonantinduction.core.items.ItemDevStaff;
import resonantinduction.core.prefab.part.PacketMultiPart;
import resonantinduction.core.resource.BlockDust;
import resonantinduction.core.resource.ItemBiomass;
import resonantinduction.core.resource.ItemOreResource;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.TileDust;
import resonantinduction.core.resource.fluid.ItemOreResourceBucket;
import resonantinduction.core.resource.fluid.TileFluidMixture;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import resonantinduction.core.items.ItemFlour;

/** The core module of Resonant Induction
 * 
 * @author Calclavia */
@Mod(modid = ResonantInduction.ID, name = ResonantInduction.NAME, version = Reference.VERSION, dependencies = "required-after:ForgeMultipart@[1.0.0.244,);required-after:ResonantEngine;before:ThermalExpansion;before:Mekanism")
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
@ModstatInfo(prefix = "resonantin")
public class ResonantInduction
{
    /** Mod Information */
    public static final String ID = "ResonantInduction|Core";
    public static final String NAME = Reference.NAME;
    public static final Logger LOGGER = Logger.getLogger(Reference.NAME);
    /** Packets */
    public static final PacketTile PACKET_TILE = new PacketTile(Reference.CHANNEL);
    public static final PacketMultiPart PACKET_MULTIPART = new PacketMultiPart(Reference.CHANNEL);
    public static final PacketAnnotation PACKET_ANNOTATION = new PacketAnnotation(Reference.CHANNEL);
    public static final HashMap<Integer, BlockFluidFinite> blockMixtureFluids = new HashMap<Integer, BlockFluidFinite>();
    public static final HashMap<Integer, BlockFluidFinite> blockMoltenFluid = new HashMap<Integer, BlockFluidFinite>();
    public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID).setPrefix(Reference.PREFIX).setTab(TabRI.CORE);
    @Instance(ID)
    public static ResonantInduction INSTANCE;
    @SidedProxy(clientSide = "resonantinduction.core.ClientProxy", serverSide = "resonantinduction.core.CommonProxy")
    public static CommonProxy proxy;
    @Mod.Metadata(ID)
    public static ModMetadata metadata;
    /** Blocks and Items */
    public static Block blockOre;
    public static ItemOreResource itemRubble, itemDust, itemRefinedDust;
    public static ItemOreResourceBucket itemBucketMixture, itemBucketMolten;
    public static Item itemBiomass, itemDevStaff;
    public static Item itemFlour, itemBakingTrayWithBread;
    public static Block blockDust, blockRefinedDust;
    
	public static Block blockIndustrialStone;
    

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        ResonantInduction.LOGGER.setParent(FMLLog.getLogger());
        NetworkRegistry.instance().registerGuiHandler(this, proxy);
        Modstats.instance().getReporter().registerMod(this);
        Settings.CONFIGURATION.load();
        // Register Forge Events
        MinecraftForge.EVENT_BUS.register(ResourceGenerator.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new TextureHookHandler());

        blockIndustrialStone = contentRegistry.createBlock(BlockIndustrialStone.class, ItemBlockMetadata.class);

        /** Melting dusts */
        blockDust = contentRegistry.createBlock("dust", BlockDust.class, null, TileDust.class).setCreativeTab(null);
        blockRefinedDust = contentRegistry.createBlock("refinedDust", BlockDust.class, null, TileDust.class).setCreativeTab(null);

        // Items
        itemRubble = new ItemOreResource(Settings.getNextItemID("oreRubble"), "oreRubble");
        itemDust = new ItemOreResource(Settings.getNextItemID("oreDust"), "oreDust");
        itemRefinedDust = new ItemOreResource(Settings.getNextItemID("oreRefinedDust"), "oreRefinedDust");
        itemBucketMixture = new ItemOreResourceBucket(Settings.getNextItemID("bucketMixture"), "bucketMixture", false);
        itemBucketMolten = new ItemOreResourceBucket(Settings.getNextItemID("bucketMolten"), "bucketMolten", true);
        itemBiomass = contentRegistry.createItem(ItemBiomass.class);
        itemDevStaff = contentRegistry.createItem(ItemDevStaff.class);
        itemFlour = contentRegistry.createItem(ItemFlour.class);


        GameRegistry.registerItem(itemRubble, itemRubble.getUnlocalizedName());
        GameRegistry.registerItem(itemDust, itemDust.getUnlocalizedName());
        GameRegistry.registerItem(itemRefinedDust, itemRefinedDust.getUnlocalizedName());
        GameRegistry.registerItem(itemBucketMixture, itemBucketMixture.getUnlocalizedName());
        GameRegistry.registerItem(itemBucketMolten, itemBucketMolten.getUnlocalizedName());

        // Already registered with ContentRegistry
        // GameRegistry.registerTileEntity(TileMaterial.class, "ri_material");
        GameRegistry.registerTileEntity(TileFluidMixture.class, "ri_fluid_mixture");

        proxy.preInit();
        TabRI.ITEMSTACK = new ItemStack(blockIndustrialStone);
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        // Load Languages
        ResonantInduction.LOGGER.fine("Languages Loaded:" + LanguageUtility.loadLanguages(Reference.LANGUAGE_DIRECTORY, Reference.LANGUAGES));
        // Set Mod Metadata
        Settings.setModMetadata(metadata, ID, NAME);
        proxy.init();
        
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        ConfigHandler.configure(Settings.CONFIGURATION, "resonantinduction");

        // Generate Resources
        ResourceGenerator.generateOreResources();
        // These are from zeros flour addition
		GameRegistry.addRecipe(new ShapelessOreRecipe(itemFlour, new Object []{Item.wheat,Item.wheat}));
		FurnaceRecipes.smelting().addSmelting(itemFlour.itemID, 1, new ItemStack (Item.bread), 50f);
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack (itemFlour ,1,1),new Object []{itemFlour, Item.bucketWater}));
		MachineRecipes.INSTANCE.addRecipe(RecipeType.GRINDER.name(), Item.wheat, itemFlour);
        proxy.postInit();
        Settings.CONFIGURATION.save();
        IndustrialStoneBlocksRecipes.init();
        
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Setup command
        CommandMachine commandSentry = new CommandMachine();
        MinecraftForge.EVENT_BUS.register(commandSentry);

        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(commandSentry);

    }

    /** Recipe Types */
    public static enum RecipeType
    {
        CRUSHER,
        GRINDER,
        MIXER,
        SMELTER,
        SAWMILL;
    }

}
