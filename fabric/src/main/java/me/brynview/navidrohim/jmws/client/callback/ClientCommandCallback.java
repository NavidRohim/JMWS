package me.brynview.navidrohim.jmws.client.callback;


import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ClientCommands;


import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public interface ClientCommandCallback {

    static void Callback(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandBuildContext _commandbctx) {
        // There has to be a better way to do this??
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("jmws")
            .then(ClientCommandManager.literal("sync").executes(context -> ClientCommands.sync()))
            .then(ClientCommandManager.literal("getSyncInterval").executes(intervalContext -> ClientCommands.getSyncInterval()))
            .then(ClientCommandManager.literal("nextSync").executes(updateDisplayContext -> ClientCommands.nextSync()))
            .then(ClientCommandManager.literal("accept").executes(acceptingContext -> ClientCommands.accept()))
            .then(ClientCommandManager.literal("decline").executes(acceptingContext -> ClientCommands.decline()))
            .then(ClientCommandManager.literal("clearAll")
                    .then(ClientCommandManager.literal("groups").executes(groupClearAllCtx -> ClientCommands.clearAllGroups()))
                    .then(ClientCommandManager.literal("waypoints").executes(waypointClearAllCtx -> ClientCommands.clearAllWaypoints())))
        );
    }
}
