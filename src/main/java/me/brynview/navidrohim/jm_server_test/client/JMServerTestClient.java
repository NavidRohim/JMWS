package me.brynview.navidrohim.jm_server_test.client;

import me.brynview.navidrohim.jm_server_test.client.plugin.IClientPluginJMTest;
import me.brynview.navidrohim.jm_server_test.server.payloads.WaypointSendPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class JMServerTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient()
    {
        PayloadTypeRegistry.playS2C().register(WaypointSendPayload.ID, WaypointSendPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(WaypointSendPayload.ID, IClientPluginJMTest::HandlePacket);
    }
}
