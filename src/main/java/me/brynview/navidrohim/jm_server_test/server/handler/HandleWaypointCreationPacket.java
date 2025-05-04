package me.brynview.navidrohim.jm_server_test.server.handler;


import me.brynview.navidrohim.jm_server_test.common.payloads.RegisterUserPayload;
import me.brynview.navidrohim.jm_server_test.common.SavedWaypoint;
import me.brynview.navidrohim.jm_server_test.common.utils.WaypointIOInterface;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;


public class HandleWaypointCreationPacket {

    public static void HandlePacket(RegisterUserPayload waypointPayload, Context context) {
        SavedWaypoint savedWaypoint = waypointPayload.getSavedWaypoint();
        WaypointIOInterface.createWaypoint(savedWaypoint);

    }
}
