package me.brynview.navidrohim.jm_server_test.client;

import journeymap.api.v2.common.event.ClientEventRegistry;
import me.brynview.navidrohim.jm_server_test.client.handler.HandleClientWaypointPacket;
import me.brynview.navidrohim.jm_server_test.client.plugin.WaypointPayload;
import me.brynview.navidrohim.jm_server_test.server.handler.HandleWaypointCreationPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class JMServerTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient()
    {
        ClientPlayNetworking.registerGlobalReceiver(WaypointPayload.ID, HandleClientWaypointPacket::HandlePacket);
    }
}
