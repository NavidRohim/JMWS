package me.brynview.navidrohim.jmws.client.network;

import commonnetwork.api.Network;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.objects.ClientObject;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;

import java.util.UUID;

public class ClientNetworkDispatcher {
    public static void sendString(String data)
    {
        // ignoreCheck is true because forge broke packet validation I think.
        Network.getNetworkHandler().sendToServer(new JMWSActionPayload(data), true);
    }

    private static void sendPacket(JMWSActionPayload payload)
    {
        Network.getNetworkHandler().sendToServer(payload, true);
    }

    public static void deleteWaypoint(String waypointIdentifier, boolean silent, boolean all)
    {
        sendString(CommandFactory.deleteWaypoint(waypointIdentifier, silent, all));
    }

    public static void deleteGroup(String groupUniversalIdentifier, String groupGUID, boolean silent, boolean removeAllWaypointsInGroup, boolean removeGroupItself, boolean isGlobal, boolean deleteAllGroups) {
        sendString(CommandFactory.deleteGroup(
                groupUniversalIdentifier,
                groupGUID,
                silent,
                removeAllWaypointsInGroup,
                removeGroupItself,
                isGlobal,
                deleteAllGroups)
        );
    }

    public static void sync(boolean sendAlert, boolean isForDeathSync) {
        sendString(CommandFactory.makeWaypointSyncRequestJson(sendAlert, isForDeathSync));
    }

    public static void makeWaypoint(Waypoint waypoint, boolean silent) {
        sendString(CommandFactory.makeCreationRequestJson(waypoint, silent));
    }

    public static void makeGroup(WaypointGroup waypointGroup, boolean silent) {
        sendString(CommandFactory.makeGroupCreationRequestJson(waypointGroup, silent));
    }

    public static void declineShare(UUID originalSender)
    {
        sendString(CommandFactory.makeBaseJsonRequest(CommandFactory.Commands.REJECT_SHARE, originalSender, PlayerUtils.ourUUID()));
    }

    public static void declineShare(UUID originalSender, String messageKey)
    {
        sendString(CommandFactory.makeObjectShareRequestDeclineWithMessage(originalSender, messageKey));
    }

    public static void acceptShare(ShareRequest shareRequest)
    {
        sendString(CommandFactory.makeObjectShareRequestAccept(shareRequest));
    }

    public static void updateWaypoint(ClientObject<ClientWaypointWrapper> waypoint)
    {
        sendString(CommandFactory.makeUpdateWaypointRequest(waypoint));
    }

    public static void updateGroup(ClientObject<ClientGroupWrapper> group)
    {
        sendString(CommandFactory.makeUpdateGroupRequest(group));
    }

    public static void transitionToNewCustomData(String objectIdentifier, UUID owner, boolean isGlobal, ObjectType transitionType)
    {
        sendString(CommandFactory.makeTransitionObjectRequestForLegacyCustomData(objectIdentifier, owner, isGlobal, transitionType));
    }

    public static void transitionOldObject(String objectIdentifier, String filename, ObjectType transitionType)
    {
        sendString(CommandFactory.makeTransitionObjectRequest(filename, objectIdentifier, transitionType));
    }

    public static String makeShareRequestForServer(UUID from, UUID to, String objectIdentifier, ObjectType objectType)
    {
        return CommandFactory.makeBaseJsonRequest(CommandFactory.Commands.SHARE_FROM_CLIENT, from, to, objectIdentifier, objectType);
    }

    public static void shareWaypointWith(UUID from, UUID to, ClientObject<ClientWaypointWrapper> waypoint)
    {
        sendString(makeShareRequestForServer(from, to, waypoint.getObjectWrapper().getIdentifier(), ObjectType.WAYPOINT));
    }

    public static void shareGroupWith(UUID from, UUID to, ClientObject<JMObjectWrapper> group)
    {
        sendString(makeShareRequestForServer(from, to, group.getObjectWrapper().getIdentifier(), ObjectType.GROUP));
    }

    public static void removeShareWith(UUID from, UUID subject, ClientObject<JMObjectWrapper> waypoint)
    {
        sendString(CommandFactory.makeUnshareRequestForUserOnServer(from, subject, waypoint.getObjectWrapper().getIdentifier(), waypoint.getObjectType()));
    }

    public static void removeShareFromAll(UUID from, ClientObject<JMObjectWrapper> shareableObject)
    {
        sendString(CommandFactory.makeUnshareRequestForAllOnServer(from, shareableObject.getObjectWrapper().getIdentifier(), shareableObject.getObjectType()));
    }

    public static void makeGlobal(UUID from, ClientObject<JMObjectWrapper> globalObject, boolean global)
    {
        sendString(CommandFactory.makeGlobalRequestForServer(from, globalObject.getObjectWrapper().getIdentifier(), globalObject.getObjectType(), global));
    }

}
