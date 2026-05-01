package me.brynview.navidrohim.jmws.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.concurrent.CompletableFuture;

public class CommonClientPlatformCommands {

    @SuppressWarnings("unchecked")
    public static void registerClientDispatcher(CommandDispatcher dispatcher)
    {
        dispatcher.register(
                Commands.literal(Constants.MODID)
                        .then(Commands.literal("sync").executes(commandContext -> ClientCommands.sync()))
                        .then(Commands.literal("getSyncInterval").executes(commandContext -> ClientCommands.getSyncInterval()))
                        .then(Commands.literal("nextSync").executes(updateDisplayContext -> ClientCommands.nextSync()))
                        .then(Commands.literal("clearAll")
                                .then(Commands.literal("groups").executes(groupClearAllCtx -> ClientCommands.clearAllGroups()))
                                .then(Commands.literal("waypoints").executes(waypointClearAllCtx -> ClientCommands.clearAllWaypoints())))
        );
    }
}
