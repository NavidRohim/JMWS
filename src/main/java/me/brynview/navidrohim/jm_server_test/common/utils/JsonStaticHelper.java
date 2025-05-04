package me.brynview.navidrohim.jm_server_test.common.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import net.minidev.json.JSONObject;

import java.util.*;

public class JsonStaticHelper {

    public static String makeDeleteJson(String waypointFilename, boolean removeAll) {
        if (!removeAll) {
            return "{\n" +
                    "  \"command\": \"delete\",\n" +
                    "  \"arguments\": [\n" +
                    "  \"" + waypointFilename + "\"]\n" +
                    "}";
        } else {
            return "{\n" +
                    "  \"command\": \"update\",\n" +
                    "  \"arguments\": []\n" +
                    "}";
        }

    }
    public static String makeUpdateJson(String oldName, List<? extends Waypoint> waypointList, UUID uuid) {

        HashMap<String, Object> hashtable = new HashMap<>();
        List<String> waypointNameList = new ArrayList<>();

        for (Waypoint waypoint : waypointList) {
            waypointNameList.add(WaypointIOInterface.getWaypointFilename(waypoint, uuid));
        }
        waypointNameList.add(oldName);

        hashtable.put("command", "update");
        hashtable.put("arguments", waypointNameList);
        return new JSONObject(hashtable).toString();

    }
    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}
