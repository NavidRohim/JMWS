package me.brynview.navidrohim.jmws.client.utils;

import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import org.joml.Vector3d;

import java.util.UUID;

import static me.brynview.navidrohim.jmws.common.helper.CommonHelper._getWaypointFromRaw;

public class LegacyUtils
{
    private static String getLegacyWaypointFilename(Waypoint waypoint, UUID uuID) {
        Vector3d waypointLocationVector = new Vector3d(waypoint.getBlockPos().getX(), waypoint.getBlockPos().getY(), waypoint.getBlockPos().getZ());
        return _getWaypointFromRaw(waypointLocationVector, waypoint.getName(), uuID);
    }

    private static String getLegacyGroupFilename(UUID playerUUID, String universalID) {
        return "./jmws/groups/" + universalID + "_" + playerUUID + "-group" + ".json";
    }

    public static void transitionObject(Waypoint waypoint, UUID playerOwner, ObjectType objectType)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeTransitionObjectRequest(waypoint.getCustomData(Constants.MODID), getLegacyWaypointFilename(waypoint, playerOwner), objectType)));
    }
    public static void transitionObject(WaypointGroup waypointGroup, UUID playerOwner, ObjectType objectType)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeTransitionObjectRequest(waypointGroup.getCustomData(Constants.MODID), getLegacyGroupFilename(playerOwner, waypointGroup.getCustomData(Constants.MODID)), objectType)));
    }
}
