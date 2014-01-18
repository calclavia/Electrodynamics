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
import resonantinduction.archaic.engineering.TileEngineeringTable;
import resonantinduction.archaic.firebox.BlockFirebox;
import resonantinduction.archaic.firebox.BlockHotPlate;
import resonantinduction.archaic.firebox.TileFirebox;
import resonantinduction.archaic.firebox.TileHotPlate;
import resonantinduction.archaic.imprint.BlockImprinter;
import resonantinduction.archaic.imprint.ItemBlockImprint;
import resonantinduction.archaic.imprint.TileImprinter;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
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

	public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, ID);

	public static Block blockEngineeringTable;
	public static Block blockCrate;
	public static Block blockImprinter;
	public static Block blockTurntable;
	public static Block blockFirebox;
	public static Block blockHotPlate;

	public static Item itemImprint;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		Settings.load();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		blockEngineeringTable = contentRegistry.createTile(BlockEngineeringTable.class, TileEngineeringTable.class);
		blockCrate = contentRegistry.createBlock(BlockCrate.class, ItemBlockCrate.class, TileCrate.class);
		blockImprinter = contentRegistry.createTile(BlockImprinter.class, TileImprinter.class);
		blockTurntable = contentRegistry.createBlock(BlockTurntable.class);
		blockFirebox = contentRegistry.createTile(BlockFirebox.class, TileFirebox.class);
		blockHotPlate = contentRegistry.createBlock(BlockHotPlate.class, ItemBlockMetadata.class, TileHotPlate.class);

		itemImprint = contentRegistry.createItem(ItemBlockImprint.class);
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
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 2), "WWW", "WSW", "WWW", 'S', new ItemStack(blockCrate, 1, 1), 'W', "ingotSteel"));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockFirebox, "III", "SFS", "SSS", 'I', Item.ingotIron, 'F', Block.furnaceIdle, 'S', Block.stone));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockImprinter, "SSS", "W W", "PPP", 'S', Block.stone, 'P', Block.pistonBase, 'W', "logWood"));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockTurntable, "SSS", "PGP", "WWW", 'S', Block.stone, 'G', "gear", 'P', Block.pistonBase, 'W', "logWood"));
		GameRegistry.addRecipe(new ShapedOreRecipe(blockTurntable, "SSS", "PGP", "WWW", 'S', Block.stone, 'G', Item.redstone, 'P', Block.pistonBase, 'W', "logWood"));

		GameRegistry.addRecipe(new ShapedOreRecipe(blockHotPlate, "SSS", "III", 'I', Item.ingotIron, 'S', Block.stone));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockHotPlate, 1, 1), "HHH", "WWW", 'H', new ItemStack(blockHotPlate, 1, 0), 'W', UniversalRecipe.WIRE.get()));
	}
}
