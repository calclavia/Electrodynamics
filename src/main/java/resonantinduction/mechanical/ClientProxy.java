package resonantinduction.mechanical;

import net.minecraftforge.client.MinecraftForgeClient;
import resonantinduction.mechanical.fluid.pipe.ItemPipeRenderer;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;
import resonantinduction.mechanical.fluid.pipe.TilePipe;
import resonantinduction.mechanical.fluid.tank.ItemTankRenderer;
import resonantinduction.mechanical.fluid.tank.RenderTank;
import resonantinduction.mechanical.fluid.tank.TileTank;
import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		MinecraftForgeClient.registerItemRenderer(Mechanical.blockTank.blockID, new ItemTankRenderer());
		MinecraftForgeClient.registerItemRenderer(Mechanical.blockPipe.blockID, new ItemPipeRenderer());
		MinecraftForgeClient.registerItemRenderer(Mechanical.blockReleaseValve.blockID, new ItemPipeRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TilePipe.class, new RenderPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, RenderTank.INSTANCE);
	}
}
