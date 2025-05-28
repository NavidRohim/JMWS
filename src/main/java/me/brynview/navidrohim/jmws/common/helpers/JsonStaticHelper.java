package me.brynview.navidrohim.jmws.common.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
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

    public static String makeDeleteRequestJson(String waypointFilename, boolean silent, boolean all) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_WAYPOINT, List.of(waypointFilename, silent, all));
    }

    public static String makeDeleteGroupRequestJson(String groupFilename, boolean silent, boolean all) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_GROUP, List.of(groupFilename, silent, all));
    }
    public static String makeWaypointSyncRequestJson(boolean sendAlert) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, List.of(Map.of(), Map.of(), sendAlert));
    }

    public static String makeCreationRequestJson(Waypoint waypoint, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_CREATE, List.of(waypoint.toString(), silent));
    }

    public static String makeGroupCreationRequestJson(WaypointGroup waypointGroup, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_CREATE_GROUP, List.of(waypointGroup.toString(), silent));
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

// this whole class is scuffed. I cannot be hard coding json strings bro. must change.
