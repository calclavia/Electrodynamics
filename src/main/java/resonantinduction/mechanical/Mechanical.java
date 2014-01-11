package resonantinduction.mechanical;

import net.minecraft.block.Block;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.mechanical.belt.BlockConveyorBelt;
import resonantinduction.mechanical.belt.TileConveyorBelt;
import resonantinduction.mechanical.fluid.pipe.BlockPipe;
import resonantinduction.mechanical.fluid.pipe.TilePipe;
import resonantinduction.mechanical.fluid.pump.BlockGrate;
import resonantinduction.mechanical.fluid.pump.BlockPump;
import resonantinduction.mechanical.fluid.pump.TileGrate;
import resonantinduction.mechanical.fluid.pump.TilePump;
import resonantinduction.mechanical.fluid.tank.BlockTank;
import resonantinduction.mechanical.fluid.tank.TileTank;
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

/**
 * Resonant Induction Archaic Module
 * 
 * @author DarkCow, Calclavia
 * 
 */
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

	// Transport
	public static Block blockConveyorBelt;

	// #Fluids
	public static Block blockTank;
	public static Block blockPipe;
	public static Block blockGrate;
	public static Block blockPump;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		blockConveyorBelt = contentRegistry.createTile(BlockConveyorBelt.class, TileConveyorBelt.class);

		blockTank = contentRegistry.createTile(BlockTank.class, TileTank.class);
		blockPipe = contentRegistry.createTile(BlockPipe.class, TilePipe.class);
		blockGrate = contentRegistry.createTile(BlockGrate.class, TileGrate.class);
		blockPump = contentRegistry.createTile(BlockPump.class, TilePump.class);
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		Settings.setModMetadata(metadata, ID, NAME);
		proxy.init();
	}
}
