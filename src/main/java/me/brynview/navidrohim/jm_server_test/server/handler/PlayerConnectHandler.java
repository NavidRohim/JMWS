package me.brynview.navidrohim.jm_server_test.server.handler;

import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.server.util.WaypointIOInterface;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.List;

public class PlayerConnectHandler {
    public static void Listener(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        List<String> playerWaypoints = WaypointIOInterface.getPlayerWaypointNames(serverPlayNetworkHandler.player.getUuid());
        JMServerTest.LOGGER.info(playerWaypoints);
    }
}
