package me.brynview.navidrohim.jm_server_test.server.handler;


import me.brynview.navidrohim.jm_server_test.client.payloads.WaypointPayloadOutbound;
import me.brynview.navidrohim.jm_server_test.common.SavedWaypoint;
import me.brynview.navidrohim.jm_server_test.server.util.WaypointIOInterface;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;


public class HandleWaypointCreationPacket {

    public static void HandlePacket(WaypointPayloadOutbound waypointPayload, Context context) {
        SavedWaypoint savedWaypoint = waypointPayload.getSavedWaypoint();
        WaypointIOInterface.createWaypoint(savedWaypoint);

    }
}
