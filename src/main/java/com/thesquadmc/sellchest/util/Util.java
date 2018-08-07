package com.thesquadmc.sellchest.util;

import com.thesquadmc.sellchest.SellChest;
import com.thesquadmc.sellchest.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Level;

public final class Util
{
    private Util() {}

    private static final SellChest plugin = JavaPlugin.getPlugin(SellChest.class);

    public static void registerListener(Listener listener)
    {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public static List<String> colourList(List<String> list)
    {
        list.replaceAll(Util::colour);
        return list;
    }

    public static String colour(String input)
    {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static void message(CommandSender sender, Message message)
    {
        sender.sendMessage(message.value());
    }

    public static void log(String msg)
    {
        plugin.getLogger().info(msg);
    }

    public static void logErr(String msg)
    {
        plugin.getLogger().log(Level.WARNING, msg);
    }

    public static void logFatal(String msg)
    {
        plugin.getLogger().severe(msg);
    }

    public static double round(double value, int places)
    {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
