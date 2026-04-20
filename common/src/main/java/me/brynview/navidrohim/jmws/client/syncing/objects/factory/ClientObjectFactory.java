package me.brynview.navidrohim.jmws.client.syncing.objects.factory;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils;
import me.brynview.navidrohim.jmws.client.syncing.SyncRegistry;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientObjectFactory {
    @NotNull
    public static ClientWaypointWrapper fromWaypoint(Waypoint waypoint)
    {

        @Nullable ClientSyncInformation syncInfo = ClientSyncUtils.syncInformationFromString(waypoint.getCustomData(Constants.MODID), SyncRegistry.WAYPOINT);

        if (syncInfo == null)
        {
            return new ClientWaypointWrapper(waypoint, waypoint.getModId());
        }

        ClientWaypointWrapper wp = ObjectIdentifierMap.getObjectFromMap(syncInfo.objectIdentifier, ClientWaypointWrapper.class);

        if (wp == null) {
            return new ClientWaypointWrapper(waypoint, waypoint.getModId());
        }
        return wp;
    }

    @NotNull
    public static ClientGroupWrapper fromGroup(@NotNull WaypointGroup group)
    {

        @Nullable ClientSyncInformation syncInfo = ClientSyncUtils.syncInformationFromString(group.getCustomData(Constants.MODID), SyncRegistry.GROUP);

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

    @Nullable
    public static ClientObjectWrapper<?> fromType(SyncRegistry type, String data)
    {
        return type.getDecoder().decodeStringToWrapper(data);
    }
}
