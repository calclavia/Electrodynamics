package mffs;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class SoundHandler
{
	public static final SoundHandler INSTANCE = new SoundHandler();

	public static final String[] SOUND_FILES = { "fieldmove5.ogg", "fieldmove4.ogg", "fieldmove3.ogg", "fieldmove2.ogg", "fieldmove1.ogg", "field1.ogg", "field2.ogg", "field3.ogg", "field4.ogg", "field5.ogg" };

	@ForgeSubscribe
	public void loadSoundEvents(SoundLoadEvent event)
	{
		for (int i = 0; i < SOUND_FILES.length; i++)
		{
			event.manager.soundPoolSounds.addSound(ModularForceFieldSystem.PREFIX + SOUND_FILES[i]);
		}
	}
}