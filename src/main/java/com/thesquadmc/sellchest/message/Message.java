package com.thesquadmc.sellchest.message;
import com.thesquadmc.sellchest.util.Util;
import com.thesquadmc.sellchest.SellChest;

import java.util.Arrays;

public enum Message
{
    NO_PERMISSION,
    NO_CONSOLE,

    GIVE_COMMAND_USAGE,
    GIVE_COMMAND_NOT_ONLINE,
    GIVE_COMMAND_INVALID_NUMBER,
    GIVE_COMMAND_TOO_SMALL,
    GIVE_COMMAND_GIVEN,
    GIVE_COMMAND_RECEIVED,

    SELL_WAND_BROKE,

    MONEY_EARNED,
    CREATED_SIGN,
    SIGN_ERROR,
    SELL_ERROR,
    NO_SELL_PERMISSION;

    private String msg;

    public String value()
    {
        return msg;
    }

    private void setValue(String msg)
    {
        this.msg = msg;
    }

    /**
     * Initialise the plugin messages
     * @param plugin The plugin instance
     */
    public static void init(SellChest plugin)
    {
        Arrays.stream(values()).forEach(message ->
        {
            String raw = plugin.configGet( "messages." + message.name().toLowerCase());

            if (raw == null)
            {
                Util.logErr("Unable to find message value for message '" + message.name() + "'");

                raw = message.name();
            }

            message.setValue(Util.colour(raw));
        });
    }
}
