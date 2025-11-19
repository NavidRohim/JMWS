package me.brynview.navidrohim.jmws.client.callback;


import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.brynview.navidrohim.jmws.client.commands.ClientCommands;

import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface ClientCommandCallback {

    static void Callback(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandBuildContext _commandbctx) {
        // There has to be a better way to do this??
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("jmws")
            .then(ClientCommandManager.literal("sync").executes(context -> ClientCommands.sync()))
            .then(ClientCommandManager.literal("getSyncInterval").executes(intervalContext -> ClientCommands.getSyncInterval()))
            .then(ClientCommandManager.literal("nextSync").executes(updateDisplayContext -> ClientCommands.nextSync()))
            .then(ClientCommandManager.literal("clearAll")
                    .then(ClientCommandManager.literal("groups").executes(groupClearAllCtx -> ClientCommands.clearAllGroups()))
                    .then(ClientCommandManager.literal("waypoints").executes(waypointClearAllCtx -> ClientCommands.clearAllWaypoints())))
        );

        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("share_accept")
                .then(ClientCommandManager.argument("sender", StringArgumentType.greedyString())
                        .suggests(ClientCommandCallback::suggestIncoming)
                            .executes(ClientCommandCallback::accept)));

        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("share_decline")
                .then(ClientCommandManager.argument("sender", StringArgumentType.greedyString())
                        .suggests(ClientCommandCallback::suggestIncoming)
                            .executes(ClientCommandCallback::decline)));
    }

    static int accept(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) {
        String sender = StringArgumentType.getString(fabricClientCommandSourceCommandContext, "sender");
        return ClientCommands.accept(sender);
    }

    static int decline(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) {
        String sender = StringArgumentType.getString(fabricClientCommandSourceCommandContext, "sender");
        return ClientCommands.decline(sender);
    }

    static CompletableFuture<Suggestions> suggestIncoming(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext, SuggestionsBuilder suggestionsBuilder)
    {
        List<String> names = new ArrayList<>();
        for (UUID user : IncomingShareRequests.getAll().keySet())
        {
            names.add(PlayerHelper.getUserFromUUID(user).getDisplayName().getString());
        }

        return SharedSuggestionProvider.suggest(names, suggestionsBuilder);
    }
}
