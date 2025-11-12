package me.brynview.navidrohim.jmws.client.utils;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.common.objects.SavedObject;

public class ObjectUtils
{
    public static String getIdentifier(Waypoint waypoint)
    {
        return SavedObject.SyncingInformation.getSyncingInfo(waypoint.getCustomData()).objectIdentifier;
    }
    public static String getIdentifier(WaypointGroup waypointGroup)
    {
        return SavedObject.SyncingInformation.getSyncingInfo(waypointGroup.getCustomData()).objectIdentifier;
    }
}
