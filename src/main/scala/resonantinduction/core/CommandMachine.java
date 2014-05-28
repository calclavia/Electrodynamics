package resonantinduction.core;

import java.util.HashMap;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.interfaces.ICmdMachine;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.vector.VectorWorld;

/** Command that allows interaction with machines using chat commands
 * 
 * @author Darkguardsman */
public class CommandMachine extends CommandBase
{
    public static HashMap<String, VectorWorld> selection = new HashMap<String, VectorWorld>();

    @Override
    public String getCommandName()
    {
        return "machine";
    }

    @Override
    public String getCommandUsage(ICommandSender user)
    {
        return "/machine ?";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args != null && args.length > 0 && args[0] != null)
        {
            if (args[0].equalsIgnoreCase("?"))
            {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("/machine <arguments....>"));
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Each machine has unique commands"));
            }
            else if (sender instanceof EntityPlayer)
            {
                if (args.length > 1 && args[1] != null)
                {
                    final String command = args[1];
                    final boolean hasSubCommand = args.length > 2 && args[2] != null;
                    final boolean hasSecondSub = args.length > 3 && args[3] != null;
                    final boolean hasThirdSub = args.length > 3 && args[3] != null;
                    final String subCommand = hasSubCommand ? args[2] : null;
                    final String subCommand2 = hasSecondSub ? args[3] : null;
                    final String subCommand3 = hasThirdSub ? args[4] : null;

                    if (selection.containsKey(((EntityPlayer) sender).username) && selection.get(((EntityPlayer) sender).username) != null)
                    {
                        VectorWorld pos = selection.get(((EntityPlayer) sender).username);
                        TileEntity tile = pos.getTileEntity();
                        if (tile instanceof ICmdMachine)
                        {
                            if (((ICmdMachine) tile).canTakeCommand(sender, args))
                            {
                                ((ICmdMachine) tile).processCommand(sender, args);
                            }
                            else
                            {
                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Machine refuses the command"));
                            }
                        }
                        else if (CompatibilityModule.isHandler(tile))
                        {
                            if (command.equalsIgnoreCase("energy"))
                            {
                                if (!hasSubCommand)
                                {
                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("/Machine energy set <side> <amount>"));
                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("/Machine energy get <side>"));
                                }
                                else if (subCommand.equalsIgnoreCase("get") || subCommand.equalsIgnoreCase("set"))
                                {
                                    if (hasSecondSub)
                                    {
                                        ForgeDirection direction = getDirection(subCommand2);
                                        if (direction != null)
                                        {
                                            if (subCommand.equalsIgnoreCase("get"))
                                            {
                                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Energy: " + CompatibilityModule.getEnergy(tile, direction) + "/" + CompatibilityModule.getMaxEnergy(tile, direction)));
                                            }
                                            else if (subCommand.equalsIgnoreCase("set"))
                                            {
                                                if (hasThirdSub)
                                                {
                                                    long joules = Long.getLong(subCommand3, -33);
                                                    if (joules >= 0)
                                                    {
                                                        long ex = CompatibilityModule.extractEnergy(tile, direction, Long.MAX_VALUE, false);
                                                        if (ex == CompatibilityModule.extractEnergy(tile, direction, Long.MAX_VALUE, true))
                                                        {
                                                            CompatibilityModule.receiveEnergy(tile, direction, joules, true);
                                                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Energy set"));
                                                        }
                                                        else
                                                        {
                                                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Failed to set energy! Maybe try a different side?"));
                                                        }
                                                    }
                                                }
                                                else
                                                {
                                                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("Supply an energy value"));
                                                }
                                            }
                                            else
                                            {
                                                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Unknown energy command"));
                                            }
                                        }
                                        else
                                        {
                                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Need to supply a side"));
                                        }
                                    }
                                    else
                                    {
                                        sender.sendChatToPlayer(ChatMessageComponent.createFromText("Need to supply a side"));
                                    }
                                }
                            }
                        }
                        else
                        {
                            sender.sendChatToPlayer(ChatMessageComponent.createFromText("Invalid machine selected!"));
                            selection.remove(((EntityPlayer) sender).username);
                        }
                    }
                    else
                    {
                        sender.sendChatToPlayer(ChatMessageComponent.createFromText("Please supply some arguments"));
                    }
                }
                else
                {
                    sender.sendChatToPlayer(ChatMessageComponent.createFromText("Please select the machine first"));
                }
            }
            else
            {
                sender.sendChatToPlayer(ChatMessageComponent.createFromText("Console access not supported"));
            }
        }
    }

    public ForgeDirection getDirection(String str)
    {
        if (str != null && !str.isEmpty())
        {
            //Get side from work input
            if (str.equalsIgnoreCase("north") || str.equalsIgnoreCase("n"))
            {
                return ForgeDirection.NORTH;
            }
            else if (str.equalsIgnoreCase("south") || str.equalsIgnoreCase("s"))
            {
                return ForgeDirection.SOUTH;
            }
            else if (str.equalsIgnoreCase("east") || str.equalsIgnoreCase("e"))
            {
                return ForgeDirection.EAST;
            }
            else if (str.equalsIgnoreCase("west") || str.equalsIgnoreCase("w"))
            {
                return ForgeDirection.WEST;
            }
            else if (str.equalsIgnoreCase("up") || str.equalsIgnoreCase("u"))
            {
                return ForgeDirection.UP;
            }
            else if (str.equalsIgnoreCase("down") || str.equalsIgnoreCase("d"))
            {
                return ForgeDirection.DOWN;
            }

            //Get side from number input
            int side = Integer.getInteger(str, -3);
            if (side >= 0 && side < 6)
            {
                return ForgeDirection.getOrientation(side);
            }
        }
        return null;
    }
}
