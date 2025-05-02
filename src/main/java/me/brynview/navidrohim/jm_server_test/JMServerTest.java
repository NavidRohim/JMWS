package me.brynview.navidrohim.jm_server_test;

import me.brynview.navidrohim.jm_server_test.items.DebugItem;
import me.brynview.navidrohim.jm_server_test.client.payloads.WaypointPayloadOutbound;
import me.brynview.navidrohim.jm_server_test.server.handler.PlayerConnectHandler;
import me.brynview.navidrohim.jm_server_test.server.handler.HandleWaypointCreationPacket;
import me.brynview.navidrohim.jm_server_test.server.payloads.WaypointSendPayload;
import me.brynview.navidrohim.jm_server_test.server.util.WaypointIOInterface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JMServerTest implements ModInitializer {

    public static final String MODID = "jm_server_test";
    public static final String VERSION = "0.0.3";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    @Override
    public void onInitialize() {

        // Item
        File mainDir = new File("./jmserver");
        mainDir.mkdir();

        DebugItem.initialize();

        // Listeners
        ServerPlayConnectionEvents.JOIN.register(this::JoinListener);
        PayloadTypeRegistry.playC2S().register(WaypointPayloadOutbound.ID, WaypointPayloadOutbound.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(WaypointPayloadOutbound.ID, HandleWaypointCreationPacket::HandlePacket);

    }
    public void JoinListener(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        try {
            List<String> playerWaypoints = WaypointIOInterface.getPlayerWaypointNames(serverPlayNetworkHandler.player.getUuid());
            JSONObject jsonWaypointPayloadArray = new JSONObject();


            for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                String waypointFilename = playerWaypoints.get(i);
                String jsonWaypointFileString = Files.readString(Paths.get(waypointFilename));
                jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);
            }



            WaypointSendPayload waypointPayloadOutbound = new WaypointSendPayload(jsonWaypointPayloadArray.toJSONString());
            ServerPlayNetworking.send(serverPlayNetworkHandler.player, waypointPayloadOutbound);
            Packet<ClientCommonPacketListener> packet = ServerPlayNetworking.createS2CPacket(waypointPayloadOutbound);

        } catch (IOException ioe) {
            JMServerTest.LOGGER.error(ioe.getMessage());
        }

    }
}
