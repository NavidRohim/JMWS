package me.brynview.navidrohim.jmws.server.network;


import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PlayerNetworkingHelper {
    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, boolean isError, boolean silent) {
        if (!silent) // is this dumb
        {
            ServerNetworkDispatcher.sendStringToClient(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, isError ? MessageType.FAILURE : MessageType.NEUTRAL), player);
        }
    }

    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, boolean isError) {
        sendUserMessage(player, messageKey, overlay, isError, false);
    }

    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, MessageType messageType) {
        ServerNetworkDispatcher.sendStringToClient(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, messageType), player);
    }

    public static void sendUserMessage(UUID player, String messageKey, Boolean overlay, MessageType messageType)
    {
        ServerNetworkDispatcher.sendStringToClient(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, messageType), CommonClass.minecraftServerInstance.getPlayerList().getPlayer(player));
    }

    public static void sendHandshakeAndValidate(ServerPlayer joinedUser)
    {
        JMWSServerIO.validateUserObjects(joinedUser.getUUID());
        ServerNetworkDispatcher.sendHandshakeToClient(joinedUser);
    }
}
