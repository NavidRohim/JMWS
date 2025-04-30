package me.brynview.navidrohim.jm_server_test.util;

import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jm_server_test.client.plugin.SavedWaypoint;

import java.io.File;
import java.util.UUID;

public class WaypointIOInterface {
    public static String getWaypointFilename(SavedWaypoint savedWaypoint) {
        return "./jmserver/" +
                savedWaypoint.getRawJsonData().get("x") +
                "_" +
                savedWaypoint.getRawJsonData().get("y") +
                "_" +
                savedWaypoint.getRawJsonData().get("z") +
                "_" +
                savedWaypoint.getWaypointName() +
                "_" +
                savedWaypoint.getPlayerUUID() +
                ".json";
    }

    public static String getWaypointFilename(WaypointEvent waypointEvent, UUID uuID) {
        Waypoint wp = waypointEvent.getWaypoint();
        return "./jmserver/" +
                wp.getX() +
                "_" +
                wp.getY() +
                "_" +
                wp.getZ() +
                "_" +
                wp.getName() +
                "_" +
                uuID.toString() +
                ".json";
    }

    public static boolean deleteWaypoint(SavedWaypoint savedWaypoint) {
        File waypointFileObj = new File(WaypointIOInterface.getWaypointFilename(savedWaypoint));
        return waypointFileObj.delete();
    }

    public static boolean deleteWaypoint(String filename) {
        File waypointFileObj = new File(filename);
        return waypointFileObj.delete();
    }
}
