package me.brynview.navidrohim.jmws.client.callbacks;

import com.mojang.brigadier.CommandDispatcher;

import me.brynview.navidrohim.jmws.JMWS;
import me.brynview.navidrohim.jmws.client.plugin.IClientPluginJM;
import me.brynview.navidrohim.jmws.common.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public interface ClientCommandCallback {

    static void Callback(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {

        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("jmws")
            // finished (theoretically)
            .then(ClientCommandManager.literal("sync").executes(context -> {
                IClientPluginJM.updateWaypoints(true);
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
                        JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(JsonStaticHelper.makeDeleteGroupRequestJson("", false, true)); // * = all
                        ClientPlayNetworking.send(deleteServerObjectPayload);
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("waypoints").executes(waypointClearAllCtx -> {
                        JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(JsonStaticHelper.makeDeleteRequestJson("", false, true)); // * = all
                        ClientPlayNetworking.send(deleteServerObjectPayload);
                        return 1;
                    })))

            // finished (theoretically)
            .then(ClientCommandManager.literal("nextSync").executes(updateDisplayContext -> {
                IClientPluginJM.sendUserAlert(Text.translatable("message.jmws.next_sync", (IClientPluginJM.getTickCounterUpdateThreshold() - IClientPluginJM.getCurrentUpdateTick()) / 20), true, false, JMWSMessageType.NEUTRAL);
                return 1;
            }))
        );
    }
}
