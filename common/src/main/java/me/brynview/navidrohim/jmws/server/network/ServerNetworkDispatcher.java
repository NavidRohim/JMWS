package me.brynview.navidrohim.jmws.server.network;

import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.platform.Services;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;

import java.util.UUID;

public class ServerNetworkDispatcher {
    public static void sendPacketToClient(JMWSActionPayload payload, UUID playerUuid)
    {
        Services.PLATFORM.sendActionPayloadToClient(payload, playerUuid);
    }

    public static void sendStringToClient(String data, UUID playerUuid)
    {
        sendPacketToClient(new JMWSActionPayload(data), playerUuid);
    }

    public static void sendHandshakeToClient(UUID playerUuid)
    {
        Services.PLATFORM.sendHandshakePayloadToClient(new JMWSHandshakePayload(), playerUuid);
    }

    public static void requestClientSync(UUID playerUuid)
    {
        sendStringToClient(CommandFactory.makeBaseJsonRequest(CommandFactory.Commands.REQUEST_CLIENT_SYNC), playerUuid);
    }
}
