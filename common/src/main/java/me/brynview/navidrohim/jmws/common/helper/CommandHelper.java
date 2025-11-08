package me.brynview.navidrohim.jmws.common.helper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.shared.ShareRequest;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.enums.ObjectPayloadCommands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandHelper {
    public static class PacketCommand {
        ObjectPayloadCommands command;
        Object[] arguments;

        public PacketCommand(ObjectPayloadCommands command, Object... arguments) {
            this.command = command;
            this.arguments = arguments;
        }
    }

    public static String makeBaseJsonRequest(ObjectPayloadCommands command, Object... arguments) {
        Gson json = new Gson();
       return json.toJson(new PacketCommand(command, arguments));
    }

    public static String makeDeleteRequestJson(String waypointIdentifier, boolean silent, boolean all) {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.COMMON_DELETE_WAYPOINT, waypointIdentifier, silent, all);
    }

    public static String makeDeleteGroupRequestJson(UUID playerUUID, String groupUniversalIdentifier, String groupGUID, boolean silent, boolean removeAllWaypointsInGroup, boolean removeGroupItself, boolean deleteAllGroups) {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.COMMON_DELETE_GROUP,
                groupUniversalIdentifier,
                groupGUID,
                silent,
                removeAllWaypointsInGroup,
                removeGroupItself,
                deleteAllGroups);
    }

    public static String makeWaypointSyncRequestJson(boolean sendAlert, boolean isForDeathSync) {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.SYNC, Map.of(), Map.of(), sendAlert, isForDeathSync);
    }

    public static String makeCreationRequestJson(Waypoint waypoint, boolean silent, boolean isUpdate) {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.SERVER_CREATE, waypoint.toString(), silent, isUpdate);
    }

    public static String makeGroupCreationRequestJson(WaypointGroup waypointGroup, boolean silent, boolean isUpdate) {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.SERVER_CREATE_GROUP, waypointGroup.toString(), silent, isUpdate);
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, HashMap<String, String> jsonGroupArray, boolean sendAlert, boolean isDeathSync) {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.SYNC, jsonArray, jsonGroupArray, sendAlert, isDeathSync);
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, boolean isError) {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.CLIENT_ALERT, message, overlay, isError);
    }

    public static String makeObjectShareRequestForUser(String waypoint, UUID to, UUID from, ShareRequest.Direction direction)
    {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.OBJECT_SHARE, waypoint, to, from, FetchType.WAYPOINT, direction);
    }

    public static String makeObjectShareRequestForUser(WaypointGroup waypointGroup)
    {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.OBJECT_SHARE, waypointGroup.toString(), FetchType.GROUP);
    }

    public static String makeObjectShareRequestDecline(UUID originalSender)
    {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.REJECT_SHARE, originalSender);

    }

    public static String makeObjectShareRequestUserBusy(UUID originalSender)
    {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.USER_ALREADY_PROCESSING_SHARE, originalSender);
    }

    public static String makeObjectShareRequestAccept(UUID originalSender)
    {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.AFFIRM_SHARE, originalSender);
    }

    public static String makeUpdateObjectRequest(String objectIdentifier, Waypoint waypoint)
    {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.UPDATE, objectIdentifier, FetchType.WAYPOINT, waypoint.toString());
    }

    public static String makeUpdateObjectRequest(String objectIdentifier, WaypointGroup group)
    {
        return CommandHelper.makeBaseJsonRequest(ObjectPayloadCommands.UPDATE, objectIdentifier, FetchType.GROUP, group.toString());
    }

    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}
