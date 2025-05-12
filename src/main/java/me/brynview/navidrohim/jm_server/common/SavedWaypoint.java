package me.brynview.navidrohim.jm_server.common;

import com.google.gson.JsonObject;
import me.brynview.navidrohim.jm_server.common.payloads.RegisterUserPayload;

import java.util.Map;
import java.util.UUID;

public class SavedWaypoint {

    String rawPacketData;
    Map<String, String> rawJsonData;

    String name;
    String playerUUID;
    String waypointLocalID;

    Integer ix;
    Integer iy;
    Integer iz;
    String dim;

    Integer colour;
    String universalIdentifier;

    public SavedWaypoint(JsonObject payload, UUID playerUUID) {

        this.rawPacketData = payload.toString();
        this.playerUUID = playerUUID.toString();
        this.name = payload.get("name").getAsString();

        JsonObject pos = payload.get("pos").getAsJsonObject();

        this.ix = (int) pos.get("x").getAsDouble();
        this.iy = (int) pos.get("y").getAsDouble();
        this.iz = (int) pos.get("z").getAsDouble();
        this.dim = payload.get("dimensions").getAsJsonArray().get(0).getAsString();

        this.colour = payload.get("color").getAsInt();

        this.waypointLocalID = payload.get("guid").getAsString();
        this.universalIdentifier = payload.get("customData").getAsString();
    }

    public SavedWaypoint(RegisterUserPayload payload) {

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

    public UUID getPlayerUUID() {
        return UUID.fromString(this.playerUUID);
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
    public Integer getWaypointColour() {
        return this.colour;
    }
    public String getUniversalIdentifier() {
        return this.universalIdentifier;
    }
    public String getWaypointLocalID() {
        return this.waypointLocalID;
    }
    public Map<String, String> getRawJsonData() {
        return rawJsonData;
    }

    public String getRawPacketData() {
        return rawPacketData;
    }
}
