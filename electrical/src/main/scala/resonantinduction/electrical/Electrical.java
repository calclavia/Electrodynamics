package resonantinduction.electrical;

import ic2.api.item.Items;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import resonant.lib.content.ContentRegistry;
import resonant.lib.modproxy.ProxyHandler;
import resonant.lib.network.PacketHandler;
import resonant.lib.recipe.UniversalRecipe;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.core.resource.ItemResourcePart;
import resonantinduction.electrical.armbot.BlockArmbot;
import resonantinduction.electrical.armbot.TileArmbot;
import resonantinduction.electrical.battery.BlockBattery;
import resonantinduction.electrical.battery.ItemBlockBattery;
import resonantinduction.electrical.battery.TileBattery;
import resonantinduction.electrical.charger.ItemCharger;
import resonantinduction.electrical.generator.BlockMotor;
import resonantinduction.electrical.generator.TileMotor;
import resonantinduction.electrical.generator.solar.TileSolarPanel;
import resonantinduction.electrical.generator.thermopile.BlockThermopile;
import resonantinduction.electrical.generator.thermopile.TileThermopile;
import resonantinduction.electrical.itemrailing.ItemItemRailing;
import resonantinduction.electrical.laser.gun.ItemMiningLaser;
import resonantinduction.electrical.levitator.ItemLevitator;
import resonantinduction.electrical.multimeter.ItemMultimeter;
import resonantinduction.electrical.tesla.BlockTesla;
import resonantinduction.electrical.tesla.TileTesla;
import resonantinduction.electrical.transformer.ItemTransformer;
import resonantinduction.electrical.wire.EnumWireMaterial;
import resonantinduction.electrical.wire.ItemWire;
import resonantinduction.quantum.gate.ItemQuantumGlyph;
import cpw.mods.fml.common.Loader;
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

/** Resonant Induction Electrical Module
 * 
 * @author Calclavia */
@Mod(modid = Electrical.ID, name = Electrical.NAME, version = Reference.VERSION, dependencies = "before:ThermalExpansion;before:Mekanism;after:ResonantInduction|Mechanical;required-after:" + ResonantInduction.ID)
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Electrical
{
    /** Mod Information */
    public static final String ID = "ResonantInduction|Electrical";
    public static final String NAME = Reference.NAME + " Electrical";

    @Instance(ID)
    public static Electrical INSTANCE;

    @SidedProxy(clientSide = "resonantinduction.electrical.ClientProxy", serverSide = "resonantinduction.electrical.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Metadata(ID)
    public static ModMetadata metadata;

    public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, Settings.idManager, ID).setPrefix(Reference.PREFIX).setTab(TabRI.DEFAULT);

    // Energy
    public static Item itemWire;
    public static Item itemMultimeter;
    public static Item itemTransformer;
    public static Item itemCharger;
    public static Block blockTesla;
    public static Block blockBattery;
    public static Block blockEncoder;

    // Railings
    public static Item itemRailing;

    // Generators
    public static Block blockSolarPanel;
    public static Block blockMotor;
    public static Block blockThermopile;

    // Transport
    public static Item itemLevitator;
    public static Block blockArmbot;
    public static Item itemDisk;
    public static Item itemInsulation;

    // Quantum
    public static Block blockQuantumGate;
    public static Item itemQuantumGlyph;

    // Tools
    public static Item itemLaserGun;

    public ProxyHandler modproxies;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        modproxies = new ProxyHandler();
        NetworkRegistry.instance().registerGuiHandler(this, proxy);

        Settings.CONFIGURATION.load();
        // Energy
        itemWire = contentRegistry.createItem(ItemWire.class);
        itemMultimeter = contentRegistry.createItem(ItemMultimeter.class);
        itemTransformer = contentRegistry.createItem(ItemTransformer.class);
        itemCharger = contentRegistry.createItem(ItemCharger.class);
        blockTesla = contentRegistry.createTile(BlockTesla.class, TileTesla.class);
        blockBattery = contentRegistry.createBlock(BlockBattery.class, ItemBlockBattery.class, TileBattery.class);
        blockArmbot = contentRegistry.createBlock(BlockArmbot.class, null, TileArmbot.class);

        // Transport
        itemLevitator = contentRegistry.createItem(ItemLevitator.class);
        itemInsulation = contentRegistry.createItem("insulation", ItemResourcePart.class);
        itemLaserGun = contentRegistry.createItem("laserDrill", ItemMiningLaser.class);

        // Generator
        blockSolarPanel = contentRegistry.newBlock(TileSolarPanel.class);
        blockMotor = contentRegistry.createTile(BlockMotor.class, TileMotor.class);
        blockThermopile = contentRegistry.createTile(BlockThermopile.class, TileThermopile.class);

        // Quantum
        itemQuantumGlyph = contentRegistry.createItem(ItemQuantumGlyph.class);

        //Railings
        itemRailing = contentRegistry.createItem(ItemItemRailing.class);

        Settings.CONFIGURATION.save();

        OreDictionary.registerOre("wire", itemWire);
        OreDictionary.registerOre("motor", blockMotor);
        OreDictionary.registerOre("battery", ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0));
        OreDictionary.registerOre("batteryBox", ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0));

        /** Set reference itemstacks */
        TabRI.ITEMSTACK = new ItemStack(itemTransformer);

        for (EnumWireMaterial material : EnumWireMaterial.values())
        {
            material.setWire(itemWire);
        }

        proxy.preInit();
        modproxies.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        Settings.setModMetadata(metadata, ID, NAME, ResonantInduction.ID);
        MultipartElectrical.INSTANCE = new MultipartElectrical();
        proxy.init();
        modproxies.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        /** Recipes */
        /** Tesla - by Jyzarc */
        GameRegistry.addRecipe(new ShapedOreRecipe(blockTesla, "WEW", " C ", "DID", 'W', "wire", 'E', Item.eyeOfEnder, 'C', UniversalRecipe.BATTERY.get(), 'D', Item.diamond, 'I', UniversalRecipe.PRIMARY_PLATE.get()));

        /** Multimeter */
        GameRegistry.addRecipe(new ShapedOreRecipe(itemMultimeter, "WWW", "ICI", 'W', "wire", 'C', UniversalRecipe.BATTERY.get(), 'I', UniversalRecipe.PRIMARY_METAL.get()));

        // GameRegistry.addRecipe(new ShapedOreRecipe(itemDisk, "PPP", "RRR", "WWW", 'W', "wire",
        // 'P', Item.paper, 'R', Item.redstone));

        /** Battery */
        ItemStack tierOneBattery = ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0);
        ItemStack tierTwoBattery = ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 1);
        ItemStack tierThreeBattery = ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 2);

        GameRegistry.addRecipe(new ShapedOreRecipe(tierOneBattery, "III", "IRI", "III", 'R', Block.blockRedstone, 'I', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(tierTwoBattery, "RRR", "RIR", "RRR", 'R', tierOneBattery, 'I', UniversalRecipe.PRIMARY_PLATE.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(tierThreeBattery, "RRR", "RIR", "RRR", 'R', tierTwoBattery, 'I', Block.blockDiamond));

        /** Wires **/
        //GameRegistry.addRecipe(new ShapelessOreRecipe(itemInsulation, Item.slimeBall, new ItemStack(Block.cloth, 2, OreDictionary.WILDCARD_VALUE)));
        //GameRegistry.addRecipe(new ShapelessOreRecipe(itemInsulation, "slimeball", new ItemStack(Block.cloth, 2, OreDictionary.WILDCARD_VALUE)));

        GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper"));
        GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin"));
        GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Item.ingotIron));
        GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SILVER.getWire(), "MMM", 'M', "ingotSilver"));
        GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", 'M', "ingotSuperconductor"));
        GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(3), "MMM", "MEM", "MMM", 'M', Item.ingotGold, 'E', Item.eyeOfEnder));

        GameRegistry.addRecipe(new ShapedOreRecipe(itemCharger, "WWW", "ICI", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get(), 'C', UniversalRecipe.CIRCUIT_T1.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(itemTransformer, "WWW", "WWW", "III", 'W', "wire", 'I', UniversalRecipe.PRIMARY_METAL.get()));
        //GameRegistry.addRecipe(new ShapedOreRecipe(itemLevitator, " G ", "SDS", "SWS", 'W', "wire", 'G', Block.glass, 'D', Block.blockDiamond, 'S', UniversalRecipe.PRIMARY_METAL.get()));

        /** Quantum Gates */
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemQuantumGlyph, 1, 0), " CT", "LBL", "TCT", 'B', Block.blockDiamond, 'L', itemLevitator, 'C', itemCharger, 'T', blockTesla));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemQuantumGlyph, 1, 1), "TCT", "LBL", " CT", 'B', Block.blockDiamond, 'L', itemLevitator, 'C', itemCharger, 'T', blockTesla));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemQuantumGlyph, 1, 2), "TC ", "LBL", "TCT", 'B', Block.blockDiamond, 'L', itemLevitator, 'C', itemCharger, 'T', blockTesla));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemQuantumGlyph, 1, 3), "TCT", "LBL", "TC ", 'B', Block.blockDiamond, 'L', itemLevitator, 'C', itemCharger, 'T', blockTesla));

        /** Generators **/
        GameRegistry.addRecipe(new ShapedOreRecipe(blockSolarPanel, "CCC", "WWW", "III", 'W', "wire", 'C', Item.coal, 'I', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockMotor, "SRS", "SMS", "SWS", 'W', "wire", 'R', Item.redstone, 'M', Block.blockIron, 'S', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockThermopile, "ORO", "OWO", "OOO", 'W', "wire", 'O', Block.obsidian, 'R', Item.redstone));

        GameRegistry.addRecipe(new ShapedOreRecipe(itemLaserGun, "RDR", "RDR", "ICB", 'R', Item.redstone, 'D', Item.diamond, 'I', Item.ingotGold, 'C', UniversalRecipe.CIRCUIT_T2.get(), 'B', ItemBlockBattery.setTier(new ItemStack(blockBattery, 1, 0), (byte) 0)));

        /** Wire Compatiblity **/
        if (Loader.isModLoaded("IC2"))
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), Items.getItem("copperCableItem")));
            GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.TIN.getWire(), Items.getItem("tinCableItem")));
            GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.IRON.getWire(), Items.getItem("ironCableItem")));
            GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.SUPERCONDUCTOR.getWire(), Items.getItem("glassFiberCableItem")));
        }

        if (Loader.isModLoaded("Mekanism"))
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), "universalCable"));
        }

        proxy.postInit();
        modproxies.postInit();
    }
}
