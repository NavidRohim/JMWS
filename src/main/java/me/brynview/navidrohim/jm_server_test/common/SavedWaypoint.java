package me.brynview.navidrohim.jm_server_test.common;

import com.google.gson.JsonObject;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.client.payloads.WaypointPayloadOutbound;
import me.brynview.navidrohim.jm_server_test.server.payloads.WaypointSendPayload;

import java.util.HashMap;
import java.util.Map;

public class SavedWaypoint {

    String rawPacketData;
    Map<String, String> rawJsonData;

    String name;
    String playerUUID;

    Integer ix;
    Integer iy;
    Integer iz;
    String dim;

    public SavedWaypoint(JsonObject payload) {

        this.rawPacketData = payload.toString();
        this.playerUUID = payload.get("uuid").getAsString();
        this.name = payload.get("name").getAsString();

        this.ix = payload.get("x").getAsInt();
        this.iy = payload.get("y").getAsInt();
        this.iz = payload.get("z").getAsInt();
        this.dim = payload.get("d").getAsString();
    }

    public SavedWaypoint(WaypointPayloadOutbound payload) {

        this.rawPacketData = payload.jsonData();
        this.rawJsonData = payload.getJsonData();

        this.playerUUID = this.rawJsonData.get("uuid");
        this.name = this.rawJsonData.get("name");

        this.ix = Integer.getInteger(rawJsonData.get("x"));
        this.iy = Integer.getInteger(rawJsonData.get("y"));
        this.iz = Integer.getInteger(rawJsonData.get("z"));
        this.dim = this.rawJsonData.get("d");
    }

    public String getWaypointName() {
        return this.name;
    }

    public String getPlayerUUID() {
        return this.playerUUID;
    }

    // Coordinates
    public Integer getWaypointX() {
        return this.ix;
    }
    public Integer getWaypointY() {
        return this.iy;
    }
    public Integer getWaypointZ() {
        return this.iz;
    }
    public String getDimensionString() {
        return this.dim;
    }

    public Map<String, String> getRawJsonData() {
        return rawJsonData;
    }

    public String getRawPacketData() {
        return rawPacketData;
    }
}
