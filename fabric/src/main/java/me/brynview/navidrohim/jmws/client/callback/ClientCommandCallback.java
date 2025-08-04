package me.brynview.navidrohim.jmws.client.callback;

/*
import com.mojang.brigadier.CommandDispatcher;

import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;



public interface ClientCommandCallback {


    private static boolean isInSingleplayer() {
        return IClientPluginJM.minecraftClientInstance.isInSingleplayer();
    }
    private static void sendUserSinglePlayerWarning() {
        IClientPluginJM.sendUserAlert(Text.translatable("warning.jmws.world_is_local_no_commands"), true, false, JMWSMessageType.WARNING);
    }

    static void Callback(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess _commandRegistryAccess) {

        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("jmws")
            // finished (theoretically)
            .then(ClientCommandManager.literal("sync").executes(context -> {
                if (!isInSingleplayer()) {
                    IClientPluginJM.updateWaypoints(true);
                } else {
                    sendUserSinglePlayerWarning();
                }
                return 1;

            }))

            // finished (theoretically)
            .then(ClientCommandManager.literal("getSyncInterval").executes(intervalContext -> {
                IClientPluginJM.sendUserAlert(Text.translatable("message.jmws.sync_frequency", IClientPluginJM.getTickCounterUpdateThreshold() / 20), true, false, JMWSMessageType.NEUTRAL);
                return 1;
            }))

            // finished transition
            .then(ClientCommandManager.literal("clearAll")
                    .then(ClientCommandManager.literal("groups").executes(groupClearAllCtx -> {

                        if (!isInSingleplayer()) {
                            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(JsonStaticHelper.makeDeleteGroupRequestJson(
                                    IClientPluginJM.minecraftClientInstance.player.getUuid(),
                                    "",
                                    "",
                                    false,
                                    false,
                                    true
                            ));
                            ClientPlayNetworking.send(deleteServerObjectPayload);
                            IClientPluginJM.updateWaypoints(false);
                            IClientPluginJM.removeAllGroups();
                        } else {
                            sendUserSinglePlayerWarning();
                        }

                        return 1;
                    }))
                    .then(ClientCommandManager.literal("waypoints").executes(waypointClearAllCtx -> {
                        if (!isInSingleplayer()) {
                            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(JsonStaticHelper.makeDeleteRequestJson("", false, true)); // * = all
                            ClientPlayNetworking.send(deleteServerObjectPayload);
                            IClientPluginJM.updateWaypoints(false);
                        } else {
                            sendUserSinglePlayerWarning();
                        }
                        return 1;
                    })))

            // finished (theoretically)
            .then(ClientCommandManager.literal("nextSync").executes(updateDisplayContext -> {
                if (!isInSingleplayer()) {
                    IClientPluginJM.sendUserAlert(Text.translatable("message.jmws.next_sync", (IClientPluginJM.getTickCounterUpdateThreshold() - IClientPluginJM.getCurrentUpdateTick()) / 20), true, false, JMWSMessageType.NEUTRAL);
                } else {
                    sendUserSinglePlayerWarning();
                }
                return 1;
            }))
        );
    }
}*/
