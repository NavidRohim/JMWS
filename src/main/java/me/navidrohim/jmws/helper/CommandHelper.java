package me.navidrohim.jmws.helper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import journeymap.client.api.display.Waypoint;
import me.navidrohim.jmws.enums.WaypointPayloadCommand;

import java.util.*;

public class CommandHelper {
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
        return CommandHelper.makeBaseJsonRequest(WaypointPayloadCommand.COMMON_DELETE_WAYPOINT, Arrays.asList(waypointFilename, silent, all));
    }

    public static String makeWaypointSyncRequestJson(boolean sendAlert) {
        return CommandHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, Arrays.asList(Collections.emptyMap(), Collections.emptyMap(), sendAlert));
    }

    public static String makeCreationRequestJson(Waypoint waypoint, boolean silent, boolean isUpdate) {
        return CommandHelper.makeBaseJsonRequest(WaypointPayloadCommand.SERVER_CREATE, Arrays.asList(waypoint.toString(), silent, isUpdate));
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, boolean sendAlert) {
        return CommandHelper.makeBaseJsonRequest(WaypointPayloadCommand.SYNC, Arrays.asList(jsonArray, sendAlert));
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, boolean isError) {
        return CommandHelper.makeBaseJsonRequest(WaypointPayloadCommand.CLIENT_ALERT, Arrays.asList(message, overlay, isError));
    }

    public static JsonObject getJsonObjectFromJsonString(String jsonString) {
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }
}
