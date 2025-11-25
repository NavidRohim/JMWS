package me.brynview.navidrohim.jmws.server.objects;

import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.Server;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    public static ObjectType objectType = ObjectType.WAYPOINT;

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
        return JMWSServerIO.getWaypointFromFile(waypointPath, ownerUUID);
    }

    public String getWaypointGroupId() { return this.groupId; }

    public String getDifferentiator() { return "X=%s Y=%s Z=%s %s".formatted(x, y, z, this.primaryDimension); }

    public static List<Path> getGlobalWaypoints()
    {
        List<Path> wp = new ArrayList<>();
        for (Path path : JMWSServerIO.getAllObjects(ObjectType.WAYPOINT).toList())
        {
            if (path.toString().contains("SERVER"))
            {
                wp.add(path);
            }
        }
        return wp;
    }

    @Override
    public ObjectType getObjectType()
    {
        return objectType;
    }

}
