package me.brynview.navidrohim.jm_server_test.client.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.brynview.navidrohim.jm_server_test.JMServerTest;

import java.util.Map;

public class SavedWaypoint {

    String rawPacketData;
    Map<String, String> rawJsonData;

    String name;
    String playerUUID;

    String ix;
    String iy;
    String iz;

    SavedWaypoint(WaypointPayload payload) {

        this.rawPacketData = payload.jsonData();
        this.rawJsonData = payload.getJsonData();

        this.playerUUID = this.rawJsonData.get("player_uuid");
        this.name = this.rawJsonData.get("name");

        JMServerTest.LOGGER.debug(this.rawPacketData);

        this.ix = this.rawJsonData.get("x");
        this.iy = this.rawJsonData.get("y");
        this.iz = this.rawJsonData.get("z");
    }

    public String getWaypointName() {
        return this.name;
    }

    public String getPlayerUUID() {
        return this.playerUUID;
    }

    // Coordinates
    public Integer getWaypointX() {
        return Integer.getInteger(this.ix);
    }
    public Integer getWaypointY() {
        return Integer.getInteger(this.iy);
    }
    public Integer getWaypointZ() {
        return Integer.getInteger(this.iz);
    }

    public Map<String, String> getRawJsonData() {
        return rawJsonData;
    }

    public String getRawPacketData() {
        return rawPacketData;
    }
}
