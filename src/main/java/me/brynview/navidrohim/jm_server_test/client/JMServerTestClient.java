package me.brynview.navidrohim.jm_server_test.client;

import me.brynview.navidrohim.jm_server_test.client.plugin.IClientPluginJMTest;
import me.brynview.navidrohim.jm_server_test.common.payloads.WaypointActionPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class JMServerTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient()
    {
        ClientPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, IClientPluginJMTest::HandlePacket);
    }
}
