package me.brynview.navidrohim.jmws.server.network;

import commonnetwork.api.Network;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import net.minecraft.server.level.ServerPlayer;

public class ServerNetworkDispatcher {
    public static void sendPacketToClient(JMWSActionPayload payload, ServerPlayer player)
    {
        Network.getNetworkHandler().sendToClient(payload, player, true);
    }

    public static void sendStringToClient(String data, ServerPlayer player)
    {
        sendPacketToClient(new JMWSActionPayload(data), player);
    }

    public static void sendHandshakeToClient(ServerPlayer player)
    {
        Network.getNetworkHandler().sendToClient(new JMWSHandshakePayload(), player, true);
    }
}
