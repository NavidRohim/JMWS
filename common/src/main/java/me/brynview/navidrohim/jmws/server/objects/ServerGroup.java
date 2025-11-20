package me.brynview.navidrohim.jmws.server.objects;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
        this.groupIdentifier = payload.get("guid").getAsString();
    }

    public static List<Path> getGlobalGroups() {
        List<Path> gp = new ArrayList<>();
        for (Path path : JMWSServerIO.getAllObjects(ObjectType.GROUP).toList())
        {
            if (path.toString().contains("SERVER"))
            {
                gp.add(path);
            }
        }
        return gp;
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

    private void setLocked(boolean locked)
    {
        this.getRawJson().get("settings").getAsJsonObject().add("locked", new JsonPrimitive(locked));
        this.update(this.getRawJson().toString(), true);
    }

    private boolean getLocked()
    {
        return this.getRawJson().get("settings").getAsJsonObject().get("locked").getAsBoolean();
    }

    @Override
    public void makeGlobal()
    {
        super.makeGlobal();
        this.setLocked(true);
    }

    @Override
    public ObjectType getObjectType()
    {
        return objectType;
    }
}
