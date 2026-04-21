package me.brynview.navidrohim.jmws.client.utils;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import org.joml.Vector3d;

import java.util.UUID;

import static me.brynview.navidrohim.jmws.common.utils.CommonUtils._getWaypointFromRaw;

public class LegacyUtils
{
    private static String getLegacyWaypointFilename(Waypoint waypoint, UUID uuID) {
        Vector3d waypointLocationVector = new Vector3d(waypoint.getBlockPos().getX(), waypoint.getBlockPos().getY(), waypoint.getBlockPos().getZ());
        return _getWaypointFromRaw(waypointLocationVector, waypoint.getName(), uuID);
    }

    private static String getLegacyGroupFilename(UUID playerUUID, String universalID) {
        return "./jmws/groups/" + universalID + "_" + playerUUID + "-group" + ".json";
    }

    public static void transitionObject(Waypoint waypoint, UUID playerOwner, ServerSyncRegistryEntry serverSyncRegistry)
    {
        ClientNetworkDispatcher.transitionOldObject(waypoint.getCustomData(Constants.MODID), getLegacyWaypointFilename(waypoint, playerOwner), serverSyncRegistry);
    }
    public static void transitionObject(WaypointGroup waypointGroup, UUID playerOwner, ServerSyncRegistryEntry serverSyncRegistry)
    {
        ClientNetworkDispatcher.transitionOldObject(waypointGroup.getCustomData(Constants.MODID), getLegacyGroupFilename(playerOwner, waypointGroup.getCustomData(Constants.MODID)), serverSyncRegistry);
    }
}
