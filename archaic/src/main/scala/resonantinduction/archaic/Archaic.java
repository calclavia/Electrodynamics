package resonantinduction.archaic;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.lib.content.ContentRegistry;
import resonant.lib.modproxy.ProxyHandler;
import resonant.lib.network.PacketAnnotation;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.item.ItemBlockMetadata;
import resonant.lib.recipe.UniversalRecipe;
import resonantinduction.archaic.blocks.TileTurntable;
import resonantinduction.archaic.crate.BlockCrate;
import resonantinduction.archaic.crate.CrateCraftingHandler;
import resonantinduction.archaic.crate.ItemBlockCrate;
import resonantinduction.archaic.crate.TileCrate;
import resonantinduction.archaic.engineering.ItemHammer;
import resonantinduction.archaic.engineering.TileEngineeringTable;
import resonantinduction.archaic.filter.BlockImprinter;
import resonantinduction.archaic.filter.TileFilter;
import resonantinduction.archaic.filter.TileImprinter;
import resonantinduction.archaic.firebox.BlockFirebox;
import resonantinduction.archaic.firebox.BlockHotPlate;
import resonantinduction.archaic.firebox.TileFirebox;
import resonantinduction.archaic.firebox.TileHotPlate;
import resonantinduction.archaic.fluid.grate.TileGrate;
import resonantinduction.archaic.fluid.gutter.TileGutter;
import resonantinduction.archaic.fluid.tank.TileTank;
import resonantinduction.archaic.process.BlockCastingMold;
import resonantinduction.archaic.process.BlockMillstone;
import resonantinduction.archaic.process.TileCastingMold;
import resonantinduction.archaic.process.TileMillstone;
import resonantinduction.archaic.waila.Waila;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.core.prefab.imprint.ItemImprint;
import resonantinduction.core.resource.ItemHandCrank;
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

/** Resonant Induction Archaic Module
 * 
 * @author DarkCow, Calclavia */
@Mod(modid = Archaic.ID, name = Archaic.NAME, version = Reference.VERSION, dependencies = "required-after:" + ResonantInduction.ID)
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Archaic
{
    /** Mod Information */
    public static final String ID = "ResonantInduction|Archaic";
    public static final String NAME = Reference.NAME + " Archaic";
    public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID).setPrefix(Reference.PREFIX).setTab(TabRI.DEFAULT);
    @Instance(ID)
    public static Archaic INSTANCE;
    @SidedProxy(clientSide = "resonantinduction.archaic.ClientProxy", serverSide = "resonantinduction.archaic.CommonProxy")
    public static CommonProxy proxy;
    @Mod.Metadata(ID)
    public static ModMetadata metadata;
    public static Block blockEngineeringTable;
    public static Block blockCrate;
    public static Block blockImprinter;
    public static Block blockTurntable;
    public static Block blockFirebox;
    public static Block blockHotPlate;
    public static Block blockMillstone;
    public static Block blockCast;
    public static Item itemImprint;

    // Machine and Processing
    public static Item itemHammer;
    public static Item itemHandCrank;
    public static Block blockFilter;

    // Fluid
    public static Block blockGrate;
    public static Block blockGutter;
    public static Block blockTank;

    public ProxyHandler modproxies;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        modproxies = new ProxyHandler();
        NetworkRegistry.instance().registerGuiHandler(this, proxy);
        Settings.CONFIGURATION.load();
        blockEngineeringTable = contentRegistry.newBlock(TileEngineeringTable.class);
        blockCrate = contentRegistry.createBlock(BlockCrate.class, ItemBlockCrate.class, TileCrate.class);
        blockImprinter = contentRegistry.createTile(BlockImprinter.class, TileImprinter.class);
        blockTurntable = contentRegistry.newBlock(TileTurntable.class);
        blockFirebox = contentRegistry.createBlock(BlockFirebox.class, ItemBlockMetadata.class, TileFirebox.class);
        blockHotPlate = contentRegistry.createTile(BlockHotPlate.class, TileHotPlate.class);
        blockMillstone = contentRegistry.createTile(BlockMillstone.class, TileMillstone.class);
        blockCast = contentRegistry.createTile(BlockCastingMold.class, TileCastingMold.class);
        blockGutter = contentRegistry.newBlock(TileGutter.class);
        blockGrate = contentRegistry.newBlock(TileGrate.class);
        blockFilter = contentRegistry.newBlock(TileFilter.class);
        blockTank = contentRegistry.newBlock(TileTank.class);

        itemHandCrank = contentRegistry.createItem(ItemHandCrank.class);
        itemImprint = contentRegistry.createItem(ItemImprint.class);
        itemHammer = contentRegistry.createItem(ItemHammer.class);

        modproxies.applyModule(Waila.class, true);
        Settings.CONFIGURATION.save();

        PacketAnnotation.register(TileFirebox.class);
        PacketAnnotation.register(TileFilter.class);
        proxy.preInit();
        modproxies.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        Settings.setModMetadata(metadata, ID, NAME, ResonantInduction.ID);
        proxy.init();
        modproxies.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        TabRI.ITEMSTACK = new ItemStack(blockEngineeringTable);
        if (OreDictionary.getOres("cobblestone") == null)
        {
            OreDictionary.registerOre("cobblestone", Block.cobblestone);
        }
        if (OreDictionary.getOres("stickWood") == null)
        {
            OreDictionary.registerOre("stickWood", Item.stick);
        }

        // Add recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(blockEngineeringTable, "P", "C", 'P', Block.pressurePlatePlanks, 'C', Block.workbench));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockFilter, "B", "P", "B", 'B', Block.fenceIron, 'P', Item.paper));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 0), "WWW", "WSW", "WWW", 'S', "stickWood", 'W', "logWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 1), "WWW", "WSW", "WWW", 'S', new ItemStack(blockCrate, 1, 0), 'W', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 2), "WWW", "WSW", "WWW", 'S', new ItemStack(blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get()));

        GameRegistry.addRecipe(new ShapedOreRecipe(blockFirebox, "III", "SFS", "SSS", 'I', Item.ingotIron, 'F', Block.furnaceIdle, 'S', Block.stone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockFirebox, 1, 1), "III", "SFS", "SSS", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'F', new ItemStack(blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get()));

        GameRegistry.addRecipe(new ShapedOreRecipe(blockImprinter, "SSS", "W W", "PPP", 'S', Block.stone, 'P', Block.pistonBase, 'W', "logWood"));

        GameRegistry.addRecipe(new ShapedOreRecipe(blockTurntable, "SSS", "PGP", "WWW", 'S', Block.stone, 'G', Item.redstone, 'P', Block.pistonBase, 'W', "logWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockCast, "I I", "IBI", "III", 'S', Item.ingotIron, 'B', Block.fenceIron));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockGutter, "S S", "I I", "III", 'S', Item.stick, 'I', "cobblestone"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockGrate, "WBW", "B B", "WBW", 'B', Block.fenceIron, 'W', "plankWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockHotPlate, "SSS", "III", 'I', Item.ingotIron, 'S', Block.stone));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockMillstone, "SPS", "SAS", "SSS", 'P', Block.pistonBase, 'A', Item.pickaxeStone, 'S', Block.stone));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockTank, "GGG", "GSG", "GGG", 'G', Block.glass, 'S', Item.ingotIron));

        GameRegistry.addRecipe(new ShapedOreRecipe(itemHandCrank, "S  ", "SSS", "  S", 'S', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(itemImprint, "PPP", "PIP", "PPP", 'P', Item.paper, 'I', new ItemStack(Item.dyePowder, 0)));
        GameRegistry.addRecipe(new ShapedOreRecipe(itemHammer, "CC ", "CS ", "  S", 'C', "cobblestone", 'S', "stickWood"));
        
        GameRegistry.registerCraftingHandler(new CrateCraftingHandler());
        proxy.postInit();
        modproxies.postInit();
    }
}
