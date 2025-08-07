package me.navidrohim.jmws.plugin;


import journeymap.client.api.display.Waypoint;
import me.navidrohim.jmws.helper.CommonHelper;

import java.util.HashMap;

import static me.navidrohim.jmws.CommonClass.minecraftClientInstance;

public class ObjectIdentifierMap {

    private static final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();

    public static Waypoint getOldWaypoint(Waypoint newWaypoint) {
        String persistentWaypointID = newWaypoint.getCustomData();
        return waypointIdentifierMap.get(persistentWaypointID);
    }


    public static Waypoint getOldWaypoint(String waypointID)
    {
        return waypointIdentifierMap.get(waypointID);
    }

    public static String addWaypointToMap(Waypoint waypoint)
    {
        String waypointIdentifier = CommonHelper.makeWaypointHash(minecraftClientInstance.player.getUniqueID(), waypoint.getGuid(), waypoint.getName());
        waypointIdentifierMap.put(waypointIdentifier, waypoint);
        waypoint.setCustomData(waypointIdentifier);

        return waypointIdentifier;
    }

    public static void removeWaypointFromMap(Waypoint waypoint)
    {
        waypointIdentifierMap.remove(waypoint.getCustomData());
    }

}
