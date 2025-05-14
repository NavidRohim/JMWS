package me.brynview.navidrohim.jm_server.common.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.api.v2.common.waypoint.Waypoint;

import java.util.*;

public class JsonStaticHelper {

    public static String makeDeleteRequestJson(String waypointFilename) {
        return "{\n" +
                "  \"command\": \"delete\",\n" +
                "  \"arguments\": [\n" +
                "  \"" + waypointFilename + "\"]\n" +
                "}";
    }

    public static String makeWaypointRequestJson() {
        return """
                {
                  "command": "request",
                  "arguments": []
                }""";
    }

    public static String makeCreationRequestJson(Waypoint waypoint) {
        return "{\n" +
                "  \"command\": \"create\",\n" +
                "  \"arguments\": [" + waypoint + "]\n" +
                "}";
    }

    public static String makeCreationRequestResponseJson(HashMap<String, String> jsonArray, List<String> waypointIDList) {
        return "{\n" +
                "  \"command\": \"creation_response\",\n" +
                "  \"arguments\": [" + jsonArray + ", " + waypointIDList + "]\n" +
                "}";
    }

    public static String makeServerUpdateRequestJson() {
        return "{\n" +
                "  \"command\": \"update\",\n" +
                "  \"arguments\": []\n" +
                "}";
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay) {
        return "{\n" +
                "  \"command\": \"alert\",\n" +
                "  \"arguments\": [" + message + ", " + overlay + "]\n" +
                "}";
    }
    public static String makeEmptyServerCommandRequestJson(String command) {
        return "{\n" +
                "  \"command\": \"" + command + "\",\n" +
                "  \"arguments\": []\n" +
                "}";
    }

    public static String makeDeleteClientWaypointRequestJson(String waypointUUID) {
        return "{\n" +
                "  \"command\": \"deleteWaypoint\",\n" +
                "  \"arguments\": [\"" + waypointUUID + "\"]\n" +
                "}";
    }
    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}

// this whole class is scuffed. I cannot be hard coding json strings bro. must change.
