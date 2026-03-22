package me.brynview.navidrohim.jmws.common.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.enums.ShareRequestDirection;

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
       return CommonClass.gson.toJson(new PacketCommand(command, arguments));
    }

    public static String makeDeleteRequestJson(String waypointIdentifier, boolean silent, boolean all) {
        return CommandFactory.makeBaseJsonRequest(Commands.COMMON_DELETE_WAYPOINT, waypointIdentifier, silent, all);
    }

    public static String makeDeleteGroupRequestJson(String groupUniversalIdentifier, String groupGUID, boolean silent, boolean removeAllWaypointsInGroup, boolean removeGroupItself, boolean isGlobal, boolean deleteAllGroups) {
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

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, HashMap<String, String> jsonGroupArray, boolean sendAlert, boolean isDeathSync) {
        return CommandFactory.makeBaseJsonRequest(Commands.SYNC, jsonArray, jsonGroupArray, sendAlert, isDeathSync);
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, MessageType messageType) {
        return CommandFactory.makeBaseJsonRequest(Commands.CLIENT_ALERT, message, overlay, messageType);
    }

    public static String makeObjectShareRequestForUser(String waypoint, UUID to, UUID from, ShareRequestDirection direction, ObjectType objectType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.OBJECT_SHARE, waypoint, to, from, objectType, direction);
    }

    public static String makeObjectShareRequestDecline(UUID originalSender)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.REJECT_SHARE, originalSender, PlayerHelper.ourUUID());
    }

    public static String makeObjectShareRequestDeclineWithMessage(UUID originalSender, String messageKey)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.USER_ALREADY_PROCESSING_SHARE, originalSender, PlayerHelper.ourUUID(), messageKey);
    }

    public static String makeObjectShareRequestAccept(ShareRequest shareRequest)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.AFFIRM_SHARE, shareRequest.originalSender, shareRequest.requestIdentifier, shareRequest.sharedObjectType, shareRequest.meantFor);
    }

    public static String makeUpdateObjectRequest(String objectIdentifier, boolean isGlobal, Waypoint waypoint)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, objectIdentifier, ObjectType.WAYPOINT, isGlobal, waypoint.toString());
    }

    public static String makeUpdateObjectRequest(String objectIdentifier, boolean isGlobal,  WaypointGroup group)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, objectIdentifier, ObjectType.GROUP, isGlobal, group.toString());
    }

    public static String makeTransitionObjectRequest(String objectIdentifier, String filename, ObjectType transitionType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.TRANSITION, objectIdentifier, filename, transitionType);
    }

    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    /**
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

        // Object sharing
        OBJECT_SHARE, // Share waypoint / group
        AFFIRM_SHARE, // Confirm user wants shared object
        REJECT_SHARE, // User doesnt want shared object.

        // Object sharing errors
        USER_ALREADY_PROCESSING_SHARE, // User is already processing another share request

        UPDATE,
        TRANSITION
    }
}
