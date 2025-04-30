package me.brynview.navidrohim.jm_server_test;

import me.brynview.navidrohim.jm_server_test.items.DebugItem;
import me.brynview.navidrohim.jm_server_test.client.plugin.WaypointPayload;
import me.brynview.navidrohim.jm_server_test.server.handler.PlayerConnectHandler;
import me.brynview.navidrohim.jm_server_test.server.handler.HandleWaypointCreationPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JMServerTest implements ModInitializer {

    public static final String MODID = "jm_server_test";
    public static final String VERSION = "0.0.2-indev";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    @Override
    public void onInitialize() {

        // Item
        DebugItem.initialize();

        // Listeners
        PayloadTypeRegistry.playC2S().register(WaypointPayload.ID, WaypointPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(WaypointPayload.ID, HandleWaypointCreationPacket::HandlePacket);
        ServerPlayConnectionEvents.JOIN.register(PlayerConnectHandler::Listener);
    }
}
