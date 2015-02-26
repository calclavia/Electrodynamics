package mffs.render.fx

import mffs.content.Textures
import nova.core.render.Color
import nova.core.util.transform.Vector3d

/**
 * Based off Thaumcraft's Beam Renderer.
 *
 * @author Calclavia, Azanor
 */
class FXFortronBeam(position: Vector3d, target: Vector3d, color: Color, maxAge: Double)
	extends FXBeam(Textures.fortron, position, target, color, maxAge) {
}