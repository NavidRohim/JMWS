package me.brynview.navidrohim.jmws.common.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.JMServer;
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

    public static String makeHandshakeRequestJson() {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_HANDSHAKE, null);
    }

    public static String makeDeleteRequestJson(String waypointFilename, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_WAYPOINT, List.of(waypointFilename, silent));
    }

    public static String makeWaypointSyncRequestJson() {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, null);
    }

    public static String makeCreationRequestJson(Waypoint waypoint, boolean silent) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_CREATE, List.of(waypoint.toString(), silent));
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, List.of(jsonArray));
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

    public static String makeDeleteClientWaypointRequestJson(String waypointUUID) {
        return JsonStaticHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_WAYPOINT, List.of(waypointUUID));
    }
    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}

// this whole class is scuffed. I cannot be hard coding json strings bro. must change.
