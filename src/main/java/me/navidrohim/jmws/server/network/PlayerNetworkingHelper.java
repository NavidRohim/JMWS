package me.navidrohim.jmws.server.network;


import me.navidrohim.jmws.helper.CommandHelper;
import me.navidrohim.jmws.payloads.JMWSActionPayload;
import me.navidrohim.jmws.payloads.JMWSHandshakePayload;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerNetworkingHelper {
    public static void replyHandshake(JMWSHandshakePayload handshakePayload, EntityPlayerMP player) {
        //Dispatcher.sendToClient(handshakePayload, player);
    }

    public static void sendUserMessage(EntityPlayerMP player, String messageKey, Boolean overlay, boolean isError) {
        JMWSActionPayload messagePayload = new JMWSActionPayload(CommandHelper.makeClientAlertRequestJson(messageKey, overlay, isError));
        //Dispatcher.sendToClient(messagePayload, player);
    }
}
