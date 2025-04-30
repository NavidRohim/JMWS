package me.brynview.navidrohim.jm_server_test.client.handler;

import journeymap.api.v2.common.waypoint.WaypointFactory;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.client.plugin.WaypointPayload;
import me.brynview.navidrohim.jm_server_test.common.SavedWaypoint;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;

public class HandleClientWaypointPacket {
    public static void HandlePacket(WaypointPayload waypointPayload, ClientPlayNetworking.Context context) {
        SavedWaypoint savedWaypoint = waypointPayload.getSavedWaypoint();
        WaypointFactory.createClientWaypoint(JMServerTest.MODID, BlockPos.ofFloored(savedWaypoint.getWaypointX(), savedWaypoint.getWaypointY(), savedWaypoint.getWaypointZ()), savedWaypoint.getDimensionString(), true);

    }
}
