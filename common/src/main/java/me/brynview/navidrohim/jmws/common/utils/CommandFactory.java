package me.brynview.navidrohim.jmws.common.utils;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.syncing.SyncObjectType;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;

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

    public static String makeObjectShareRequestAccept(ShareRequest shareRequest)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.AFFIRM_SHARE, shareRequest.originalSender, shareRequest.requestIdentifier, shareRequest.sharedObjectType, shareRequest.meantFor);
    }

    public static String makeUpdateWaypointRequest(ClientWaypointWrapper waypoint)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, waypoint.getIdentifier(), ObjectType.WAYPOINT, waypoint.getGlobal(), waypoint.getSerialization());
    }

    public static String makeUpdateObjectRequest(ClientBaseObjectWrapper<?> objectWrapper)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, objectWrapper.getIdentifier(), objectWrapper.getType().toString(), objectWrapper.getGlobal(), objectWrapper.getSerialization());
    }

    public static String makeUpdateGroupRequest(ClientGroupWrapper group)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, group.getIdentifier(), ObjectType.GROUP, group.getGlobal(), group.getSerialization());
    }

    public static String makeTransitionObjectRequest(String objectIdentifier, String filename, ObjectType transitionType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.TRANSITION, objectIdentifier, filename, transitionType);
    }

    public static String makeTransitionObjectRequestForLegacyCustomData(String objectIdentifier, UUID owner, boolean isGlobal, ObjectType transitionType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.TRANSITION_NEW_DATA, objectIdentifier, owner, isGlobal, transitionType);
    }

    public static String makeUnshareRequestForAllOnServer(UUID from, String objectIdentifier, SyncObjectType syncObjectType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.REMOVE_SHARE_WITH_ALL, from, objectIdentifier, syncObjectType.getId());
    }

    public static String makeGlobalRequestForServer(UUID from, String objectIdentifier, SyncObjectType syncObjectType, boolean global)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.MAKE_GLOBAL, from, objectIdentifier, syncObjectType.getId(), global);
    }

    public static class PeerToPeer
    {
        public static String makeBaseRequestForUser(UUID to, PeerToPeerCommand command, Object... arguments)
        {
            return CommandFactory.makeBaseJsonRequest(Commands.SPECIAL_FORWARD_TO_CLIENT, command, to, arguments);
        }

        public static String shareToUser(UUID to, ClientBaseObjectWrapper<?> shareableObject)
        {
            return makeBaseRequestForUser(to, PeerToPeerCommand.CLIENT_SHARE_REQUEST, shareableObject.getSerialization(), shareableObject.getType().getId());
        }

        public static String declineWithReason(UUID to, String translationKeyReason)
        {
            return makeBaseRequestForUser(to, PeerToPeerCommand.CLIENT_REJECTED_SHARE_WITH_REASON, translationKeyReason);
        }

        public static String removeShareWith(UUID with, String identifier, SyncObjectType type)
        {
            return makeBaseRequestForUser(with, PeerToPeerCommand.CLIENT_REMOVE_SHARE_WITH, identifier, type.getId());
        }

        public static String removeShareWithAll(String identifier, SyncObjectType type)
        {
            return CommandFactory.makeBaseJsonRequest(Commands.REMOVE_SHARE_WITH_ALL, PlayerUtils.ourUUID(), identifier, type.getId());
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
        REQUEST_CLIENT_SYNC,
        CLIENT_ALERT,
        COMMON_DISPLAY_INTERVAL,
        COMMON_DISPLAY_NEXT_UPDATE,
        UPDATE,

        // Object sharing (server)
        OBJECT_SHARE, // Share waypoint / group
        AFFIRM_SHARE, // Confirm user wants shared object
        REJECT_SHARE, // User doesnt want shared object.
        REMOVE_SHARE_WITH_ALL,

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
        CLIENT_ACCEPTED_SHARE,
        CLIENT_REMOVE_SHARE_WITH
    }
}
