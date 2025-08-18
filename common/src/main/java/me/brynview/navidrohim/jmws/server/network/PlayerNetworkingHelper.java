package me.brynview.navidrohim.jmws.server.network;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.helper.CommandHelper;
import me.brynview.navidrohim.jmws.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.payloads.JMWSActionPayload;
import net.minecraft.server.level.ServerPlayer;

public class PlayerNetworkingHelper {
    public static void replyHandshake(JMWSHandshakePayload handshakePayload, ServerPlayer player) {
        Dispatcher.sendToClient(handshakePayload, player);
    }

    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, boolean isError) {
        JMWSActionPayload messagePayload = new JMWSActionPayload(CommandHelper.makeClientAlertRequestJson(messageKey, overlay, isError));
        Dispatcher.sendToClient(messagePayload, player);
    }
}
