package me.brynview.navidrohim.jmws.client.objects.factory;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.objects.ClientObject;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import org.jetbrains.annotations.Nullable;


public class ClientObjectFactory {
    @Nullable
    public static ClientObject<ClientWaypointWrapper> fromWaypoint(Waypoint waypoint)
    {

        try
        {
            return new ClientObject<>(
                    waypoint.getName(),
                    waypoint.getCustomData(Constants.MODID),
                    waypoint.getGuid(),
                    ObjectType.WAYPOINT,
                    new ClientWaypointWrapper(waypoint)
            );
        } catch (NullPointerException e)
        {
            return null;
        }
    }

    @Nullable
    public static ClientObject<ClientGroupWrapper> fromGroup(WaypointGroup group)
    {
        try {
            return new ClientObject<>(
                    group.getName(),
                    group.getCustomData(Constants.MODID),
                    group.getGuid(),
                    ObjectType.GROUP,
                    new ClientGroupWrapper(group)
            );
        } catch (NullPointerException e)
        {
            return null;
        }
    }

}
