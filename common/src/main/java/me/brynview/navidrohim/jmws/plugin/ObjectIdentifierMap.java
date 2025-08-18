package me.brynview.navidrohim.jmws.plugin;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.helpers.CommonHelper;

import java.util.HashMap;

import static me.brynview.navidrohim.jmws.CommonClass.*;

public class ObjectIdentifierMap {

    private static final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();
    private static final HashMap<String, WaypointGroup> groupIdentifierMap = new HashMap<>();

    public static Waypoint getOldWaypoint(Waypoint newWaypoint) {
        String persistentWaypointID = newWaypoint.getCustomData();
        return waypointIdentifierMap.get(persistentWaypointID);
    }


    public static Waypoint getOldWaypoint(String waypointID)
    {
        return waypointIdentifierMap.get(waypointID);
    }


    public static WaypointGroup getOldGroup(WaypointGroup newWaypointGroup)
    {
        return groupIdentifierMap.get(newWaypointGroup.getCustomData());
    }


    public static WaypointGroup getOldGroup(String groupID)
    {
        return groupIdentifierMap.get(groupID);
    }

    public static String addWaypointToMap(Waypoint waypoint)
    {
        String waypointIdentifier = CommonHelper.makeWaypointHash(minecraftClientInstance.player.getUUID(), waypoint.getGuid(), waypoint.getName());
        waypointIdentifierMap.put(waypointIdentifier, waypoint);
        waypoint.setCustomData(waypointIdentifier);

        return waypointIdentifier;
    }

    public static String addGroupToMap(WaypointGroup waypointGroup)
    {
        String waypointIdentifier = CommonHelper.makeWaypointHash(minecraftClientInstance.player.getUUID(), waypointGroup.getGuid(), waypointGroup.getName());
        groupIdentifierMap.put(waypointIdentifier, waypointGroup);
        waypointGroup.setCustomData(waypointIdentifier);

        return waypointIdentifier;
    }

    public static void removeWaypointFromMap(Waypoint waypoint)
    {
        waypointIdentifierMap.remove(waypoint.getCustomData());
    }

    public static void removeGroupFromMap(WaypointGroup group)
    {
        groupIdentifierMap.remove(group.getCustomData());
    }
}
