package mffs.render.fx

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
class FXMFFS(par1World: World, par2: Double, par4: Double, par6: Double, par8: Double = 0, par10: Double = 0, par12: Double = 0) extends EntityFX(par1World, par2, par4, par6, par8, par10, par12)
{
  protected var controller: IEffectController = null

  override def onUpdate
  {
    if (this.controller != null)
    {
      if (!this.controller.canContinueEffect)
      {
        this.setDead
      }
    }
  }

  def setController(controller: IEffectController): FXMFFS =
  {
    this.controller = controller
    return this
  }
}