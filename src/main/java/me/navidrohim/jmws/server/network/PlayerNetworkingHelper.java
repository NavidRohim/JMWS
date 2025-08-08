package me.navidrohim.jmws.server.network;


import me.navidrohim.jmws.helper.CommandHelper;
import me.navidrohim.jmws.payloads.*;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerNetworkingHelper {
    public static void replyHandshake(JMWSHandshakeReplyMessage handshakePayload, EntityPlayerMP player) {
        JMWSNetworkWrapper.INSTANCE.sendTo(handshakePayload, player);
    }

    public static void      sendUserMessage(EntityPlayerMP player, String messageKey, Boolean overlay, boolean isError) {
        JMWSActionMessage messagePayload = new JMWSActionMessage(CommandHelper.makeClientAlertRequestJson(messageKey, overlay, isError));
        JMWSNetworkWrapper.INSTANCE.sendTo(messagePayload, player);
    }
}
