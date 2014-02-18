package resonantinduction.archaic;

import net.minecraftforge.client.MinecraftForgeClient;
import resonantinduction.archaic.channel.ItemChannelRenderer;
import resonantinduction.archaic.channel.RenderChannel;
import resonantinduction.archaic.channel.TileChannel;
import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileChannel.class, RenderChannel.INSTANCE);
    }

    @Override
    public void init()
    {
        MinecraftForgeClient.registerItemRenderer(Archaic.blockChannel.blockID, new ItemChannelRenderer());
    }
}
