package resonantinduction.core;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.SoundHandler;
import resonantinduction.core.multimeter.PartMultimeter;
import resonantinduction.core.render.BlockRenderingHandler;
import resonantinduction.core.render.RenderRIItem;
import resonantinduction.energy.battery.RenderBattery;
import resonantinduction.energy.battery.TileBattery;
import resonantinduction.energy.fx.FXElectricBolt;
import resonantinduction.energy.gui.GuiMultimeter;
import resonantinduction.energy.tesla.RenderTesla;
import resonantinduction.energy.tesla.TileTesla;
import resonantinduction.mechanics.item.ItemDust;
import resonantinduction.transport.levitator.RenderLevitator;
import resonantinduction.transport.levitator.TileEMLevitator;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public boolean isPaused()
	{
		if (FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient().getIntegratedServer().getPublic())
		{
			GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

			if (screen != null)
			{
				if (screen.doesGuiPauseGame())
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isFancy()
	{
		return FMLClientHandler.instance().getClient().gameSettings.fancyGraphics;
	}
}
