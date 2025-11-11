package me.brynview.navidrohim.jmws.common.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Dataclass that holds a synced waypoint from the server. Contains data to make a local waypoint.
 */
public class SavedWaypoint extends SavedObject {

    // Packet information
    String rawPacketData;

    // Main defining information
    String groupId;

    // Identifier (used for code)
    String universalIdentifier;

    // location data
    Integer ix;
    Integer iy;
    Integer iz;

    // User settings
    public UserSharingFile sharing;

    public SavedWaypoint(JsonObject payload, UUID playerUUID) {

        JsonObject pos = payload.get("pos").getAsJsonObject();
        JsonObject userSettings = payload.get("settings").getAsJsonObject();
        JsonObject iconSettings = payload.get("icon").getAsJsonObject();

        // Packet information
        this.rawPacketData = payload.toString();

        // Main defining information
        this.groupId = payload.get("groupId").getAsString();

        // Identifier (used for code)
        this.universalIdentifier = payload.get("customData").getAsString();
        this.groupIdentifier = payload.get("guid").getAsString();

        // location data
        this.ix = (int) pos.get("x").getAsDouble();
        this.iy = (int) pos.get("y").getAsDouble();
        this.iz = (int) pos.get("z").getAsDouble();

        // User settings
        this.sharing = new UserSharingFile(playerUUID);
    }

    public String getCustomData() { return this.universalIdentifier; }
    public String getWaypointGroupId() { return this.groupId; }
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
}
