package mffs

import java.util.Set

import cpw.mods.fml.client.IModGuiFactory
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen

/**
 * Handles the config GUI
 * @author Calclavia
 */
class MFFSGuiFactory extends IModGuiFactory
{
  override def initialize(minecraftInstance: Minecraft)
  {
  }

  override def mainConfigGuiClass: Class[_ <: GuiScreen] =
  {
    return classOf[Nothing]
  }

  override def runtimeGuiCategories: Set[IModGuiFactory.RuntimeOptionCategoryElement] =
  {
    return null
  }

  /**
   * Return an instance of a {@link RuntimeOptionGuiHandler} that handles painting the
   * right hand side option screen for the specified {@link RuntimeOptionCategoryElement}.
   *
   * @param element The element we wish to paint for
   * @return The Handler for painting it
   */
  override def getHandlerFor(element: IModGuiFactory.RuntimeOptionCategoryElement): IModGuiFactory.RuntimeOptionGuiHandler =
  {
    return null
  }
}
