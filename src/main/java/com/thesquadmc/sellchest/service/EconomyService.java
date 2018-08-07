package com.thesquadmc.sellchest.service;

import com.earth2me.essentials.Essentials;
import com.thesquadmc.sellchest.SellChest;
import com.thesquadmc.sellchest.message.Message;
import com.thesquadmc.sellchest.util.Util;
import com.thesquadmc.sellchest.util.UtilItem;
import javafx.util.Pair;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.shop.MainMenuItem;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopItem;
import net.brcdev.shopgui.shop.ShopManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EconomyService implements Service
{
    private final SellChest plugin;

    public EconomyService(SellChest plugin)
    {
        this.plugin = plugin;
    }

    private Economy economy;

    @Override
    public boolean init()
    {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (economyProvider != null)
        {
            economy = economyProvider.getProvider();

            initPrices();
        }

        return economy != null;
    }

    public double sellChest(Player player, InventoryHolder holder)
    {
        double amount = getSellPrice(player, holder.getInventory(), unsellable -> {});

        if (amount > 0.0D)
        {
            addMoney(player, amount);
        }

        return amount;
    }

    public void sellInventory(Player player, Inventory inventory)
    {
        double amount = getSellPrice(player, inventory, unsellable ->
            unsellable.forEach(stack -> UtilItem.giveItem(player, stack))
        );

        if (amount > 0.0D)
        {
            addMoney(player, amount);
        }
    }

    private void addMoney(Player player, double amount)
    {
        player.sendMessage(Message.MONEY_EARNED.value().replace("%amount%", String.valueOf(Util.round(amount, 2))));

        economy.depositPlayer(player, amount);
    }

    private Map<Material, Map<Byte, Integer>> prices = new HashMap<>();

    private Essentials essentials;

    private ShopGuiPlugin shopGui;
    private List<MainMenuItem> shopGuiItems;

    private double getSellPrice(Player player, Inventory inventory, Consumer<List<ItemStack>> unsellableConsumer)
    {
        double total = 0;
        List<ItemStack> unsellable = new ArrayList<>();

        for (ItemStack stack : inventory.getContents())
        {
            if (stack == null || stack.getType() == Material.AIR)
            {
                continue;
            }

            if (shopGuiItems != null)
            {
                Pair<Shop, ShopItem> shopPair = getShopAndItem(stack);

                if (shopPair == null)
                {
                    unsellable.add(stack);
                    continue;
                }

                double amount = shopPair.getValue().getSellPriceForAmount(shopPair.getKey(), player, shopGui.getPlayerManager().getPlayerData(player), stack.getAmount());
                total += amount;

                inventory.remove(stack);
            }
            else if (essentials != null)
            {
                BigDecimal worth = essentials.getWorth().getPrice(stack);

                if (worth != null && worth.doubleValue() != 0)
                {
                    total += worth.doubleValue() * stack.getAmount();
                    inventory.remove(stack);

                    continue;
                }

                unsellable.add(stack);
            }
            else
            {

                Map<Byte, Integer> data = prices.get(stack.getType());

                if (data == null)
                {
                    unsellable.add(stack);
                    continue;
                }

                Integer amount = data.get(stack.getData().getData());

                if (amount == null)
                {
                    unsellable.add(stack);
                    continue;
                }

                total += amount * stack.getAmount();
                inventory.remove(stack);
            }
        }

        if (!unsellable.isEmpty())
        {
            Util.message(player, Message.SELL_ERROR);

            unsellableConsumer.accept(unsellable);
        }

        return total;
    }

    private Pair<Shop, ShopItem> getShopAndItem(ItemStack stack)
    {
        for (MainMenuItem mainMenuItem : shopGuiItems)
        {
            Shop shop = shopGui.getShopManager().getShopById(mainMenuItem.getShopId());
            ShopItem shopItem = shop.getShopItems().stream().filter(item -> item.getItem().isSimilar(stack)).findFirst().orElse(null);

            if (shopItem != null)
            {
                return new Pair<>(shop, shopItem);
            }
        }

        return null;
    }

    private final String PRICES_PATH = "worth";

    public static final File PRICES_FILE = new File("plugins" + File.separator + "SellChest" + File.separator + "prices.yml");

    @SuppressWarnings("unchecked")
    private void initPrices()
    {
        if (plugin.configGet("use_shopgui_worth"))
        {
            shopGui = ShopGuiPlugin.getInstance();

            try
            {
                Field mainMenuField = ShopManager.class.getDeclaredField("mainMenuItems");
                mainMenuField.setAccessible(true);

                shopGuiItems = (List<MainMenuItem>) mainMenuField.get(shopGui.getShopManager());
            }
            catch (ReflectiveOperationException ex)
            {
                Util.logFatal("Could not load ShopGUI prices");
            }

            return;
        }

        if (plugin.configGet("use_essentials_worth"))
        {
            Plugin essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");

            if (essPlugin == null)
            {
                Util.logErr("Could not find Essentials plugin to get worth.yml, defaulting ro prices.yml");
            }
            else
            {
                essentials = (Essentials) essPlugin;
                return;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(PRICES_FILE);

        for (String key : config.getConfigurationSection(PRICES_PATH).getKeys(false))
        {
            Material material;

            try
            {
                material = Material.valueOf(key.toUpperCase());
            }
            catch (IllegalArgumentException ex)
            {
                continue;
            }

            prices.putIfAbsent(material, new HashMap<>());

            for (String sub : config.getConfigurationSection(PRICES_PATH + "." + key).getKeys(false))
            {
                int data;

                try
                {
                    data = Integer.parseInt(sub);
                }
                catch (NumberFormatException ex)
                {
                    continue;
                }

                int price;

                try
                {
                    price = config.getInt(PRICES_PATH + "." + key + "." + data);
                }
                catch (NumberFormatException ex)
                {
                    continue;
                }

                prices.get(material).put((byte) data, price);
            }
        }
    }
}
