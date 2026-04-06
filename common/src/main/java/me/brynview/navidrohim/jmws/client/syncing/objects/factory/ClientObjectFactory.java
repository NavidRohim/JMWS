package me.brynview.navidrohim.jmws.client.syncing.objects.factory;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin.isJmwsWaypoint;


public class ClientObjectFactory {
    @NotNull
    public static ClientWaypointWrapper fromWaypoint(Waypoint waypoint)
    {

        try
        {
            @Nullable SyncInformation syncInfo = SyncInformation.syncInformationFromString(waypoint.getCustomData(Constants.MODID));

            if (syncInfo == null)
            {
                return new ClientWaypointWrapper(waypoint, waypoint.getModId());
            }

            ClientWaypointWrapper wp = ObjectIdentifierMap.getObjectFromMap(syncInfo.objectIdentifier, ClientWaypointWrapper.class);

            if (wp == null) {
                return new ClientWaypointWrapper(waypoint, waypoint.getModId());
            }
            return wp;

        } catch (NullPointerException e)
        {
            throw e;
        }
    }

    @NotNull
    public static ClientGroupWrapper fromGroup(@NotNull WaypointGroup group)
    {

        @Nullable SyncInformation syncInfo = SyncInformation.syncInformationFromString(group.getCustomData(Constants.MODID));

        if (syncInfo == null)
        {
            return new ClientGroupWrapper(group, group.getModId());
        }

        ClientGroupWrapper gp = ObjectIdentifierMap.getObjectFromMap(syncInfo.objectIdentifier, ClientGroupWrapper.class);

        if (gp == null) {
            return new ClientGroupWrapper(group, group.getModId());
        }
        return gp;
    }

}
