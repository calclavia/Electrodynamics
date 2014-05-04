/**
 * 
 */
package resonantinduction.core;

import java.awt.Color;

import net.minecraft.world.World;
import universalelectricity.api.vector.IVector3;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.ProxyBase;

/** @author Calclavia */
public class CommonProxy extends ProxyBase
{
    public boolean isPaused()
    {
        return false;
    }

    public boolean isGraphicsFancy()
    {
        return false;
    }

    public void renderBlockParticle(World world, double x, double y, double z, Vector3 velocity, int blockID, float scale)
    {

    }

    public void renderBlockParticle(World world, Vector3 position, Vector3 velocity, int blockID, float scale)
    {

    }

    public void renderBeam(World world, IVector3 position, IVector3 hit, Color color, int age)
    {
    }

    public void renderBeam(World world, IVector3 position, IVector3 target, float red, float green, float blue, int age)
    {

    }

}
