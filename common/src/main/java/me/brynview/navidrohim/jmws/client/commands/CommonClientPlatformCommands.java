package me.brynview.navidrohim.jmws.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

public class CommonClientPlatformCommands {
    public static int decline(CommandContext context)
    {
        String sender = StringArgumentType.getString(context, "sender");
        return ClientCommands.decline(sender);
    }

    public static int accept(CommandContext context) {
        String sender = StringArgumentType.getString(context, "sender");
        return ClientCommands.accept(sender);
    }
}
