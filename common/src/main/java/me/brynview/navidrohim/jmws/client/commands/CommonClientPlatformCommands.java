package me.brynview.navidrohim.jmws.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.concurrent.CompletableFuture;

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

    public static void registerClientDispatcher(CommandDispatcher dispatcher)
    {
        dispatcher.register(
                Commands.literal("jmws")
                        .then(Commands.literal("sync").executes(commandContext -> ClientCommands.sync()))
                        .then(Commands.literal("getSyncInterval").executes(commandContext -> ClientCommands.getSyncInterval()))
                        .then(Commands.literal("nextSync").executes(updateDisplayContext -> ClientCommands.nextSync()))
                        .then(Commands.literal("clearAll")
                                .then(Commands.literal("groups").executes(groupClearAllCtx -> ClientCommands.clearAllGroups()))
                                .then(Commands.literal("waypoints").executes(waypointClearAllCtx -> ClientCommands.clearAllWaypoints())))
        );
        dispatcher.register(Commands.literal("share_accept")
                .then(Commands.argument("sender", StringArgumentType.greedyString())
                        .suggests(CommonClientPlatformCommands::getShareRequestSuggestions)
                        .executes(CommonClientPlatformCommands::accept)));

        dispatcher.register(Commands.literal("share_decline")

                .then(Commands.argument("sender", StringArgumentType.greedyString())
                        .suggests(CommonClientPlatformCommands::getShareRequestSuggestions)
                        .executes(CommonClientPlatformCommands::decline)));
    }


    private static CompletableFuture<Suggestions> getShareRequestSuggestions(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder)
    {
        for (String suggestion : ShareSuggestions.getIncomingShareRequestNames())
        {
            suggestionsBuilder.suggest(suggestion);
        }
        return suggestionsBuilder.buildFuture();
    }
}
