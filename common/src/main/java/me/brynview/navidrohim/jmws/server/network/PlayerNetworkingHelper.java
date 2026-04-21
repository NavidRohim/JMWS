package me.brynview.navidrohim.jmws.server.network;

import commonnetwork.api.Dispatcher;
import commonnetwork.api.Network;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PlayerNetworkingHelper {
    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, boolean isError, boolean silent, String... translationArgs) {
        if (!silent) // is this dumb
        {
            JMWSActionPayload messagePayload = new JMWSActionPayload(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, isError ? MessageType.FAILURE : MessageType.NEUTRAL, translationArgs));
            Dispatcher.sendToClient(messagePayload, player);
        }
    }

    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, boolean isError) {
        sendUserMessage(player, messageKey, overlay, isError, false);
    }

    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, MessageType messageType) {
        JMWSActionPayload messagePayload = new JMWSActionPayload(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, messageType));
        Dispatcher.sendToClient(messagePayload, player);
    }

    public static void sendUserMessage(UUID player, String messageKey, Boolean overlay, MessageType messageType) {
        JMWSActionPayload messagePayload = new JMWSActionPayload(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, messageType));
        Dispatcher.sendToClient(messagePayload, JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(player));
    }

    public static void sendHandshakeAndValidate(ServerPlayer joinedUser)
    {
        //JMWSServerIO.validateUserObjects(joinedUser.getUUID());
        Network.getNetworkHandler().sendToClient(new JMWSHandshakePayload(), joinedUser, true);
        //Dispatcher.sendToClient(new JMWSHandshakePayload(), joinedUser, false);
    }
}
