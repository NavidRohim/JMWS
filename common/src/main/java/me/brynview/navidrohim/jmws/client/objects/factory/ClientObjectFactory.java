package me.brynview.navidrohim.jmws.client.objects.factory;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.objects.ClientObject;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;

public class ClientObjectFactory {
    public static ClientObject<ClientWaypointWrapper> fromWaypoint(Waypoint waypoint)
    {
        return new ClientObject<>(
                waypoint.getName(),
                waypoint.getCustomData(Constants.MODID),
                waypoint.getGuid(),
                ObjectType.WAYPOINT,
                new ClientWaypointWrapper(waypoint)
        );
    }
}
