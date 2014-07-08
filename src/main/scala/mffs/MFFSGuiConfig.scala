package mffs

import cpw.mods.fml.client.config.GuiConfig
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.config.{ConfigElement, Configuration}

/**
 * @author Calclavia
 */
class MFFSGuiConfig(parent: GuiScreen) extends GuiConfig(parent, new ConfigElement(Settings.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), Reference.id, false, false, GuiConfig.getAbridgedConfigPath(Settings.config.toString()))
{

}
