package com.thesquadmc.sellchest.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class UtilItem
{
    private UtilItem() {}

    public static void giveItem(Player player, ItemStack stack)
    {
        Inventory inventory = player.getInventory();

        if (inventory.firstEmpty() == -1)
        {
            player.getWorld().dropItem(player.getLocation(), stack);
        }
        else
        {
            player.getInventory().addItem(stack);
        }
    }

    public static String getName(ItemStack stack)
    {
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName())
        {
            return stack.getType().name();
        }

        return stack.getItemMeta().getDisplayName();
    }

    public static ItemStack createStack(FileConfiguration config, String path)
    {
        Material material = Material.STONE;
        int amount = 1;

        ConfigurationSection section = config.getConfigurationSection(path);

        try
        {
            material = Material.valueOf(section.getString("type").toUpperCase());
        }
        catch (IllegalArgumentException ignored)
        {
            Util.log("Invalid material: " + section.getString("type"));
        }

        int data = section.getInt("data");

        String name = Util.colour(section.getString("name"));
        List<String> lore = Util.colourList(section.getStringList("lore"));

        boolean glowing = section.getBoolean("glowing");

        ItemStack stack = new ItemStack(material, amount, (byte) data);
        ItemMeta meta = stack.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        if (glowing)
        {
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        stack.setItemMeta(meta);
        return stack;
    }
}
