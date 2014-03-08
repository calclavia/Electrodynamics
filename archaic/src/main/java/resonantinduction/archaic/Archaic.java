package resonantinduction.archaic;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonantinduction.archaic.blocks.BlockTurntable;
import resonantinduction.archaic.crate.BlockCrate;
import resonantinduction.archaic.crate.ItemBlockCrate;
import resonantinduction.archaic.crate.TileCrate;
import resonantinduction.archaic.engineering.ItemHammer;
import resonantinduction.archaic.engineering.TileEngineeringTable;
import resonantinduction.archaic.filter.BlockFilter;
import resonantinduction.archaic.filter.BlockImprinter;
import resonantinduction.archaic.filter.TileFilter;
import resonantinduction.archaic.filter.TileImprinter;
import resonantinduction.archaic.firebox.BlockFirebox;
import resonantinduction.archaic.firebox.BlockHotPlate;
import resonantinduction.archaic.firebox.TileFirebox;
import resonantinduction.archaic.firebox.TileHotPlate;
import resonantinduction.archaic.fluid.grate.BlockGrate;
import resonantinduction.archaic.fluid.grate.TileGrate;
import resonantinduction.archaic.fluid.gutter.TileGutter;
import resonantinduction.archaic.fluid.tank.TileTank;
import resonantinduction.archaic.process.BlockCastingMold;
import resonantinduction.archaic.process.BlockMillstone;
import resonantinduction.archaic.process.TileCastingMold;
import resonantinduction.archaic.process.TileMillstone;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.core.prefab.imprint.ItemImprint;
import resonantinduction.core.resource.ItemHandCrank;
import calclavia.lib.content.ContentRegistry;
import calclavia.lib.network.PacketAnnotation;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.item.ItemBlockMetadata;
import calclavia.lib.recipe.UniversalRecipe;
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
 * Resonant Induction Archaic Module
 * 
 * @author DarkCow, Calclavia
 */
@Mod(modid = Archaic.ID, name = Archaic.NAME, version = Reference.VERSION, dependencies = "required-after:" + ResonantInduction.ID)
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Archaic
{
	/** Mod Information */
	public static final String ID = "ResonantInduction|Archaic";
	public static final String NAME = Reference.NAME + " Archaic";

	@Instance(ID)
	public static Archaic INSTANCE;

	@SidedProxy(clientSide = "resonantinduction.archaic.ClientProxy", serverSide = "resonantinduction.archaic.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Metadata(ID)
	public static ModMetadata metadata;

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID).setPrefix(Reference.PREFIX).setTab(TabRI.DEFAULT);

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

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		Settings.load();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		blockEngineeringTable = contentRegistry.newBlock(TileEngineeringTable.class);
		blockCrate = contentRegistry.createBlock(BlockCrate.class, ItemBlockCrate.class, TileCrate.class);
		blockImprinter = contentRegistry.createTile(BlockImprinter.class, TileImprinter.class);
		blockTurntable = contentRegistry.createBlock(BlockTurntable.class);
		blockFirebox = contentRegistry.createBlock(BlockFirebox.class, ItemBlockMetadata.class, TileFirebox.class);
		blockHotPlate = contentRegistry.createTile(BlockHotPlate.class, TileHotPlate.class);
		blockMillstone = contentRegistry.createTile(BlockMillstone.class, TileMillstone.class);
		blockCast = contentRegistry.createTile(BlockCastingMold.class, TileCastingMold.class);
		blockGutter = contentRegistry.newBlock(TileGutter.class);
		blockGrate = contentRegistry.createTile(BlockGrate.class, TileGrate.class);
		blockFilter = contentRegistry.createTile(BlockFilter.class, TileFilter.class);
		blockTank = contentRegistry.newBlock(TileTank.class);

		itemHandCrank = contentRegistry.createItem(ItemHandCrank.class);
		itemImprint = contentRegistry.createItem(ItemImprint.class);
		itemHammer = contentRegistry.createItem(ItemHammer.class);

		proxy.preInit();
		Settings.save();
		TabRI.ITEMSTACK = new ItemStack(blockEngineeringTable);

		PacketAnnotation.register(TileFirebox.class);
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		Settings.setModMetadata(metadata, ID, NAME, ResonantInduction.ID);
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		// Add recipes
		GameRegistry.addRecipe(new ShapedOreRecipe(blockEngineeringTable, "P", "C", 'P', Block.pressurePlatePlanks, 'C', Block.workbench));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockFilter, "B", "P", "B", 'B', Block.fenceIron, 'P', Item.paper));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 0), "WWW", "WSW", "WWW", 'S', Item.stick, 'W', "logWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 1), "WWW", "WSW", "WWW", 'S', new ItemStack(blockCrate, 1, 0), 'W', "ingotIron"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 2), "WWW", "WSW", "WWW", 'S', new ItemStack(blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get()));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockFirebox, "III", "SFS", "SSS", 'I', Item.ingotIron, 'F', Block.furnaceIdle, 'S', Block.stone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockFirebox, 1, 1), "III", "SFS", "SSS", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'F', new ItemStack(blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get()));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockImprinter, "SSS", "W W", "PPP", 'S', Block.stone, 'P', Block.pistonBase, 'W', "logWood"));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockTurntable, "SSS", "PGP", "WWW", 'S', Block.stone, 'G', UniversalRecipe.MOTOR.get(), 'P', Block.pistonBase, 'W', "logWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockCast, "I I", "IBI", "III", 'S', Item.ingotIron, 'B', Block.fenceIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockGutter, "S S", "I I", "III", 'S', Item.stick, 'I', "plankWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockGrate, "WBW", "B B", "WBW", 'B', Block.fenceIron, 'W', "plankWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockHotPlate, "SSS", "III", 'I', Item.ingotIron, 'S', Block.stone));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockMillstone, "SPS", "SAS", "SSS", 'P', Block.pistonBase, 'A', Item.pickaxeStone, 'S', Block.stone));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockTank, "GGG", "GSG", "GGG", 'G', Block.glass, 'S', Item.ingotIron));

		GameRegistry.addRecipe(new ShapedOreRecipe(itemHandCrank, "S  ", "SSS", "  S", 'S', Item.stick));
		GameRegistry.addRecipe(new ShapedOreRecipe(itemImprint, "PPP", "PIP", "PPP", 'P', Item.paper, 'I', new ItemStack(Item.dyePowder, 0)));
		GameRegistry.addRecipe(new ShapedOreRecipe(itemHammer, "CC ", "CS ", "  S", 'C', Block.cobblestone, 'S', Item.stick));
		proxy.postInit();
	}
}
