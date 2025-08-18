package me.brynview.navidrohim.jmws.client.callback;


import com.mojang.brigadier.CommandDispatcher;

import me.brynview.navidrohim.jmws.client.ClientCommands;


import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;


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
    }
}
