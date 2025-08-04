package me.brynview.navidrohim.jmws.io;

import journeymap.api.v2.common.waypoint.Waypoint;
import org.joml.Vector3d;

import java.io.File;
import java.util.UUID;

public class CommonIO {
    // This is kinda just a "put whatever here that is used everywhere" class

    public static String getWaypointFilename(Waypoint waypoint, UUID uuID) {
        Vector3d waypointLocationVector = new Vector3d(waypoint.getX(), waypoint.getY(), waypoint.getZ());
        return _getWaypointFromRaw(waypointLocationVector, waypoint.getName(), uuID);
    }

    public static String _getWaypointFromRaw(Vector3d coordVector, String waypointName, UUID playerUUID) {
        return "./jmws/" +
                coordVector.x +
                "_" +
                coordVector.y +
                "_" +
                coordVector.z +
                "_" +
                waypointName +
                "_" +
                playerUUID +
                ".json";
    }

    public static boolean deleteFile(String filename) {
        File waypointFileObj = new File(filename);
        return waypointFileObj.delete();
    }
}
