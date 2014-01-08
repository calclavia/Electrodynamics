package com.builtbroken.minecraft.interfaces;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Prefab for creating commands that most terminal entities can use
 * 
 * @author DarkGuardsman
 */
public interface ITerminalCommand
{
	/**
	 * The command has been called by a player in a terminal.
	 * 
	 * @return false if the call was not supported rather than failed. Used too allow several
	 * commands with the same name to exist but each has its own sub calls
	 */
	public boolean called(EntityPlayer player, ITerminal terminal, String[] args);

	/**
	 * Can the machine use the command. Used to prevent commands from being called on machines that
	 * can't support it
	 */
	public boolean canSupport(ITerminal terminal);

	/** What the command starts with like /time */
	public String getCommandName();

	/**
	 * Used to restrict sub commands like /time day, or /time night. Will be added to the name of
	 * the command so a command called time will have a sub comamnd day its node will equal time.day
	 */
	public Set<String> getPermissionNodes();

	public String getNode(String[] args);
}
