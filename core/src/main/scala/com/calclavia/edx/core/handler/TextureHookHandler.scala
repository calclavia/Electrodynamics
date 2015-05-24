package com.calclavia.edx.core.handler

import com.calclavia.edx.core.Reference
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.{Side, SideOnly}
import Reference
import edx.quantum.QuantumContent
import net.minecraftforge.client.event.TextureStitchEvent
import resonantengine.lib.render.RenderUtility

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
    QuantumContent.fluidUraniumHexaflouride.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "uraniumHexafluoride"))
    QuantumContent.fluidSteam.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "steam"))
    QuantumContent.FLUID_DEUTERIUM.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "deuterium"))
    QuantumContent.getFluidTritium.setIcons(RenderUtility.loadedIconMap.get(Reference.prefix + "tritium"))
    QuantumContent.getFluidToxicWaste.setIcons(QuantumContent.blockToxicWaste.getIcon(0, 0))
    QuantumContent.FLUID_PLASMA.setIcons(QuantumContent.blockPlasma.getIcon(0, 0))
  }

}