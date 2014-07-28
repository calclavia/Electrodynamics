package resonantinduction.atomic;

import cpw.mods.fml.common.eventhandler.Event;
import ic2.api.item.IC2Items;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.EnumHelper;
import resonant.content.loader.ModManager;
import resonant.engine.content.debug.TileCreativeBuilder;
import resonant.lib.network.discriminator.PacketAnnotation;
import resonant.lib.network.discriminator.PacketAnnotationManager;
import resonantinduction.atomic.blocks.BlockToxicWaste;
import resonantinduction.atomic.blocks.BlockUraniumOre;
import resonantinduction.atomic.blocks.TileElectromagnet;
import resonantinduction.atomic.items.*;
import resonantinduction.atomic.machine.extractor.turbine.TileElectricTurbine;
import resonantinduction.atomic.machine.extractor.turbine.TileFunnel;
import resonantinduction.atomic.machine.plasma.BlockPlasmaHeater;
import resonantinduction.atomic.machine.plasma.TilePlasma;
import resonantinduction.atomic.machine.quantum.TileQuantumAssembler;
import resonantinduction.atomic.machine.reactor.TileReactorCell;
import resonantinduction.atomic.schematic.SchematicBreedingReactor;
import universalelectricity.core.transform.vector.VectorWorld;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import resonant.api.IElectromagnet;
import resonant.api.event.PlasmaEvent.SpawnPlasmaEvent;
import resonant.api.recipe.QuantumAssemblerRecipes;
import resonant.core.content.debug.BlockCreativeBuilder;
import resonant.lib.prefab.block.BlockRadioactive;
import resonant.lib.prefab.ore.OreGenBase;
import resonant.lib.prefab.ore.OreGenReplaceStone;
import resonant.lib.prefab.ore.OreGenerator;
import resonant.lib.recipe.UniversalRecipe;
import resonant.lib.render.RenderUtility;
import resonantinduction.atomic.blocks.TileSiren;
import resonantinduction.atomic.machine.accelerator.EntityParticle;
import resonantinduction.atomic.machine.accelerator.TileAccelerator;
import resonantinduction.atomic.machine.centrifuge.TileCentrifuge;
import resonantinduction.atomic.machine.plasma.TilePlasmaHeater;
import resonantinduction.atomic.machine.reactor.TileControlRod;
import resonantinduction.atomic.machine.thermometer.TileThermometer;
import resonantinduction.atomic.schematic.SchematicAccelerator;
import resonantinduction.atomic.schematic.SchematicFissionReactor;
import resonantinduction.atomic.schematic.SchematicFusionReactor;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantTab;
import resonantinduction.core.Settings;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Atomic.ID, name = Atomic.NAME, version = Reference.VERSION, dependencies = "required-after:ResonantEngine;after:IC2;after:ResonantInduction|Electrical;required-after:" + ResonantInduction.ID)
public class Atomic
{
    public static final String ID = "ResonantInduction|Atomic";
    public static final String TEXTURE_DIRECTORY = "textures/";
    public static final String GUI_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
    public static final int ENTITY_ID_PREFIX = 49;
    public static final int SECOND_IN_TICKS = 20;
    public static final String BAN_ANTIMATTER_POWER = FlagRegistry.registerFlag("ban_antimatter_power");
    public static final String NAME = Reference.name() + " Atomic";
    public static final ModManager contentRegistry = new ModManager().setPrefix(Reference.prefix()).setTab(ResonantTab.tab());
    private static final String[] SUPPORTED_LANGUAGES = new String[]
    { "en_US", "pl_PL", "de_DE", "ru_RU" };

    @Instance(ID)
    public static Atomic INSTANCE;

    @SidedProxy(clientSide = "ClientProxy", serverSide = "CommonProxy")
    public static CommonProxy proxy;

    @Mod.Metadata(ID)
    public static ModMetadata metadata;

    /** Block and Items */
    public static Block blockRadioactive;
    public static Block blockCentrifuge;
    public static Block blockElectricTurbine;
    public static Block blockNuclearBoiler;
    public static Block blockControlRod;
    public static Block blockThermometer;
    public static Block blockFusionCore;
    public static Block blockPlasma;
    public static Block blockElectromagnet;
    public static Block blockChemicalExtractor;
    public static Block blockSiren;
    public static Block blockSteamFunnel;
    public static Block blockAccelerator;
    public static Block blockFulmination;
    public static Block blockQuantumAssembler;
    public static Block blockReactorCell;

    public static Block blockUraniumOre;

    //items
    public static Item itemCell, itemFissileFuel, itemBreedingRod, itemDarkMatter, itemAntimatter, itemDeuteriumCell, itemTritiumCell, itemWaterCell;
    public static Item itemBucketToxic;
    public static Item itemYellowCake;
    public static Item itemUranium;
    public static Item itemHazmatTop;
    public static Item itemHazmatBody;
    public static Item itemHazmatLeggings;
    public static Item itemHazmatBoots;

    /** Fluids */
    public static Block blockToxicWaste;

    /** Water, Uranium Hexafluoride, Steam, Deuterium, Toxic waste */
    public static FluidStack FLUIDSTACK_WATER, FLUIDSTACK_URANIUM_HEXAFLOURIDE, FLUIDSTACK_STEAM, FLUIDSTACK_DEUTERIUM, FLUIDSTACK_TRITIUM, FLUIDSTACK_TOXIC_WASTE;
    public static Fluid FLUID_URANIUM_HEXAFLOURIDE, FLUID_PLASMA, FLUID_STEAM, FLUID_DEUTERIUM, FLUID_TRITIUM, FLUID_TOXIC_WASTE;
    public static OreGenBase uraniumOreGeneration;

    /** Is this ItemStack a cell?
     * 
     * @param itemStack
     * @return */
    public static boolean isItemStackEmptyCell(ItemStack itemStack)
    {
        return isItemStackOreDictionaryCompatible(itemStack, "cellEmpty");
    }

    public static boolean isItemStackWaterCell(ItemStack itemStack)
    {
        return isItemStackOreDictionaryCompatible(itemStack, "cellWater");
    }

    public static boolean isItemStackUraniumOre(ItemStack itemStack)
    {
        return isItemStackOreDictionaryCompatible(itemStack, "dropUranium", "oreUranium");
    }

    public static boolean isItemStackDeuteriumCell(ItemStack itemStack)
    {
        return isItemStackOreDictionaryCompatible(itemStack, "molecule_1d", "molecule_1h2", "cellDeuterium");
    }

    public static boolean isItemStackTritiumCell(ItemStack itemStack)
    {
        return isItemStackOreDictionaryCompatible(itemStack, "molecule_h3", "cellTritium");
    }

    /** Compare to Ore Dict
     * 
     * @param itemStack
     * @return */
    public static boolean isItemStackOreDictionaryCompatible(ItemStack itemStack, String... names)
    {
        if (itemStack != null && names != null && names.length > 0)
        {
            String name = OreDictionary.getOreName(OreDictionary.getOreID(itemStack));

            for (String compareName : names)
            {
                if (name.equals(compareName))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static int getFluidAmount(FluidStack fluid)
    {
        if (fluid != null)
        {
            return fluid.amount;
        }
        return 0;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);



        TileCreativeBuilder.register(new SchematicAccelerator());
        TileCreativeBuilder.register(new SchematicBreedingReactor());
        TileCreativeBuilder.register(new SchematicFissionReactor());
        TileCreativeBuilder.register(new SchematicFusionReactor());

        Settings.config.load();

        /** Registers Gases & Fluids */
        FLUID_URANIUM_HEXAFLOURIDE = new Fluid("uraniumhexafluoride").setGaseous(true);
        FLUID_STEAM = new Fluid("steam").setGaseous(true);
        FLUID_DEUTERIUM = new Fluid("deuterium").setGaseous(true);
        FLUID_TRITIUM = new Fluid("tritium").setGaseous(true);
        FLUID_TOXIC_WASTE = new Fluid("toxicwaste");
        FLUID_PLASMA = new Fluid("plasma").setGaseous(true);

        FluidRegistry.registerFluid(FLUID_URANIUM_HEXAFLOURIDE);
        FluidRegistry.registerFluid(FLUID_STEAM);
        FluidRegistry.registerFluid(FLUID_TRITIUM);
        FluidRegistry.registerFluid(FLUID_DEUTERIUM);
        FluidRegistry.registerFluid(FLUID_TOXIC_WASTE);
        FluidRegistry.registerFluid(FLUID_PLASMA);

        /** Fluid Stack Reference Initialization */
        FLUIDSTACK_WATER = new FluidStack(FluidRegistry.WATER, 0);
        FLUIDSTACK_URANIUM_HEXAFLOURIDE = new FluidStack(FLUID_URANIUM_HEXAFLOURIDE, 0);
        FLUIDSTACK_STEAM = new FluidStack(FluidRegistry.getFluidID("steam"), 0);
        FLUIDSTACK_DEUTERIUM = new FluidStack(FluidRegistry.getFluidID("deuterium"), 0);
        FLUIDSTACK_TRITIUM = new FluidStack(FluidRegistry.getFluidID("tritium"), 0);
        FLUIDSTACK_TOXIC_WASTE = new FluidStack(FluidRegistry.getFluidID("toxicwaste"), 0);

        /** Block Initiation */
        blockRadioactive = new BlockRadioactive().setUnlocalizedName(Reference.prefix() + "radioactive").setTextureName(Reference.prefix() + "radioactive").setCreativeTab(CreativeTabs.tabBlock);
        blockUraniumOre = new BlockUraniumOre();
        blockToxicWaste = new BlockToxicWaste().setCreativeTab(null);

        blockElectricTurbine = contentRegistry.newBlock(TileElectricTurbine.class);
        blockCentrifuge = contentRegistry.newBlock(TileCentrifuge.class);
        blockReactorCell = contentRegistry.newBlock(TileReactorCell.class);
        blockNuclearBoiler = contentRegistry.newBlock(TileNuclearBoiler.class);
        blockChemicalExtractor = contentRegistry.newBlock(TileChemicalExtractor.class);
        blockFusionCore = contentRegistry.newBlock(TilePlasmaHeater.class);
        blockControlRod = contentRegistry.newBlock(TileControlRod.class);
        blockThermometer = contentRegistry.newBlock(TileThermometer.class);
        blockPlasma = contentRegistry.newBlock(TilePlasma.class);
        blockElectromagnet = contentRegistry.newBlock(TileElectromagnet.class);
        blockSiren = contentRegistry.newBlock(TileSiren.class);
        blockSteamFunnel = contentRegistry.newBlock(TileFunnel.class);
        blockAccelerator = contentRegistry.newBlock(TileAccelerator.class);
        blockFulmination = contentRegistry.newBlock(TileFulmination.class);
        blockQuantumAssembler = contentRegistry.newBlock(TileQuantumAssembler.class);


        /** Items */
        itemHazmatTop = new ItemHazmat("HazmatMask", 0);
        itemHazmatBody = new ItemHazmat("HazmatBody", 1);
        itemHazmatLeggings = new ItemHazmat("HazmatLeggings", 2);
        itemHazmatBoots = new ItemHazmat("HazmatBoots", 3);
        itemCell = new Item().setUnlocalizedName("cellEmpty");
        itemFissileFuel = new ItemFissileFuel().setUnlocalizedName("rodFissileFuel");
        itemDeuteriumCell = new ItemCell().setUnlocalizedName("cellDeuterium");
        itemTritiumCell = new ItemCell().setUnlocalizedName("cellTritium");
        itemWaterCell = new ItemCell().setUnlocalizedName("cellWater");
        itemDarkMatter = new ItemDarkMatter().setUnlocalizedName("darkMatter");
        itemAntimatter = new ItemAntimatter().setUnlocalizedName("antimatter");
        itemBreedingRod = new ItemBreederFuel().setUnlocalizedName("rodBreederFuel");
        itemYellowCake = new ItemRadioactive().setUnlocalizedName("yellowcake");
        itemUranium = contentRegistry.newItem(ItemUranium.class);

        /** Fluid Item Initialization */
        FLUID_PLASMA.setBlockID(blockPlasma);

        int bucketID = Settings.getNextItemID();
        itemBucketToxic = (new ItemBucket(Settings.config.getItem("Toxic Waste Bucket", bucketID).getInt(bucketID), blockToxicWaste.blockID)).setCreativeTab(ResonantTab.DEFAULT).setUnlocalizedName(Reference.PREFIX + "bucketToxicWaste")
                .setContainerItem(Item.bucketEmpty).setTextureName(Reference.PREFIX + "bucketToxicWaste");

        FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("toxicwaste"), new ItemStack(itemBucketToxic), new ItemStack(Item.bucketEmpty));
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.WATER, new ItemStack(itemWaterCell), new ItemStack(itemCell));
        FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("deuterium"), 200), new ItemStack(itemDeuteriumCell), new ItemStack(itemCell));
        FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("tritium"), 200), new ItemStack(itemTritiumCell), new ItemStack(itemCell));

        if (OreDictionary.getOres("oreUranium").size() > 1 && Settings.config.get(Configuration.CATEGORY_GENERAL, "Auto Disable Uranium If Exist", false).getBoolean(false))
        {
            ResonantInduction.LOGGER.fine("Disabled Uranium Generation. Detected another uranium being generated: " + OreDictionary.getOres("oreUranium").size());
        }
        else
        {
            uraniumOreGeneration = new OreGenReplaceStone("Uranium Ore", "oreUranium", new ItemStack(blockUraniumOre), 0, 25, 9, 3, "pickaxe", 2);
            uraniumOreGeneration.enable(Settings.config);
            OreGenerator.addOre(uraniumOreGeneration);
            ResonantInduction.LOGGER.fine("Added Atomic Science uranium to ore generator.");
        }

        Settings.config.save();

        MinecraftForge.EVENT_BUS.register(itemAntimatter);
        MinecraftForge.EVENT_BUS.register(FulminationHandler.INSTANCE);

        /** Cell registry. */
        if (Settings.allowOreDictionaryCompatibility)
        {
            OreDictionary.registerOre("ingotUranium", itemUranium);
            OreDictionary.registerOre("dustUranium", itemYellowCake);
        }

        OreDictionary.registerOre("breederUranium", new ItemStack(itemUranium, 1, 1));
        OreDictionary.registerOre("blockRadioactive", blockRadioactive);

        OreDictionary.registerOre("cellEmpty", itemCell);
        OreDictionary.registerOre("cellUranium", itemFissileFuel);
        OreDictionary.registerOre("cellTritium", itemTritiumCell);
        OreDictionary.registerOre("cellDeuterium", itemDeuteriumCell);
        OreDictionary.registerOre("cellWater", itemWaterCell);
        OreDictionary.registerOre("strangeMatter", itemDarkMatter);
        OreDictionary.registerOre("antimatterMilligram", new ItemStack(itemAntimatter, 1, 0));
        OreDictionary.registerOre("antimatterGram", new ItemStack(itemAntimatter, 1, 1));

        ForgeChunkManager.setForcedChunkLoadingCallback(this, new LoadingCallback() {
            @Override
            public void ticketsLoaded(List<Ticket> tickets, World world) {
                for (Ticket ticket : tickets) {
                    if (ticket.getType() == Type.ENTITY) {
                        if (ticket.getEntity() != null) {
                            if (ticket.getEntity() instanceof EntityParticle) {
                                ((EntityParticle) ticket.getEntity()).updateTicket = ticket;
                            }
                        }
                    }
                }
            }
        });
        Settings.config.save();
        ResonantTab.ITEMSTACK = new ItemStack(blockReactorCell);
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        Settings.setModMetadata(metadata, ID, NAME, ResonantInduction.ID);
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        /** IC2 Recipes */
        if (Loader.isModLoaded("IC2") && Settings.allowAlternateRecipes())
        {
            OreDictionary.registerOre("cellEmpty", IC2Items.getItem("cell"));

            // Check to make sure we have actually registered the Ore, otherwise tell the user about
            // it.
            String cellEmptyName = OreDictionary.getOreName(OreDictionary.getOreID("cellEmpty"));
            if (cellEmptyName == "Unknown")
            {
                ResonantInduction.logger().info("Unable to register cellEmpty in OreDictionary!");
            }

            // IC2 exchangeable recipes
            GameRegistry.addRecipe(new ShapelessOreRecipe(itemYellowCake, IC2Items.getItem("reactorUraniumSimple")));
            GameRegistry.addRecipe(new ShapelessOreRecipe(IC2Items.getItem("cell"), itemCell));
            GameRegistry.addRecipe(new ShapelessOreRecipe(itemCell, "cellEmpty"));
        }

        // Antimatter
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemAntimatter, 1, 1), new Object[]
        { itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter }));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemAntimatter, 8, 0), new Object[]
        { new ItemStack(itemAntimatter, 1, 1) }));

        // Steam Funnel
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockSteamFunnel, 2), new Object[]
        { " B ", "B B", "B B", 'B', UniversalRecipe.SECONDARY_METAL.get() }));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockSteamFunnel, 2), new Object[]
        { " B ", "B B", "B B", 'B', "ingotIron" }));

        // Atomic Assembler
        GameRegistry.addRecipe(new ShapedOreRecipe(blockQuantumAssembler, new Object[]
        { "CCC", "SXS", "SSS", 'X', blockCentrifuge, 'C', UniversalRecipe.CIRCUIT_T3.get(), 'S', UniversalRecipe.PRIMARY_PLATE.get() }));

        // Fulmination Generator
        GameRegistry.addRecipe(new ShapedOreRecipe(blockFulmination, new Object[]
        { "OSO", "SCS", "OSO", 'O', Blocks.obsidian, 'C', UniversalRecipe.CIRCUIT_T2.get(), 'S', UniversalRecipe.PRIMARY_PLATE.get() }));

        // Particle Accelerator
        GameRegistry.addRecipe(new ShapedOreRecipe(blockAccelerator, new Object[]
        { "SCS", "CMC", "SCS", 'M', UniversalRecipe.MOTOR.get(), 'C', UniversalRecipe.CIRCUIT_T3.get(), 'S', UniversalRecipe.PRIMARY_PLATE.get() }));

        // Centrifuge
        GameRegistry.addRecipe(new ShapedOreRecipe(blockCentrifuge, new Object[]
        { "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T2.get(), 'S', UniversalRecipe.PRIMARY_PLATE.get(), 'B', UniversalRecipe.SECONDARY_METAL.get(), 'M',
                UniversalRecipe.MOTOR.get() }));

        // Nuclear Boiler
        GameRegistry.addRecipe(new ShapedOreRecipe(blockNuclearBoiler, new Object[]
        { "S S", "FBF", "SMS", 'F', Blocks.furnace, 'S', UniversalRecipe.PRIMARY_PLATE.get(), 'B', Items.bucket, 'M', UniversalRecipe.MOTOR.get() }));

        // Chemical Extractor
        GameRegistry.addRecipe(new ShapedOreRecipe(blockChemicalExtractor, new Object[]
        { "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T3.get(), 'S', UniversalRecipe.PRIMARY_PLATE.get(), 'B', UniversalRecipe.SECONDARY_METAL.get(), 'M',
                UniversalRecipe.MOTOR.get() }));

        // Siren
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockSiren, 2), new Object[]
        { "NPN", 'N', Blocks.noteblock, 'P', UniversalRecipe.SECONDARY_PLATE.get() }));

        // Fission Reactor
        GameRegistry
                .addRecipe(new ShapedOreRecipe(blockReactorCell, new Object[]
                { "SCS", "MEM", "SCS", 'E', "cellEmpty", 'C', UniversalRecipe.CIRCUIT_T2.get(), 'S', UniversalRecipe.PRIMARY_PLATE.get(), 'M',
                        UniversalRecipe.MOTOR.get() }));

        // Fusion Reactor
        GameRegistry.addRecipe(new ShapedOreRecipe(blockFusionCore, new Object[]
        { "CPC", "PFP", "CPC", 'P', UniversalRecipe.PRIMARY_PLATE.get(), 'F', blockReactorCell, 'C', UniversalRecipe.CIRCUIT_T3.get() }));

        // Turbine
        GameRegistry.addRecipe(new ShapedOreRecipe(blockElectricTurbine, new Object[]
        { " B ", "BMB", " B ", 'B', UniversalRecipe.SECONDARY_PLATE.get(), 'M', UniversalRecipe.MOTOR.get() }));

        // Empty Cell
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCell, 16), new Object[]
        { " T ", "TGT", " T ", 'T', "ingotTin", 'G', Blocks.glass }));

        // Water Cell
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemWaterCell), new Object[]
        { "cellEmpty", Items.water_bucket }));

        // Thermometer
        GameRegistry.addRecipe(new ShapedOreRecipe(blockThermometer, new Object[]
        { "SSS", "GCG", "GSG", 'S', UniversalRecipe.PRIMARY_METAL.get(), 'G', Blocks.glass, 'C', UniversalRecipe.CIRCUIT_T1.get() }));

        // Control Rod
        GameRegistry.addRecipe(new ShapedOreRecipe(blockControlRod, new Object[]
        { "I", "I", "I", 'I', Items.iron_ingot }));

        // Fuel Rod
        GameRegistry.addRecipe(new ShapedOreRecipe(itemFissileFuel, new Object[]
        { "CUC", "CUC", "CUC", 'U', "ingotUranium", 'C', "cellEmpty" }));

        // Breeder Rod
        GameRegistry.addRecipe(new ShapedOreRecipe(itemBreedingRod, new Object[]
        { "CUC", "CUC", "CUC", 'U', "breederUranium", 'C', "cellEmpty" }));

        // Electromagnet
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockElectromagnet, 2, 0), new Object[]
        { "BBB", "BMB", "BBB", 'B', UniversalRecipe.SECONDARY_METAL.get(), 'M', UniversalRecipe.MOTOR.get(S) }));

        // Electromagnet Glass
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(blockElectromagnet, 1, 1), new Object[]
        { blockElectromagnet, Blocks.glass }));

        // Hazmat Suit
        GameRegistry.addRecipe(new ShapedOreRecipe(itemHazmatTop, new Object[]
        { "SSS", "BAB", "SCS", 'A', Items.leather_helmet, 'C', UniversalRecipe.CIRCUIT_T1.get(), 'S', Blocks.wool }));
        GameRegistry.addRecipe(new ShapedOreRecipe(itemHazmatBody, new Object[]
        { "SSS", "BAB", "SCS", 'A', Items.leather_chestplate, 'C', UniversalRecipe.CIRCUIT_T1.get(), 'S', Blocks.wool }));
        GameRegistry.addRecipe(new ShapedOreRecipe(itemHazmatLeggings, new Object[]
        { "SSS", "BAB", "SCS", 'A', Items.leather_leggings, 'C', UniversalRecipe.CIRCUIT_T1.get(), 'S', Blocks.wool }));
        GameRegistry.addRecipe(new ShapedOreRecipe(itemHazmatBoots, new Object[]
        { "SSS", "BAB", "SCS", 'A', Items.leather_boots, 'C', UniversalRecipe.CIRCUIT_T1.get(), 'S', Blocks.wool }));

        EntityRegistry.registerGlobalEntityID(EntityParticle.class, "ASParticle", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerModEntity(EntityParticle.class, "ASParticle", ENTITY_ID_PREFIX, this, 80, 3, true);

        Atomic.proxy.init();

        Settings.config.load();


            for (String oreName : OreDictionary.getOreNames())
            {
                if (oreName.startsWith("ingot"))
                {
                    for (ItemStack itemStack : OreDictionary.getOres(oreName))
                    {
                        if (itemStack != null)
                        {
                            QuantumAssemblerRecipes.addRecipe(itemStack);
                        }
                    }
                }
            }

        Settings.config.save();
    }

    @EventHandler
    public void thermalEventHandler(EventThermalUpdate evt)
    {
        VectorWorld pos = evt.position;
        Block block = Block.blocksList[pos.getBlock()];

        if (block == blockElectromagnet)
        {
            evt.heatLoss = evt.deltaTemperature * 0.6f;
        }
    }

    @EventHandler
    public void plasmaEvent(SpawnPlasmaEvent evt)
    {
        Block block = evt.world.getBlock(evt.x, evt.y, evt.z);

        if (block != null && block.getBlockHardness(evt.world, evt.x, evt.y, evt.z) >= 0)
        {
            TileEntity tile = evt.world.getTileEntity(evt.x, evt.y, evt.z);

            if (tile instanceof TilePlasma)
            {
                ((TilePlasma) tile).setTemperature(evt.temperature);
                return;
            }else if (tile instanceof IElectromagnet)
            {
                return;
            }
            else
            {
                evt.world.setBlockToAir(evt.x, evt.y, evt.z);
                evt.world.setBlock(evt.x, evt.y, evt.z, blockPlasma);
            }
        }
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void preTextureHook(TextureStitchEvent.Pre event)
    {
        if (event.map.getTextureType() == 0)
        {
            RenderUtility.registerIcon(Reference.prefix() + "uraniumHexafluoride", event.map);
            RenderUtility.registerIcon(Reference.prefix() + "steam", event.map);
            RenderUtility.registerIcon(Reference.prefix() + "deuterium", event.map);
            RenderUtility.registerIcon(Reference.prefix() + "tritium", event.map);
            RenderUtility.registerIcon(Reference.prefix() + "atomic_edge", event.map);
            RenderUtility.registerIcon(Reference.prefix() + "funnel_edge", event.map);
            RenderUtility.registerIcon(Reference.prefix() + "glass", event.map);
        }
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void postTextureHook(TextureStitchEvent.Post event)
    {
        FLUID_URANIUM_HEXAFLOURIDE.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix() + "uraniumHexafluoride"));
        FLUID_STEAM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix() + "steam"));
        FLUID_DEUTERIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix() + "deuterium"));
        FLUID_TRITIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix() + "tritium"));
        FLUID_TOXIC_WASTE.setIcons(blockToxicWaste.getIcon(0, 0));
        FLUID_PLASMA.setIcons(blockPlasma.getIcon(0, 0));
    }    

    @EventHandler
    public void fillBucketEvent(FillBucketEvent evt)
    {
        if (!evt.world.isRemote && evt.target != null && evt.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            VectorWorld pos = new VectorWorld(evt.world, evt.target);

            if (pos.getBlock() == blockToxicWaste)
            {
                pos.setBlockToAir();
                evt.result = new ItemStack(itemBucketToxic);
                evt.setResult(Event.Result.ALLOW);
            }
        }
    }

    /** Recipes */
    public static enum RecipeType
    {
        CHEMICAL_EXTRACTOR;
    }
}
