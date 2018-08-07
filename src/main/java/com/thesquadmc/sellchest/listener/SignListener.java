package com.thesquadmc.sellchest.listener;

import com.thesquadmc.sellchest.SellChest;
import com.thesquadmc.sellchest.message.Message;
import com.thesquadmc.sellchest.util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Attachable;

import java.util.List;

public class SignListener implements Listener
{
    public SignListener(SellChest plugin)
    {
        signPattern = Util.colourList(plugin.configGet("sign.pattern"));
        replacementLines = Util.colourList(plugin.configGet("sign.replace"));
    }

    private final List<String> signPattern;
    private final List<String> replacementLines;

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        Player player = event.getPlayer();

        Block block = event.getBlock();
        Sign sign = (Sign) event.getBlock().getState();

        if (matchesPattern(event.getLines()))
        {
            Block attached = block.getRelative(((Attachable) sign.getData()).getAttachedFace());

            if (attached.getType() != Material.CHEST && attached.getType() != Material.TRAPPED_CHEST && attached.getType() != Material.HOPPER)
            {
                Util.message(player, Message.SIGN_ERROR);
                return;
            }

            replaceLines(event);
            Util.message(player, Message.CREATED_SIGN);
        }
    }

    private void replaceLines(SignChangeEvent event)
    {
        for (int i = 0; i < replacementLines.size(); i++)
        {
            String line = replacementLines.get(i);

            event.setLine(i, Util.colour(line));
        }
    }

    private boolean matchesPattern(String[] lines)
    {
        for (int i = 0; i < signPattern.size(); i++)
        {
            if (!signPattern.get(0).equalsIgnoreCase(lines[0]))
            {
                return false;
            }
        }

        return true;
    }
}