package resonantinduction.archaic;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonantinduction.archaic.blocks.BlockTurntable;
import resonantinduction.archaic.crate.BlockCrate;
import resonantinduction.archaic.crate.ItemBlockCrate;
import resonantinduction.archaic.crate.TileCrate;
import resonantinduction.archaic.engineering.BlockEngineeringTable;
import resonantinduction.archaic.engineering.ItemHammer;
import resonantinduction.archaic.engineering.TileEngineeringTable;
import resonantinduction.archaic.firebox.BlockFirebox;
import resonantinduction.archaic.firebox.BlockHotPlate;
import resonantinduction.archaic.firebox.TileFirebox;
import resonantinduction.archaic.firebox.TileHotPlate;
import resonantinduction.archaic.imprint.BlockImprinter;
import resonantinduction.archaic.imprint.ItemImprint;
import resonantinduction.archaic.imprint.TileImprinter;
import resonantinduction.archaic.process.BlockCast;
import resonantinduction.archaic.process.BlockMillstone;
import resonantinduction.archaic.process.TileCast;
import resonantinduction.archaic.process.TileMillstone;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.core.part.BlockMachineMaterial;
import calclavia.lib.content.ContentRegistry;
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

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID).setPrefix(Reference.PREFIX).setTab(TabRI.CORE);

	public static Block blockEngineeringTable;
	public static Block blockCrate;
	public static Block blockImprinter;
	public static Block blockTurntable;
	public static Block blockFirebox;
	public static Block blockHotPlate;
	public static Block blockMillstone;
	public static Block blockCast;
	public static Block blockMachinePart;

	public static Item itemImprint;

	// Machine and Processing
	public static Item itemHammer;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		Settings.load();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		blockEngineeringTable = contentRegistry.createTile(BlockEngineeringTable.class, TileEngineeringTable.class);
		blockCrate = contentRegistry.createBlock(BlockCrate.class, ItemBlockCrate.class, TileCrate.class);
		blockImprinter = contentRegistry.createTile(BlockImprinter.class, TileImprinter.class);
		blockTurntable = contentRegistry.createBlock(BlockTurntable.class);
		blockFirebox = contentRegistry.createBlock(BlockFirebox.class, ItemBlockMetadata.class, TileFirebox.class);
		blockHotPlate = contentRegistry.createTile(BlockHotPlate.class, TileHotPlate.class);
		blockMillstone = contentRegistry.createTile(BlockMillstone.class, TileMillstone.class);
		blockCast = contentRegistry.createTile(BlockCast.class, TileCast.class);
		blockMachinePart = contentRegistry.createBlock(BlockMachineMaterial.class, ItemBlockMetadata.class);

		itemImprint = contentRegistry.createItem(ItemImprint.class);
		itemHammer = contentRegistry.createItem(ItemHammer.class);
		proxy.preInit();
		Settings.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		Settings.setModMetadata(metadata, ID, NAME);
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		// Add recipes
		GameRegistry.addRecipe(new ShapedOreRecipe(blockEngineeringTable, "RAH", "SCP", "WWW", 'R', Item.shears, 'H', Item.hoeStone, 'A', Item.axeStone, 'P', Item.pickaxeStone, 'S', Item.shovelStone, 'C', Block.workbench, 'W', "logWood"));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 0), "WWW", "WSW", "WWW", 'S', Item.stick, 'W', "logWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 1), "WWW", "WSW", "WWW", 'S', new ItemStack(blockCrate, 1, 0), 'W', "ingotIron"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 2), "WWW", "WSW", "WWW", 'S', new ItemStack(blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get()));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockFirebox, "III", "SFS", "SSS", 'I', Item.ingotIron, 'F', Block.furnaceIdle, 'S', Block.stone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockFirebox, 1, 1), "III", "SFS", "SSS", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'F', new ItemStack(blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get()));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockImprinter, "SSS", "W W", "PPP", 'S', Block.stone, 'P', Block.pistonBase, 'W', "logWood"));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockTurntable, "SSS", "PGP", "WWW", 'S', Block.stone, 'G', UniversalRecipe.MOTOR.get(), 'P', Block.pistonBase, 'W', "logWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockCast, "I I", "IBI", "III", 'S', Item.ingotIron, 'B', Block.fenceIron));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockHotPlate, "SSS", "III", 'I', Item.ingotIron, 'S', Block.stone));

		GameRegistry.addRecipe(new ShapedOreRecipe(itemImprint, "PPP", "PIP", "PPP", 'P', Item.paper, 'I', new ItemStack(Item.dyePowder, 0)));
		GameRegistry.addRecipe(new ShapedOreRecipe(itemHammer, "CC ", "CS ", "  S", 'C', Block.cobblestone, 'S', Item.stick));
	}
}
