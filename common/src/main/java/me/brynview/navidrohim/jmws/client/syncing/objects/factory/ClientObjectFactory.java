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
    public static ClientObject<ClientWaypointWrapper> fromWaypoint(Waypoint waypoint)
    {

        try
        {
            ClientObject<ClientWaypointWrapper> obj = new ClientObject<>(
                    waypoint.getName(),
                    waypoint.getCustomData(Constants.MODID),
                    waypoint.getGuid(),
                    ObjectType.WAYPOINT
            );
            obj.setWrapper(new ClientWaypointWrapper(waypoint, obj, Constants.MODID));

            return obj;

        } catch (NullPointerException e)
        {
            return null;
        }
    }

    @Nullable
    public static ClientObject<ClientGroupWrapper> fromGroup(WaypointGroup group)
    {
        try {
            ClientObject<ClientGroupWrapper> obj = new ClientObject<>(
                    group.getName(),
                    group.getCustomData(Constants.MODID),
                    group.getGuid(),
                    ObjectType.GROUP
            );
            obj.setWrapper(new ClientGroupWrapper(group, obj, Constants.MODID));

            return obj;
        } catch (NullPointerException e)
        {
            return null;
        }
    }

}
