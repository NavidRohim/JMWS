package me.brynview.navidrohim.jmws.common.utils;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncRegistry;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandFactory {

    public static class PacketCommand {
        Commands command;
        Object[] arguments;

        public PacketCommand(Commands command, Object... arguments) {
            this.command = command;
            this.arguments = arguments;
        }
    }

    public static String makeBaseJsonRequest(Commands command, Object... arguments) {
       return JMWSCommon.gson.toJson(new PacketCommand(command, arguments));
    }

    public static String deleteWaypoint(String waypointIdentifier, boolean silent, boolean all) {
        return CommandFactory.makeBaseJsonRequest(Commands.COMMON_DELETE_WAYPOINT, waypointIdentifier, silent, all);
    }

    public static String deleteGroup(String groupUniversalIdentifier, String groupGUID, boolean silent, boolean removeAllWaypointsInGroup, boolean removeGroupItself, boolean isGlobal, boolean deleteAllGroups) {
        return CommandFactory.makeBaseJsonRequest(Commands.COMMON_DELETE_GROUP,
                groupUniversalIdentifier,
                groupGUID,
                silent,
                removeAllWaypointsInGroup,
                removeGroupItself,
                isGlobal,
                deleteAllGroups);
    }

    public static String makeWaypointSyncRequestJson(boolean sendAlert, boolean isForDeathSync) {
        return CommandFactory.makeBaseJsonRequest(Commands.SYNC, Map.of(), Map.of(), sendAlert, isForDeathSync);
    }

    public static String makeCreationRequestJson(Waypoint waypoint, boolean silent) {
        return CommandFactory.makeBaseJsonRequest(Commands.SERVER_CREATE, waypoint.toString(), silent);
    }

    public static String makeGroupCreationRequestJson(WaypointGroup waypointGroup, boolean silent) {
        return CommandFactory.makeBaseJsonRequest(Commands.SERVER_CREATE_GROUP, waypointGroup.toString(), silent, false);
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, HashMap<String, String> jsonGroupArray, boolean sendAlert, boolean isDeathSync) { // SERVER ONLY
        return CommandFactory.makeBaseJsonRequest(Commands.SYNC, jsonArray, jsonGroupArray, sendAlert, isDeathSync);
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, MessageType messageType) { // SERVER ONLY
        return CommandFactory.makeBaseJsonRequest(Commands.CLIENT_ALERT, message, overlay, messageType.toString());
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, MessageType messageType, String... translationArgs) { // SERVER ONLY
        return CommandFactory.makeBaseJsonRequest(Commands.CLIENT_ALERT, message, overlay, messageType.toString(), translationArgs);
    }

    public static String makeUpdateObjectRequest(ClientBaseObjectWrapper<?> objectWrapper)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, objectWrapper.getIdentifier(), objectWrapper.getType().toString(), objectWrapper.getGlobal(), objectWrapper.getSerialization());
    }

    public static String makeTransitionObjectRequest(String objectIdentifier, String filename, ServerSyncRegistryEntry transitionType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.TRANSITION, objectIdentifier, filename, transitionType);
    }

    public static String makeTransitionObjectRequestForLegacyCustomData(String objectIdentifier, UUID owner, boolean isGlobal, ServerSyncRegistryEntry transitionType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.TRANSITION_NEW_DATA, objectIdentifier, owner, isGlobal, transitionType);
    }

    public static String makeGlobalRequestForServer(UUID from, String objectIdentifier, ClientSyncRegistry syncRegistryType, boolean global)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.MAKE_GLOBAL, from, objectIdentifier, syncRegistryType.getId(), global);
    }

    public static String makeStopShareRequest(UUID with, SyncInformation syncInformation)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.SERVER_REMOVE_SHARE_WITH, with, syncInformation.serialize());
    }

    public static class PeerToPeer
    {
        public static String makeBaseRequestForUser(UUID to, PeerToPeerCommand command, Object... arguments)
        {
            return CommandFactory.makeBaseJsonRequest(Commands.SPECIAL_FORWARD_TO_CLIENT, command, to, arguments);
        }

        public static String shareToUser(UUID to, ClientBaseObjectWrapper<?> shareableObject)
        {
            // As we are sharing an object directly with another user, we must serialize and send the full object and the second argument which indicates
            // how to encode the raw data into a usable object.
            return makeBaseRequestForUser(to, PeerToPeerCommand.CLIENT_SHARE_REQUEST, shareableObject.getSerialization(), shareableObject.getType().getId());
        }

        /* Share responses*/

        public static String declineWithReason(UUID to, String translationKeyReason, String... translationArgs)
        {
            return makeBaseRequestForUser(to, PeerToPeerCommand.CLIENT_REJECTED_SHARE_WITH_REASON, translationKeyReason, translationArgs);
        }

        public static String acceptShare(ShareRequest shareRequest)
        {
            return CommandFactory.makeBaseJsonRequest(Commands.AFFIRM_SHARE, shareRequest.currentSharedObject.getInfo().serialize(), PlayerUtils.ourUUID());
        }
    }
    /*
     * Enums for different packet commands
     */

    public enum Commands {

        // Waypoint handling
        SERVER_CREATE,
        COMMON_DELETE_WAYPOINT,

        // Group handling
        SERVER_CREATE_GROUP,
        COMMON_DELETE_GROUP,

        // Utility
        SYNC,
        CLIENT_ALERT,
        UPDATE,

        // Object sharing (server)
        AFFIRM_SHARE, // Confirm user wants shared object
        SERVER_REMOVE_SHARE_WITH,

        // Global
        MAKE_GLOBAL,

        // Legacy
        TRANSITION,
        TRANSITION_NEW_DATA,

        // Special
        SPECIAL_FORWARD_TO_CLIENT,
        UNKNOWN
    }

    public enum PeerToPeerCommand {
        CLIENT_SHARE_REQUEST,
        CLIENT_REJECTED_SHARE_WITH_REASON,

        SERVER_ADD_SHARE
    }
}
