package resonantinduction.core.interfaces;

import net.minecraft.command.ICommandSender;

/**
 * Used by machines that can be control by chat commands. Mainly for dev debug of the machine.
 *
 * @author robert
 */
public interface ICmdMachine
{
	/**
	 * Pre-check too see if this machine can even process the command
	 */
	public boolean canTakeCommand(ICommandSender sender, String[] args);

	/**
	 * Processing of the command
	 */
	public void processCommand(ICommandSender sender, String[] args);
}
