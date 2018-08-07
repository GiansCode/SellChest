package com.thesquadmc.sellchest.listener;

import com.thesquadmc.sellchest.SellChest;
import com.thesquadmc.sellchest.message.Message;
import com.thesquadmc.sellchest.util.Util;
import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InteractListener implements Listener
{
    private final SellChest plugin;

    public InteractListener(SellChest plugin)
    {
        this.plugin = plugin;

        sellSignPattern = Util.colourList(plugin.configGet("sign.replace"));
    }

    private final List<String> sellSignPattern;

    private final Pattern usagePattern = Pattern.compile(".* ([0-9]+)/([0-9]+)");

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Player player = event.getPlayer();

            Block block = event.getClickedBlock();

            if (block.getState() instanceof Sign)
            {
                Sign sign = (Sign) block.getState();

                if (!isSellSign(sign))
                {
                    return;
                }

                Block attached = block.getRelative(((Attachable) sign.getData()).getAttachedFace());

                if (!isValidContainer(attached))
                {
                    return;
                }

                InventoryHolder holder = (InventoryHolder) attached.getState();

                if (!plugin.getWorldGuard().canUseChest(player, attached.getLocation()))
                {
                    Util.message(player, Message.NO_SELL_PERMISSION);
                    return;
                }

                plugin.getEconomy().sellChest(player, holder);
            }
            else if (block.getState() instanceof InventoryHolder)
            {
                if (!isValidContainer(block))
                {
                    return;
                }

                InventoryHolder holder = (InventoryHolder) block.getState();
                ItemStack stack = player.getItemInHand();

                if (plugin.isSellWand(stack))
                {
                    event.setCancelled(true);

                    if (!plugin.getWorldGuard().canUseChest(player, block.getLocation()))
                    {
                        Util.message(player, Message.NO_SELL_PERMISSION);
                        return;
                    }

                    if (plugin.getEconomy().sellChest(player, holder) > 0.0D)
                    {
                        int[] data = getAvailableAndMax(stack);

                        if (data == null)
                        {
                            return;
                        }

                        int available = data[0];

                        if (available == -1)
                        {
                            return;
                        }

                        available--;

                        if (available == 0)
                        {
                            player.getInventory().remove(stack);
                            Util.message(player, Message.SELL_WAND_BROKE);
                        }
                        else
                        {
                            int max = data[1];

                            ItemStack newStack = plugin.getNewWand(available, max);
                            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newStack);
                        }
                    }
                }
            }
        }
    }

    private boolean isValidContainer(Block block)
    {
        return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.HOPPER;
    }

    private int[] getAvailableAndMax(ItemStack stack)
    {
        for (String line : stack.getItemMeta().getLore())
        {
            line = ChatColor.stripColor(line);

            Matcher matcher = usagePattern.matcher(line);

            if (matcher.matches())
            {
                return new int[] {
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
                };
            }
        }

        // Shouldn't happen
        return null;
    }

    private boolean isSellSign(Sign sign)
    {
        for (int i = 0; i < sellSignPattern.size(); i++)
        {
            if (!sellSignPattern.get(i).equals(sign.getLine(i)))
            {
                return false;
            }
        }

        return true;
    }
}
