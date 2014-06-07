package mffs.render;

import mffs.ModularForceFieldSystem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;
import resonant.lib.render.fx.FxBeam;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Based off Thaumcraft's Beam Renderer.
 * 
 * @author Calclavia, Azanor
 * 
 */
@SideOnly(Side.CLIENT)
public class FxFortronBeam extends FxBeam
{
	public FxFortronBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age)
	{
		super(new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.BLOCK_DIRECTORY + "fortron.png"), world, position, target, red, green, blue, age);
	}
}