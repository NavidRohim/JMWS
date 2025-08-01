package me.brynview.navidrohim.jmws.common.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.display.WaypointGroup;
import me.brynview.navidrohim.jmws.common.enums.WaypointPayloadCommand;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JsonStaticHelper {

    public static class PacketCommand {
        WaypointPayloadCommand command;
        List<Object> arguments;

        public PacketCommand(WaypointPayloadCommand command, List<Object> arguments) {
            this.command = command;
            this.arguments = arguments;
        }
    }

    public static String makeBaseJsonRequest(WaypointPayloadCommand command, List<Object> arguments) {
        Gson json = new Gson();
        return json.toJson(new PacketCommand(command, arguments));
    }

    public static String makeDeleteRequestJson(String waypointFilename, boolean silent, boolean all) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_WAYPOINT, List.of(waypointFilename, silent, all));
    }

    public static String makeDeleteGroupRequestJson(UUID playerUUID, @Nullable String groupUniversalIdentifier, @Nullable String groupGUID, boolean silent, boolean removeAllWaypointsInGroup, boolean deleteAllGroups) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_GROUP, List.of(
                playerUUID,
                groupUniversalIdentifier,
                groupGUID,
                silent,
                removeAllWaypointsInGroup,
                deleteAllGroups));
    }

    public static String makeWaypointSyncRequestJson(boolean sendAlert) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, List.of(Map.of(), Map.of(), sendAlert));
    }

    public static String makeCreationRequestJson(journeymap.client.api.display.Waypoint waypoint, boolean silent, boolean isUpdate) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_CREATE, List.of(waypoint.toString(), silent, isUpdate));
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, HashMap<String, String> jsonGroupArray, boolean sendAlert) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, List.of(jsonArray, jsonGroupArray, sendAlert));
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, boolean isError) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.CLIENT_ALERT, List.of(message, overlay, isError));
    }

    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}
