package me.brynview.navidrohim.jmws.client.syncing.objects.factory;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import org.jetbrains.annotations.Nullable;

import static me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin.isJmwsWaypoint;


public class ClientObjectFactory {
    @Nullable
    public static ClientWaypointWrapper fromWaypoint(Waypoint waypoint)
    {

        try
        {
            return new ClientWaypointWrapper(waypoint, waypoint.getModId());
        } catch (NullPointerException e)
        {
            return null;
        }
    }

    @Nullable
    public static ClientGroupWrapper fromGroup(WaypointGroup group)
    {
        try {

            return new ClientGroupWrapper(group, group.getModId());
        } catch (NullPointerException e)
        {
            return null;
        }
    }

}
