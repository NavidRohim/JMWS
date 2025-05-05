package me.brynview.navidrohim.jm_server_test.common.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.api.v2.common.waypoint.Waypoint;

import java.util.*;

public class JsonStaticHelper {

    public static String makeDeleteJson(String waypointFilename) {
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

    public static String makeCreationRequestResponseJson(HashMap<String, String> jsonArray) {
        return "{\n" +
                "  \"command\": \"creation_response\",\n" +
                "  \"arguments\": [" + jsonArray + "]\n" +
                "}";
    }
    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}
