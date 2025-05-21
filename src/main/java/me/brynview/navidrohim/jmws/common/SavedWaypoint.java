package me.brynview.navidrohim.jmws.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.JMServer;
import me.brynview.navidrohim.jmws.common.utils.JMWSIOInterface;
import net.minecraft.util.Identifier;

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
    Integer version;
    String name;
    Integer colour;
    String playerUUID;
    String modId;
    String groupId;
    String origin;

    // Identifier (used for code)
    String waypointLocalID;
    String universalIdentifier;

    // location data
    Integer ix;
    Integer iy;
    Integer iz;
    String dim;

    List<String> dimensions;

    // User settings
    Boolean persistent;
    Boolean enabled;
    Boolean showDeviation;

    // Icon information
    Identifier resourceLocation;
    Float opacity;
    Integer textureWidth;
    Integer textureHeight;
    Integer rotation; // what?

    public SavedWaypoint(JsonObject payload, UUID playerUUID) {

        JsonObject pos = payload.get("pos").getAsJsonObject();
        JsonObject userSettings = payload.get("settings").getAsJsonObject();
        JsonObject iconSettings = payload.get("icon").getAsJsonObject();

        // Packet information
        this.rawPacketData = payload.toString();

        // Main defining information
        this.version = payload.get("version").getAsInt();
        this.name = payload.get("name").getAsString();
        this.colour = payload.get("color").getAsInt();
        this.playerUUID = playerUUID.toString();
        this.modId = payload.get("modId").getAsString();
        this.groupId = payload.get("groupId").getAsString();
        this.origin = payload.get("origin").getAsString();

        // Identifier (used for code)
        this.universalIdentifier = payload.get("customData").getAsString();
        this.groupIdentifier = payload.get("guid").getAsString();

        // location data
        this.ix = (int) pos.get("x").getAsDouble();
        this.iy = (int) pos.get("y").getAsDouble();
        this.iz = (int) pos.get("z").getAsDouble();
        this.dim = pos.get("primaryDimension").getAsString();

        this.dimensions = StreamSupport.stream(payload.get("dimensions").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .collect(Collectors.toList());

        // User settings
        this.persistent = userSettings.get("persistent").getAsBoolean();
        this.enabled = userSettings.get("enable").getAsBoolean();
        this.showDeviation = userSettings.get("showDeviation").getAsBoolean();

        // Icon information
        this.resourceLocation = Identifier.of(iconSettings.get("resourceLocation").getAsString());
        this.opacity = iconSettings.get("opacity").getAsFloat();
        this.textureWidth = iconSettings.get("textureWidth").getAsInt();
        this.textureHeight = iconSettings.get("textureHeight").getAsInt();
        this.rotation = iconSettings.get("rotation").getAsInt();

    }

    // Main defining data
    public Integer getWaypointVersion() { return this.version; }
    public Integer getWaypointColour() {
        return this.colour;
    }
    public UUID getPlayerUUID() {
        return UUID.fromString(this.playerUUID);
    }
    public String getUniversalIdentifier() { return this.universalIdentifier; }
    public String getWaypointModId() { return this.modId; }
    public String getWaypointGroupId() { return this.groupId; }
    public String getWaypointOrigin() { return this.origin; }
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
    public String getDimensionString() {
        return this.dim;
    }
    public List<String> getWaypointDimensions() { return this.dimensions; }

    // User settings
    public Boolean getWaypointPersistence() { return this.persistent; }
    public Boolean getWaypointEnabled() { return this.enabled; }
    public Boolean getWaypointDeviation() { return this.showDeviation; }

    // Icon information
    public Identifier getWaypointResourceString() { return this.resourceLocation; }
    public Float getWaypointOpacity() { return this.opacity; }
    public Integer getWaypointTextureWidth() { return this.textureWidth; }
    public Integer getWaypointTextureHeight() { return this.textureHeight; }
    public Integer getWaypointRotation() { return this.rotation; }

    // Other
    public Map<String, String> getRawJsonData() {
        return rawJsonData;
    }
}
