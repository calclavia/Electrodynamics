package resonantinduction.core;

import java.awt.Color;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.lib.render.fx.FxLaser;
import universalelectricity.api.vector.IVector3;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** @author Calclavia */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
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
    public boolean isGraphicsFancy()
    {
        return FMLClientHandler.instance().getClient().gameSettings.fancyGraphics;
    }

    @Override
    public void renderBlockParticle(World world, Vector3 position, Vector3 velocity, int blockID, float scale)
    {
        this.renderBlockParticle(world, position.x, position.y, position.z, velocity, blockID, scale);
    }

    @Override
    public void renderBlockParticle(World world, double x, double y, double z, Vector3 velocity, int blockID, float scale)
    {
        EntityFX fx = new EntityDiggingFX(world, x, y, z, velocity.x, velocity.y, velocity.z, Block.blocksList[blockID], 0, 0);
        fx.multipleParticleScaleBy(scale);
        fx.noClip = true;
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
    }

    @Override
    public void renderBeam(World world, IVector3 position, IVector3 hit, Color color, int age)
    {
        renderBeam(world, position, hit, color.getRed(), color.getGreen(), color.getBlue(), age);
    }

    @Override
    public void renderBeam(World world, IVector3 position, IVector3 target, float red, float green, float blue, int age)
    {
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FxLaser(world, position, target, red, green, blue, age));
    }

}
