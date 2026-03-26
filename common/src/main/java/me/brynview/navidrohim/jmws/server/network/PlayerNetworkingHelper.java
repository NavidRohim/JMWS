package me.brynview.navidrohim.jmws.server.network;

import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PlayerNetworkingHelper {
    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, boolean isError, boolean silent) {
        if (!silent) // is this dumb
        {
            JMWSActionPayload messagePayload = new JMWSActionPayload(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, isError ? MessageType.FAILURE : MessageType.NEUTRAL));
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
        Dispatcher.sendToClient(messagePayload, CommonClass.minecraftServerInstance.getPlayerList().getPlayer(player));
    }

    public static void sendHandshakeAndValidate(ServerPlayer joinedUser)
    {
        JMWSServerIO.validateUserObjects(joinedUser.getUUID());
        Dispatcher.sendToClient(new JMWSHandshakePayload(), joinedUser);
    }
}
