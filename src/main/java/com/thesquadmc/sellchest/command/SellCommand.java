package com.thesquadmc.sellchest.command;

import com.thesquadmc.sellchest.message.Message;
import com.thesquadmc.sellchest.util.Util;
import com.thesquadmc.sellchest.SellChest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class SellCommand implements CommandExecutor, Listener
{
    private final SellChest plugin;

    public SellCommand(SellChest plugin)
    {
        this.plugin = plugin;

        this.invName = Util.colour(plugin.configGet("inventory.name"));
        this.invSize = plugin.configGet("inventory.size");

        Util.registerListener(this);
    }

    private final String invName;
    private final int invSize;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            Util.message(sender, Message.NO_CONSOLE);
            return true;
        }

        handleArgs((Player) sender, args);
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        if (inventory.getTitle().equals(invName))
        {
            plugin.getEconomy().sellInventory(player, inventory);
        }
    }

    private Inventory getInventory()
    {
        return Bukkit.createInventory(null, invSize, invName);
    }

    private void handleArgs(Player player, String[] args)
    {
        if (!player.hasPermission("sellchest.command.sell"))
        {
            Util.message(player, Message.NO_PERMISSION);
            return;
        }

        player.openInventory(getInventory());
    }
}
