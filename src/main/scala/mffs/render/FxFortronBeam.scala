package mffs.render

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mffs.ModularForceFieldSystem
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import resonant.lib.render.fx.FxBeam
import universalelectricity.api.vector.Vector3

/**
 * Based off Thaumcraft's Beam Renderer.
 *
 * @author Calclavia, Azanor
 */
@SideOnly(Side.CLIENT)
class FxFortronBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int) extends FxBeam(new ResourceLocation(ModularForceFieldSystem.DOMAIN, ModularForceFieldSystem.BLOCK_DIRECTORY + "fortron.png"), world, position, target, red, green, blue, age)
{
	noClip = true
}