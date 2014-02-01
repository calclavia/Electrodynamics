/**
 * 
 */
package resonantinduction.core;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class SoundHandler
{
	public static final SoundHandler INSTANCE = new SoundHandler();

	public static final String[] SOUND_FILES = { "hammer.ogg", "grinder1.ogg", "grinder2.ogg", "grinder3.ogg", "grinder4.ogg", "electricshock1.ogg", "electricshock2.ogg", "electricshock3.ogg", "electricshock4.ogg", "electricshock5.ogg", "electricshock6.ogg", "electricshock7.ogg", "conveyor.ogg" };

	@ForgeSubscribe
	public void loadSoundEvents(SoundLoadEvent event)
	{
		for (int i = 0; i < SOUND_FILES.length; i++)
		{
			event.manager.addSound(Reference.PREFIX + SOUND_FILES[i]);
		}

		ResonantInduction.LOGGER.fine("Loaded sound fxs");
	}
}
