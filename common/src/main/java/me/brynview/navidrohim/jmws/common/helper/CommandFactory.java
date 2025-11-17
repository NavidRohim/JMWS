package me.brynview.navidrohim.jmws.common.helper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.share.ShareRequest;
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
        Gson json = new Gson();
       return json.toJson(new PacketCommand(command, arguments));
    }

    public static String makeDeleteRequestJson(String waypointIdentifier, boolean silent, boolean all) {
        return CommandFactory.makeBaseJsonRequest(Commands.COMMON_DELETE_WAYPOINT, waypointIdentifier, silent, all);
    }

    public static String makeDeleteGroupRequestJson(String groupUniversalIdentifier, String groupGUID, boolean silent, boolean removeAllWaypointsInGroup, boolean removeGroupItself, boolean deleteAllGroups) {
        return CommandFactory.makeBaseJsonRequest(Commands.COMMON_DELETE_GROUP,
                groupUniversalIdentifier,
                groupGUID,
                silent,
                removeAllWaypointsInGroup,
                removeGroupItself,
                deleteAllGroups);
    }

    public static String makeWaypointSyncRequestJson(boolean sendAlert, boolean isForDeathSync) {
        return CommandFactory.makeBaseJsonRequest(Commands.SYNC, Map.of(), Map.of(), sendAlert, isForDeathSync);
    }

    public static String makeCreationRequestJson(Waypoint waypoint, boolean silent) {
        return CommandFactory.makeBaseJsonRequest(Commands.SERVER_CREATE, waypoint.toString(), silent);
    }

    public static String makeGroupCreationRequestJson(WaypointGroup waypointGroup, boolean silent, boolean isUpdate) {
        return CommandFactory.makeBaseJsonRequest(Commands.SERVER_CREATE_GROUP, waypointGroup.toString(), silent, isUpdate);
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, HashMap<String, String> jsonGroupArray, boolean sendAlert, boolean isDeathSync) {
        return CommandFactory.makeBaseJsonRequest(Commands.SYNC, jsonArray, jsonGroupArray, sendAlert, isDeathSync);
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, JMWSMessageType messageType) {
        return CommandFactory.makeBaseJsonRequest(Commands.CLIENT_ALERT, message, overlay, messageType);
    }

    public static String makeObjectShareRequestForUser(String waypoint, UUID to, UUID from, ShareRequest.Direction direction, ObjectType objectType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.OBJECT_SHARE, waypoint, to, from, objectType, direction);
    }

    public static String makeObjectShareRequestForUser(WaypointGroup waypointGroup)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.OBJECT_SHARE, waypointGroup.toString(), ObjectType.GROUP);
    }

    public static String makeObjectShareRequestDecline(UUID originalSender)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.REJECT_SHARE, originalSender);
    }

    public static String makeObjectShareRequestDeclineWithMessage(UUID originalSender, String messageKey)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.USER_ALREADY_PROCESSING_SHARE, originalSender, PlayerHelper.ourUUID(), messageKey);
    }

    public static String makeObjectShareRequestAccept(ShareRequest shareRequest)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.AFFIRM_SHARE, shareRequest.originalSender, shareRequest.requestIdentifier, shareRequest.sharedObjectType);
    }

    public static String makeUpdateObjectRequest(String objectIdentifier, Waypoint waypoint)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, objectIdentifier, ObjectType.WAYPOINT, waypoint.toString());
    }

    public static String makeUpdateObjectRequest(String objectIdentifier, WaypointGroup group)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, objectIdentifier, ObjectType.GROUP, group.toString());
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

        UPDATE

    }
}
