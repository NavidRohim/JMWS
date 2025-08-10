package me.navidrohim.jmws.client.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SavedWaypoint extends SavedObject {

    // Packet information
    String rawPacketData;
    Map<String, String> rawJsonData;

    // Main defining information
    String name;
    String playerUUID;
    String origin;

    // Identifier (used for code)

    String universalIdentifier;

    // location data
    Integer ix;
    Integer iy;
    Integer iz;

    List<String> dimensions;

    // User settings

    Boolean enabled;


    public SavedWaypoint(JsonObject payload, UUID playerUUID) {

        String iconSettings = payload.get("icon").getAsString();

        // Packet information
        this.rawPacketData = payload.toString();

        // Main defining information
        this.name = payload.get("name").getAsString();
        this.playerUUID = playerUUID.toString();
        this.origin = payload.get("origin").getAsString();

        // Identifier (used for code)
        //this.universalIdentifier = payload.get("customData").getAsString();

        // location data
        this.ix = (int) payload.get("x").getAsDouble();
        this.iy = (int) payload.get("y").getAsDouble();
        this.iz = (int) payload.get("z").getAsDouble();

        this.dimensions = StreamSupport.stream(payload.get("dimensions").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .collect(Collectors.toList());

        // User settings
        this.enabled = payload.get("enable").getAsBoolean();
    }

    // Main defining data
    public String getUniversalIdentifier() { return this.universalIdentifier; }
    public String getRawPacketData() { return this.rawPacketData; }

    // Locations
    public Integer getWaypointX() {
        return this.ix;
    }
    public Integer getWaypointY() {
        return this.iy;
    }
    public Integer getWaypointZ() {
        return this.iz;
    }


    // User settings

    // Other
    public Map<String, String> getRawJsonData() {
        return rawJsonData;
    }
}
