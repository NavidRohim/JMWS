package me.brynview.navidrohim.jmws.client.network;

import commonnetwork.api.Network;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;

import java.util.UUID;

public class ClientNetworkDispatcher {
    private static void sendString(String data)
    {
        // ignoreCheck is true because forge broke packet validation, I think.
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

    public static void updateObject(ClientBaseObjectWrapper<?> object)
    {
        sendString(CommandFactory.makeUpdateObjectRequest(object));
    }

    public static void transitionToNewCustomData(String objectIdentifier, UUID owner, boolean isGlobal, ServerSyncRegistryEntry transitionType)
    {
        sendString(CommandFactory.makeTransitionObjectRequestForLegacyCustomData(objectIdentifier, owner, isGlobal, transitionType));
    }

    public static void transitionOldObject(String objectIdentifier, String filename, ServerSyncRegistryEntry transitionType)
    {
        sendString(CommandFactory.makeTransitionObjectRequest(filename, objectIdentifier, transitionType));
    }

    public static void makeGlobal(ClientBaseObjectWrapper<?> globalObject, boolean global)
    {
        sendString(CommandFactory.makeGlobalRequestForServer(PlayerUtils.ourUUID(), globalObject.getIdentifier(), globalObject.getType(), global)); // TODO
    }

    public static class PeerToPeer
    {
        public static void shareWith(UUID to, ClientBaseObjectWrapper<?> shareableObject)
        {
            sendString(CommandFactory.PeerToPeer.shareToUser(to, shareableObject)); // PLACEHOLDER
        }

        public static void acceptShare(ShareRequest shareRequest)
        {
            sendString(CommandFactory.PeerToPeer.acceptShare(shareRequest));
        }

        public static void declineShare(UUID shareRequest, String messageKey, String... translationArgs)
        {
            sendString(CommandFactory.PeerToPeer.declineWithReason(shareRequest, messageKey, translationArgs));
        }

        public static void declineShare(UUID shareRequest)
        {
            sendString(CommandFactory.PeerToPeer.declineWithReason(shareRequest, "sharing.jmws.share_rejected", JMWSCommon.minecraftClientInstance.player.getPlainTextName()));
        }

        public static void removeShare(UUID with, SyncInformation syncInformation)
        {
            sendString(CommandFactory.makeStopShareRequest(with, syncInformation));
        }
    }
}
