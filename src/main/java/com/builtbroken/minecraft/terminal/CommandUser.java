package com.builtbroken.minecraft.terminal;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import calclavia.lib.access.ISpecialAccess;

import com.builtbroken.minecraft.interfaces.ITerminal;
import com.builtbroken.minecraft.interfaces.ITerminalCommand;

public class CommandUser implements ITerminalCommand
{
	@Override
	public String getCommandName()
	{
		return "users";
	}

	@Override
	public boolean called(EntityPlayer player, ITerminal terminal, String[] args)
	{
		if (args[0].equalsIgnoreCase("users") && args.length > 1 && args[1] != null && terminal instanceof ISpecialAccess)
		{
			ISpecialAccess turret = terminal;

			// ILockable
			if (args[1].equalsIgnoreCase("List"))
			{
				terminal.addToConsole("");
				terminal.addToConsole("Listing Users");
				for (int i = 0; i < turret.getUsers().size(); i++)
				{
					terminal.addToConsole(" " + i + ") " + turret.getUsers().get(i).getName());
				}
				return true;
			}
			if (args[1].equalsIgnoreCase("remove") && args.length > 2)
			{
				if (args[2] != null)
				{
					if (turret.setUserAccess(args[2], null, false))
					{
						terminal.addToConsole("Removed: " + args[2]);
						return true;
					}
					else
					{
						terminal.addToConsole(" User not found.");
						return true;
					}
				}
				else
				{
					terminal.addToConsole("Invalid username.");
					return true;
				}
			}
			if (args[1].equalsIgnoreCase("add") && args.length > 2)
			{
				if (args[2] != null && terminal.getGroup(args[2]) != null)
				{
					if (args.length > 3)
					{
						if (terminal.getGroup(args[2]).isMemeber(args[3]))
						{
							terminal.addToConsole("User already exists.");
							return true;
						}
						else if (turret.setUserAccess(args[3], terminal.getGroup(args[2]), true))
						{
							terminal.addToConsole("Added: " + args[3] + " to group " + args[2]);
							return true;
						}
						else
						{
							terminal.addToConsole("Invalid username.");
							return true;
						}
					}
				}
				else
				{
					terminal.addToConsole("Invalid group.");
					return true;
				}
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean canSupport(ITerminal mm)
	{
		return mm != null;
	}

	@Override
	public Set<String> getPermissionNodes()
	{
		Set<String> nodes = new HashSet<String>();
		nodes.add("add");
		nodes.add("remove");
		nodes.add("list");
		return nodes;
	}

	@Override
	public String getNode(String[] args)
	{
		if (args != null && args.length >= 1)
		{
			if (args[0] != null && args[0].equalsIgnoreCase(this.getCommandName()))
			{
				if (args.length >= 2)
				{
					if (args[1] != null && args[1].equalsIgnoreCase("add"))
					{
						return "users.add";
					}
					if (args[1] != null && args[1].equalsIgnoreCase("remove"))
					{
						return "users.remove";
					}
					if (args[1] != null && args[1].equalsIgnoreCase("list"))
					{
						return "users.list";
					}
				}
				return "users";
			}
		}
		return null;
	}
}
