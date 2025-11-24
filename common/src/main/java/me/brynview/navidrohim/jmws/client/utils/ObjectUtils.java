package me.brynview.navidrohim.jmws.client.utils;

import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;

import java.util.UUID;

public class ObjectUtils
{
    public static String getIdentifier(Waypoint waypoint)
    {
        return ServerObject.SyncingInformation.getSyncingInfo(waypoint.getCustomData()).objectIdentifier;
    }
    public static String getIdentifier(WaypointGroup waypointGroup)
    {
        return ServerObject.SyncingInformation.getSyncingInfo(waypointGroup.getCustomData()).objectIdentifier;
    }
    public static void transitionObject(String objectID, UUID playerOwner, ObjectType objectType)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeTransitionObjectRequest(objectID, playerOwner, objectType)));
    }
}
