package com.thesquadmc.sellchest;

import com.thesquadmc.sellchest.command.SellCommand;
import com.thesquadmc.sellchest.command.SellWandCommand;
import com.thesquadmc.sellchest.message.Message;
import com.thesquadmc.sellchest.service.WorldGuardService;
import com.thesquadmc.sellchest.util.Util;
import com.thesquadmc.sellchest.listener.InteractListener;
import com.thesquadmc.sellchest.listener.SignListener;
import com.thesquadmc.sellchest.service.EconomyService;
import com.thesquadmc.sellchest.util.UtilItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class SellChest extends JavaPlugin
{
    private EconomyService economy;
    private WorldGuardService worldGuard;

    private ItemStack sellWand;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();

        if (!EconomyService.PRICES_FILE.exists())
        {
            saveResource("prices.yml", false);
        }

        Message.init(this);
        initServices();
    }

    private void initServices()
    {
        economy = new EconomyService(this);

        if (!economy.init())
        {
            Util.logFatal("Unable to connect to economy through Vault");

            disable();
            return;
        }

        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("sellwand").setExecutor(new SellWandCommand(this));

        sellWand = UtilItem.createStack(getConfig(), "sell_wand");

        (worldGuard = new WorldGuardService(this)).init();

        Util.registerListener(new SignListener(this));
        Util.registerListener(new InteractListener(this));
    }

    public boolean isSellWand(ItemStack stack)
    {
        return stack.getType() == sellWand.getType()
            && UtilItem.getName(stack).equals(sellWand.getItemMeta().getDisplayName());
    }

    public ItemStack getNewWand(int uses, int max)
    {
        ItemStack stack = sellWand.clone();

        ItemMeta meta = stack.getItemMeta();
        List<String> newLore = stack.getItemMeta().getLore()
            .stream()
            .map(line -> line.replace("%available%", String.valueOf(uses)).replace("%max%", String.valueOf(max)))
            .collect(Collectors.toList());

        meta.setLore(newLore);
        stack.setItemMeta(meta);

        return stack;
    }

    public EconomyService getEconomy()
    {
        return economy;
    }

    public WorldGuardService getWorldGuard()
    {
        return worldGuard;
    }

    private void disable()
    {
        getServer().getPluginManager().disablePlugin(this);
    }

    public <T> T configGet(String path)
    {
        return configGet(path, null);
    }

    @SuppressWarnings("unchecked")
    private <T> T configGet(String path, Object defaultValue)
    {
        return (T) getConfig().get(path, defaultValue);
    }
}
