package com.thesquadmc.sellchest.command;

import com.thesquadmc.sellchest.message.Message;
import com.thesquadmc.sellchest.util.Util;
import com.thesquadmc.sellchest.SellChest;
import com.thesquadmc.sellchest.util.UtilItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellWandCommand implements CommandExecutor
{
    private final SellChest plugin;

    public SellWandCommand(SellChest plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        handleArgs(sender, args);
        return true;
    }

    private void handleArgs(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("sellchest.command.give"))
        {
            Util.message(sender, Message.NO_PERMISSION);
            return;
        }

        if (args.length != 3 || !args[0].equalsIgnoreCase("give"))
        {
            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null)
        {
            Util.message(sender, Message.GIVE_COMMAND_NOT_ONLINE);
            return;
        }

        int uses;

        try
        {
            uses = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException ex)
        {
            Util.message(sender, Message.GIVE_COMMAND_INVALID_NUMBER);
            return;
        }

        if (uses < 1)
        {
            Util.message(sender, Message.GIVE_COMMAND_TOO_SMALL);
            return;
        }

        sender.sendMessage(Message.GIVE_COMMAND_GIVEN.value().replace("%player%", target.getName()).replace("%uses%", String.valueOf(uses)));
        target.sendMessage(Message.GIVE_COMMAND_RECEIVED.value().replace("%player%", sender.getName()).replace("%uses%", String.valueOf(uses)));

        ItemStack stack = plugin.getNewWand(uses, uses);
        UtilItem.giveItem(target, stack);
    }

    private void sendUsage(CommandSender sender)
    {
        Util.message(sender, Message.GIVE_COMMAND_USAGE);
    }
}
