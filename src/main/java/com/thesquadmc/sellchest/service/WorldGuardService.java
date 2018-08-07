package com.thesquadmc.sellchest.service;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.thesquadmc.sellchest.SellChest;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

public class WorldGuardService implements Service
{
    private final SellChest plugin;

    public WorldGuardService(SellChest plugin)
    {
        this.plugin = plugin;
    }

    private WorldGuardPlugin worldGuard;

    @Override
    public boolean init()
    {
        worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        return worldGuard != null;
    }

    public boolean canUseChest(Player player, Location location)
    {
        return worldGuard.canBuild(player, location);
    }
}
