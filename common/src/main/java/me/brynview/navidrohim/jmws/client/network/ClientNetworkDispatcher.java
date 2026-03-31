package me.brynview.navidrohim.jmws.client.network;

import commonnetwork.api.Network;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;

import java.util.UUID;

public class ClientNetworkDispatcher {
    private static void sendString(String data)
    {
        // ignoreCheck is true because forge broke packet validation I think.
        sendPacket(new JMWSActionPayload(data));
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

    public static void shareWith(UUID to, ClientObject<? extends ClientObjectWrapper> obj)
    {
        sendString(CommandFactory.makeShareRequestForServer(PlayerUtils.ourUUID(), to, obj.getObjectWrapper().getIdentifier(), obj.getObjectType()));
    }

    public static void removeShareWith(UUID subject, ClientObject<? extends ClientObjectWrapper> waypoint)
    {
        sendString(CommandFactory.makeUnshareRequestForUserOnServer(PlayerUtils.ourUUID(), subject, waypoint.getObjectWrapper().getIdentifier(), waypoint.getObjectType()));
    }

    public static void removeShareFromAll(ClientObject<? extends ClientObjectWrapper> shareableObject)
    {
        sendString(CommandFactory.makeUnshareRequestForAllOnServer(PlayerUtils.ourUUID(), shareableObject.getObjectWrapper().getIdentifier(), shareableObject.getObjectType()));
    }

    public static void makeGlobal(ClientObject<? extends ClientObjectWrapper> globalObject, boolean global)
    {
        sendString(CommandFactory.makeGlobalRequestForServer(PlayerUtils.ourUUID(), globalObject.getObjectWrapper().getIdentifier(), globalObject.getObjectType(), global));
    }

}
