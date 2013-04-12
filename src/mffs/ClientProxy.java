package mffs;

import mffs.render.RenderBlockHandler;
import mffs.render.RenderFortronCapacitor;
import mffs.tileentity.TileEntityFortronCapacitor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		super.preInit();
		MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
	}

	@Override
	public void init()
	{
		super.init();
		RenderingRegistry.registerBlockHandler(new RenderBlockHandler());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFortronCapacitor.class, new RenderFortronCapacitor());
	}

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
}