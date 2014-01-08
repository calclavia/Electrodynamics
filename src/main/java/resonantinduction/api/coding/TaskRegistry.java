package resonantinduction.api.coding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import resonantinduction.api.IArmbot;

/**
 * Used to both register task and fake machines for the encoder to use to create new programs.
 * 
 * @author DarkGuardsman
 */
public class TaskRegistry
{
	/**
	 * A class of all available commands.
	 * 
	 * String - Command name. Command - The actual command class.
	 */
	private static final HashMap<String, ITask> COMMANDS = new HashMap();

	private static final HashMap<String, IArmbot> SUDO_BOTS = new HashMap<String, IArmbot>();

	/** Registers a command and tells armbots that it exists */
	public static void registerCommand(ITask task)
	{
		if (!COMMANDS.containsKey(task.getMethodName()))
		{
			COMMANDS.put(task.getMethodName(), task);
		}
	}

	public static void registerCommand(String registryName, IProcessTask task)
	{
		if (!COMMANDS.containsKey(registryName))
		{
			COMMANDS.put(registryName, task);
		}
	}

	/** returns the first command with the same name */
	public static ITask getCommand(String name)
	{
		for (Entry<String, ITask> command : COMMANDS.entrySet())
		{
			if (command.getKey().equalsIgnoreCase(name))
			{
				return command.getValue();
			}
		}
		return null;
	}

	/** Gets all commands with the given name though there should only be one */
	public static List<ITask> getCommands(String name)
	{
		List<ITask> tasks = new ArrayList<ITask>();
		for (Entry<String, ITask> command : COMMANDS.entrySet())
		{
			if (command.getValue().getMethodName().equalsIgnoreCase(name))
			{
				tasks.add(command.getValue());
			}
		}
		return tasks;
	}

	/**
	 * Don't actually register the real machine. Register a fake version so that a code can use it
	 * for simulations
	 */
	public static void registerMachine(String name, IArmbot bot)
	{
		if (!SUDO_BOTS.containsKey(name))
		{
			SUDO_BOTS.put(name, bot);
		}
	}

	/**
	 * Do not edit the return or you will change the behavior of all machine that use this list
	 * 
	 * @return The list of registered sudo machines for the encoder to check against
	 */
	public static HashMap<String, IArmbot> getSudoMachines()
	{
		return SUDO_BOTS;
	}

	/** Get one of the sudo bots in the hashmap. Make sure to clone before editing */
	public static IArmbot getBot(String string)
	{
		return SUDO_BOTS.get(string);
	}
}
