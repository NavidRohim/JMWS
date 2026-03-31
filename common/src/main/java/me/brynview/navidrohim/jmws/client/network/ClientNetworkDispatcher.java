package me.brynview.navidrohim.jmws.client.network;

import commonnetwork.api.Network;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;

public class ClientNetworkDispatcher {
    public static void sendString(String data)
    {
        // ignoreCheck is true because forge broke packet validation I think.
        Network.getNetworkHandler().sendToServer(new JMWSActionPayload(data), true);
    }

    public static void sendPacket(JMWSActionPayload payload)
    {
        Network.getNetworkHandler().sendToServer(payload, true);
    }
}
