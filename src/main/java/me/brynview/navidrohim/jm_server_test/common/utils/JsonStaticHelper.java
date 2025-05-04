package me.brynview.navidrohim.jm_server_test.common.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonStaticHelper {

    public static String makeDeleteJson(String waypointFilename) {
        return "{\n" +
                "  \"command\": \"delete\",\n" +
                "  \"arguments\": [\n" +
                "  \"" + waypointFilename + "\"]\n" +
                "}";
    }
    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }
}
