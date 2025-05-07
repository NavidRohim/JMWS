package me.brynview.navidrohim.jm_server.client;

import me.brynview.navidrohim.jm_server.client.plugin.IClientPluginJM;
import me.brynview.navidrohim.jm_server.common.payloads.RegisterUserPayload;
import me.brynview.navidrohim.jm_server.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jm_server.common.utils.JMServerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;

public class JMServerClient implements ClientModInitializer {

    public static JMServerConfig CONFIG;

    @Override
    public void onInitializeClient()
    {
        CONFIG = JMServerConfig.createAndLoad();
    }
}
