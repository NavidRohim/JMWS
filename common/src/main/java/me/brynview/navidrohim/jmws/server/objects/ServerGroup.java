package me.brynview.navidrohim.jmws.server.objects;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Dataclass that holds a synced group from the server. Contains data to make a local group.
 */
public class ServerGroup extends ServerObject {

    public static ObjectType objectType = ObjectType.GROUP;

    public ServerGroup(JsonObject payload, UUID playerUUID) {
        super(payload, playerUUID);

        this.rawPacketData = payload.toString();
        this.groupIdentifier = payload.get("guid").getAsString();
    }

    public boolean deleteWaypoints()
    {
        return ServerGroup.deleteWaypoints(this.ownerUUID, this.groupIdentifier);
    }

    public static boolean deleteWaypoints(UUID ownerUUID, String groupIdentifier)
    {
        List<Path> objectList = JMWSServerIO.getLocalWaypointsFromGroup(ownerUUID, groupIdentifier);

        if (objectList == null) {
            return false;
        }

        List<Boolean> successArray = new ArrayList<>();

        for (Path objPath : objectList) {
            successArray.add(ServerWaypoint.getFromPath(objPath, ownerUUID).delete(true));
        }

        return successArray.isEmpty() || successArray.stream().allMatch(successArray.getFirst()::equals);
    }

    @Override
    public ObjectType getObjectType()
    {
        return objectType;
    }
}
