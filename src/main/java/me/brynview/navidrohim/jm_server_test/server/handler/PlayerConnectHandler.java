package me.brynview.navidrohim.jm_server_test.server.handler;

import me.brynview.navidrohim.jm_server_test.JMServerTest;

import me.brynview.navidrohim.jm_server_test.server.payloads.WaypointSendPayload;
import me.brynview.navidrohim.jm_server_test.server.util.WaypointIOInterface;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minidev.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PlayerConnectHandler {
    public static void Listener(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
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

        } catch (IOException ioe) {
            JMServerTest.LOGGER.error(ioe.getMessage());
        }

    }
}