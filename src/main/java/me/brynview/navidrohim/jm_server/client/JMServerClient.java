package me.brynview.navidrohim.jm_server.client;

import me.brynview.navidrohim.jm_server.client.plugin.IClientPluginJM;
import me.brynview.navidrohim.jm_server.common.payloads.WaypointActionPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class JMServerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient()
    {
        ClientPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, IClientPluginJM::HandlePacket);
    }
}
