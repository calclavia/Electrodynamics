package resonantinduction.core.handler

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.client.event.TextureStitchEvent
import resonant.lib.render.RenderUtility
import resonantinduction.atomic.AtomicContent
import resonantinduction.core.Reference

/** Event handler for texture events
  * @author Calclavia
  */
object TextureHookHandler
{
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def preTextureHook(event: TextureStitchEvent.Pre)
  {
    if (event.map.getTextureType == 0)
    {
      RenderUtility.registerIcon(Reference.prefix + "glyph_0", event.map)
      RenderUtility.registerIcon(Reference.prefix + "glyph_1", event.map)
      RenderUtility.registerIcon(Reference.prefix + "glyph_2", event.map)
      RenderUtility.registerIcon(Reference.prefix + "glyph_3", event.map)
      RenderUtility.registerIcon(Reference.prefix + "mixture_flow", event.map)
      RenderUtility.registerIcon(Reference.prefix + "molten_flow", event.map)
      RenderUtility.registerIcon(Reference.prefix + "multimeter_screen", event.map)
      RenderUtility.registerIcon(Reference.prefix + "tankEdge", event.map)

      RenderUtility.registerIcon(Reference.prefix + "uraniumHexafluoride", event.map)
      RenderUtility.registerIcon(Reference.prefix + "steam", event.map)
      RenderUtility.registerIcon(Reference.prefix + "deuterium", event.map)
      RenderUtility.registerIcon(Reference.prefix + "tritium", event.map)
      RenderUtility.registerIcon(Reference.prefix + "atomic_edge", event.map)
      RenderUtility.registerIcon(Reference.prefix + "funnel_edge", event.map)
      RenderUtility.registerIcon(Reference.prefix + "glass", event.map)

    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def postTextureHook(event: TextureStitchEvent.Post)
  {
    AtomicContent.FLUID_URANIUM_HEXAFLOURIDE.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "uraniumHexafluoride"))
    AtomicContent.FLUID_STEAM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "steam"))
    AtomicContent.FLUID_DEUTERIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "deuterium"))
    AtomicContent.getFluidTritium.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "tritium"))
    AtomicContent.getFluidToxicWaste.setIcons(AtomicContent.blockToxicWaste.getIcon(0, 0))
    AtomicContent.FLUID_PLASMA.setIcons(AtomicContent.blockPlasma.getIcon(0, 0))
  }

}