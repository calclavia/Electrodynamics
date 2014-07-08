package mffs.render.fx

import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Reference
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import resonant.lib.render.fx.FXBeam
import universalelectricity.core.transform.vector.Vector3

/**
 * Based off Thaumcraft's Beam Renderer.
 *
 * @author Calclavia, Azanor
 */
@SideOnly(Side.CLIENT)
class FXFortronBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int) extends FXBeam(new ResourceLocation(Reference.domain, Reference.blockDirectory + "fortron.png"), world, position, target, red, green, blue, age)
{
  noClip = true
}