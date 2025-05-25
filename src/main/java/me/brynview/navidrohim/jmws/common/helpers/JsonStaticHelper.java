package me.brynview.navidrohim.jmws.common.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.common.io.JMWSIOInterface;
import me.brynview.navidrohim.jmws.common.enums.WaypointPayloadCommand;
import net.minidev.json.JSONObject;

import java.util.*;

public class JsonStaticHelper {

    public static String makeBaseJsonRequest(WaypointPayloadCommand command, List<Object> arguments) {
        JSONObject json = new JSONObject();
        if (arguments == null)
        {
            arguments = List.of();
        }

        json.put("command", command);
        json.put("arguments", arguments);

        return json.toJSONString();
    }

    public static String makeDeleteRequestJson(String waypointFilename, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_WAYPOINT, List.of(waypointFilename, silent));
    }

    public static String makeDeleteGroupRequestJson(String groupFilename, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_GROUP, List.of(groupFilename, silent));
    }
    public static String makeWaypointSyncRequestJson() {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, List.of(Map.of(), Map.of()));
    }

    public static String makeCreationRequestJson(Waypoint waypoint, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_CREATE, List.of(waypoint.toString(), silent));
    }

    public static String makeGroupCreationRequestJson(WaypointGroup waypointGroup, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_CREATE_GROUP, List.of(waypointGroup.toString(), silent));
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, HashMap<String, String> jsonGroupArray) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, List.of(jsonArray, jsonGroupArray));
    }

    public static String makeServerSyncRequestJson() {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.REQUEST_CLIENT_SYNC, null);
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.CLIENT_ALERT, List.of(message, overlay));
    }

    public static String makeDisplayNextUpdateRequestJson() {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DISPLAY_NEXT_UPDATE, null);
    }

    public static String makeDisplayIntervalRequestJson() {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DISPLAY_INTERVAL, null);
    }

    public static String makeDeleteClientObjectRequestJson(String waypointUUID, JMWSIOInterface.FetchType fetchType) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_WAYPOINT, List.of(waypointUUID, fetchType));
    }
    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}

// this whole class is scuffed. I cannot be hard coding json strings bro. must change.
