package me.brynview.navidrohim.jm_server_test.common;

import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.client.plugin.WaypointPayload;

import java.util.Map;

public class SavedWaypoint {

    String rawPacketData;
    Map<String, String> rawJsonData;

    String name;
    String playerUUID;

    String ix;
    String iy;
    String iz;
    String dim;

    public SavedWaypoint(WaypointPayload payload) {

        this.rawPacketData = payload.jsonData();
        this.rawJsonData = payload.getJsonData();

        this.playerUUID = this.rawJsonData.get("uuid");
        this.name = this.rawJsonData.get("name");

        JMServerTest.LOGGER.debug(this.rawPacketData);

        this.ix = this.rawJsonData.get("x");
        this.iy = this.rawJsonData.get("y");
        this.iz = this.rawJsonData.get("z");
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
        return Integer.getInteger(this.ix);
    }
    public Integer getWaypointY() {
        return Integer.getInteger(this.iy);
    }
    public Integer getWaypointZ() {
        return Integer.getInteger(this.iz);
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
