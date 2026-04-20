package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Dataclass that holds a synced waypoint from the server. Contains data to make a local waypoint.
 */
public class ServerWaypoint extends ServerObject {
    // Main defining information
    String groupId;

    public int x;
    public int y;
    public int z;
    public String primaryDimension;

    public static ServerSyncRegistry serverSyncRegistry = ServerSyncRegistry.WAYPOINT;

    public ServerWaypoint(JsonObject payload, UUID playerUUID) {
        super(payload, playerUUID);

        JsonObject pos = payload.get("pos").getAsJsonObject();

        this.x = pos.get("x").getAsInt();
        this.y = pos.get("y").getAsInt();
        this.z = pos.get("z").getAsInt();
        this.primaryDimension = pos.get("primaryDimension").getAsString();

        // Main defining information
        this.groupId = payload.get("groupId").getAsString();
    }

    public static ServerWaypoint getFromPath(Path waypointPath, UUID ownerUUID)
    {
        return getWaypointFromFile(waypointPath, ownerUUID);
    }

    public static boolean createWaypoint(JsonObject jsonObject, UUID playerUUID)
    {
        Constants.getLogger().info(jsonObject.toString());
        ServerWaypoint wp = new ServerWaypoint(jsonObject, playerUUID);
        return wp.create();
    }

    @Nullable
    public static ServerWaypoint getWaypointFromFile(Path waypointPath, UUID playerUUID)
    {
        JsonObject waypointLocalData = JMWSServerIO.getObjectDataFromDisk(waypointPath, true);
        if (waypointLocalData != null) {
            return new ServerWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }

    @Nullable
    public static ServerWaypoint getWaypointFromUniqueIdentifier(String waypointIdentifier, UUID user)
    {
        Path objPath = JMWSServerIO.getObjectPathFromUniqueIdentifier(waypointIdentifier, ServerSyncRegistry.WAYPOINT);
        return getWaypointFromFile(objPath, user);
    }

    public String getWaypointGroupId() { return this.groupId; }

    public String getDifferentiator() { return "X=%s Y=%s Z=%s %s".formatted(x, y, z, this.primaryDimension); }

    @Override
    public ServerSyncRegistry getObjectType()
    {
        return serverSyncRegistry;
    }

}
