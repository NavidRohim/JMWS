package me.brynview.navidrohim.jmws.server.utils;

import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.objects.SavedObject;
import me.brynview.navidrohim.jmws.common.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WaypointUtils {

    public static void removeWaypointFromUsers(String waypointIdentifier, @Nullable UUID playerUUID)
    {
        SavedWaypoint waypoint = (SavedWaypoint) JMWSServerIO.Utils.getObject(waypointIdentifier, playerUUID, FetchType.WAYPOINT);
        SavedObject.SyncingInformation info = SavedObject.SyncingInformation.getSyncingInfo(waypoint);

        for (String userUUID : info.sharedTo)
        {
            waypoint.sharing.removeFromShared(waypointIdentifier);
        }
    }
}
