package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.common.enums.FetchType;

import java.util.UUID;

/**
 * Dataclass that holds a synced waypoint from the server. Contains data to make a local waypoint.
 */
public class ServerWaypoint extends ServerObject {
    // Main defining information
    String groupId;

    public static FetchType objectType = FetchType.WAYPOINT;

    public ServerWaypoint(JsonObject payload, UUID playerUUID) {
        super(payload, playerUUID);

        JsonObject pos = payload.get("pos").getAsJsonObject();
        JsonObject userSettings = payload.get("settings").getAsJsonObject();
        JsonObject iconSettings = payload.get("icon").getAsJsonObject();

        // Main defining information
        this.groupId = payload.get("groupId").getAsString();

        // Identifier (used for code)
        this.groupIdentifier = payload.get("guid").getAsString();
    }

    public String getWaypointGroupId() { return this.groupId; }

    @Override
    public FetchType getObjectType()
    {
        return objectType;
    }

}
