package mffs.render.fx

import mffs.Reference

/**
 * Based off Thaumcraft's Beam Renderer.
 *
 * @author Calclavia, Azanor
 */
@SideOnly(Side.CLIENT)
class FXFortronBeam(world: World, position: Vector3d, target: Vector3d, red: Float, green: Float, blue: Float, age: Int)
	extends FXBeam(new ResourceLocation(Reference.domain, Reference.blockDirectory + "fortron.png"), world, position, target, red, green, blue, age)
{
  noClip = true
}