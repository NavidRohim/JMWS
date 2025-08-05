package me.brynview.navidrohim.jmws.client.callback;


import com.mojang.brigadier.CommandDispatcher;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;


import me.brynview.navidrohim.jmws.helper.CommandHelper;
import me.brynview.navidrohim.jmws.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.platform.Services;
import me.brynview.navidrohim.jmws.plugin.JMWSPlugin;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;


public interface ClientCommandCallback {


    private static boolean isInSingleplayer() {
        return CommonClass.minecraftClientInstance.isSingleplayer();
    }
    private static void sendUserSinglePlayerWarning() {
        PlayerHelper.sendUserAlert(Component.translatable("warning.jmws.world_is_local_no_commands"), true, false, JMWSMessageType.WARNING);
    }

    static void Callback(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandBuildContext _commandbctx) {

        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("jmws")
            // finished (theoretically)
            .then(ClientCommandManager.literal("sync").executes(context -> {
                if (!isInSingleplayer()) {
                    JMWSPlugin.updateWaypoints(true);
                } else {
                    sendUserSinglePlayerWarning();
                }
                return 1;

            }))

            // finished (theoretically)
            .then(ClientCommandManager.literal("getSyncInterval").executes(intervalContext -> {
                PlayerHelper.sendUserAlert(Component.translatable("message.jmws.sync_frequency", Services.PLATFORM.getSyncInTicks() / 20), true, false, JMWSMessageType.NEUTRAL);
                return 1;
            }))

            // finished transition
            .then(ClientCommandManager.literal("clearAll")
                    .then(ClientCommandManager.literal("groups").executes(groupClearAllCtx -> {

                        if (!isInSingleplayer()) {
                            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(CommandHelper.makeDeleteGroupRequestJson(
                                    CommonClass.minecraftClientInstance.player.getUUID(),
                                    "",
                                    "",
                                    false,
                                    false,
                                    true
                            ));
                            Dispatcher.sendToServer(deleteServerObjectPayload);
                            JMWSPlugin.updateWaypoints(false);
                            JMWSPlugin.removeAllGroups();
                        } else {
                            sendUserSinglePlayerWarning();
                        }

                        return 1;
                    }))
                    .then(ClientCommandManager.literal("waypoints").executes(waypointClearAllCtx -> {
                        if (!isInSingleplayer()) {
                            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(CommandHelper.makeDeleteRequestJson("", false, true)); // * = all
                            Dispatcher.sendToServer(deleteServerObjectPayload);
                            JMWSPlugin.updateWaypoints(false);
                        } else {
                            sendUserSinglePlayerWarning();
                        }
                        return 1;
                    })))

            // finished (theoretically)
            .then(ClientCommandManager.literal("nextSync").executes(updateDisplayContext -> {
                if (!isInSingleplayer()) {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.next_sync", (Services.PLATFORM.getSyncInTicks() - Services.PLATFORM.timeUntilNextSyncInTicks()) / 20), true, false, JMWSMessageType.NEUTRAL);
                } else {
                    sendUserSinglePlayerWarning();
                }
                return 1;
            }))
        );
    }
}
