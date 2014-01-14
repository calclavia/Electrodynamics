package resonantinduction.mechanical;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.mechanical.belt.BlockConveyorBelt;
import resonantinduction.mechanical.belt.TileConveyorBelt;
import resonantinduction.mechanical.fluid.pipe.BlockPipe;
import resonantinduction.mechanical.fluid.pipe.ItemBlockFluidContainer;
import resonantinduction.mechanical.fluid.pipe.TilePipe;
import resonantinduction.mechanical.fluid.pump.BlockGrate;
import resonantinduction.mechanical.fluid.pump.BlockPump;
import resonantinduction.mechanical.fluid.pump.TileGrate;
import resonantinduction.mechanical.fluid.pump.TilePump;
import resonantinduction.mechanical.fluid.tank.BlockTank;
import resonantinduction.mechanical.fluid.tank.TileTank;
import resonantinduction.mechanical.gear.BlockGear;
import resonantinduction.mechanical.gear.TileGear;
import resonantinduction.mechanical.item.ItemPipeGauge;
import resonantinduction.mechanical.logistic.BlockDetector;
import resonantinduction.mechanical.logistic.BlockManipulator;
import resonantinduction.mechanical.logistic.BlockRejector;
import resonantinduction.mechanical.logistic.TileDetector;
import resonantinduction.mechanical.logistic.TileManipulator;
import resonantinduction.mechanical.logistic.TileRejector;
import calclavia.lib.content.ContentRegistry;
import calclavia.lib.network.PacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;

/** Resonant Induction Archaic Module
 * 
 * @author DarkCow, Calclavia */
@Mod(modid = Mechanical.ID, name = Mechanical.NAME, version = Reference.VERSION, dependencies = "required-after:" + ResonantInduction.ID)
@NetworkMod(channels = Reference.CHANNEL, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Mechanical
{
    /** Mod Information */
    public static final String ID = "ResonantInduction|Mechanical";
    public static final String NAME = Reference.NAME + " Mechanical";

    @Instance(ID)
    public static Mechanical INSTANCE;

    @SidedProxy(clientSide = "resonantinduction.mechanical.ClientProxy", serverSide = "resonantinduction.mechanical.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Metadata(ID)
    public static ModMetadata metadata;

    public static final ContentRegistry contentRegistry = new ContentRegistry(Settings.CONFIGURATION, ID);

    // Energy
    public static Block blockGear;
    public static Block blockGearShaft;

    // Transport
    public static Block blockConveyorBelt;
    public static Block blockManipulator;
    public static Block blockDetector;
    public static Block blockRejector;

    // Fluids
    public static Block blockTank;
    public static Block blockPipe;
    public static Block blockReleaseValve;
    public static Block blockGrate;
    public static Block blockPump;

    public static Item itemPipeGuage;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        Settings.load();
        NetworkRegistry.instance().registerGuiHandler(this, proxy);
        blockGear = contentRegistry.createTile(BlockGear.class, TileGear.class);
        //blockGearShaft = contentRegistry.createTile(BlockRejector.class, TileRejector.class);

        blockConveyorBelt = contentRegistry.createTile(BlockConveyorBelt.class, TileConveyorBelt.class);
        blockManipulator = contentRegistry.createTile(BlockManipulator.class, TileManipulator.class);
        blockDetector = contentRegistry.createTile(BlockDetector.class, TileDetector.class);
        blockRejector = contentRegistry.createTile(BlockRejector.class, TileRejector.class);

        blockTank = contentRegistry.createBlock(BlockTank.class, ItemBlockFluidContainer.class, TileTank.class);
        blockPipe = contentRegistry.createBlock(BlockPipe.class, ItemBlockFluidContainer.class, TilePipe.class);
        blockGrate = contentRegistry.createTile(BlockGrate.class, TileGrate.class);
        blockPump = contentRegistry.createTile(BlockPump.class, TilePump.class);

        itemPipeGuage = contentRegistry.createItem("PipeGauge", ItemPipeGauge.class);
        proxy.preInit();
        Settings.save();
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        Settings.setModMetadata(metadata, ID, NAME);
        proxy.init();
    }
}
