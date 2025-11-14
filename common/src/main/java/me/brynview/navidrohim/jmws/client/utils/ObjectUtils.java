package me.brynview.navidrohim.jmws.client.utils;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;

public class ObjectUtils
{
    public static String getIdentifier(Waypoint waypoint)
    {
        return ServerObject.SyncingInformation.getSyncingInfo(waypoint.getCustomData()).objectIdentifier;
    }
    public static String getIdentifier(WaypointGroup waypointGroup)
    {
        return ServerObject.SyncingInformation.getSyncingInfo(waypointGroup.getCustomData()).objectIdentifier;
    }
}
