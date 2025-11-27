package me.brynview.navidrohim.jmws.server.objects;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import org.jetbrains.annotations.Nullable;

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
    }

    public boolean deleteWaypoints()
    {
        return ServerGroup.deleteWaypoints(this.ownerUUID, this.groupIdentifier);
    }

    public static boolean deleteWaypoints(UUID ownerUUID, String groupIdentifier)
    {
        List<Path> objectList = getLocalWaypointsFromGroup(ownerUUID, groupIdentifier);

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

    public String getDifferentiator()
    {
        return getGroupIdentifier().substring(0, 5);
    }

    @Override
    public void makeGlobal()
    {
        super.makeGlobal();
        this.setLocked(true);
    }

    @Override
    public void removeGlobal()
    {
        super.removeGlobal();
        this.setLocked(false);
    }

    @Override
    public ObjectType getObjectType()
    {
        return objectType;
    }

    private static List<Path> getLocalWaypointsFromGroup(UUID playerUUID, String groupID) { // note; should switch to database for this shit
        List<Path> userWaypointFilepaths = JMWSServerIO.getObjectPathsForUser(playerUUID, ObjectType.WAYPOINT);
        List<Path> groupWaypoints = new ArrayList<>();

        for (Path waypointPath : userWaypointFilepaths) {
            ServerWaypoint serverWaypoint = ServerWaypoint.getWaypointFromFile(waypointPath, playerUUID);
            if (serverWaypoint.getWaypointGroupId().equals(groupID)) {
                groupWaypoints.add(waypointPath);
            } else if (serverWaypoint == null) {
                return null;
            }
        }
        return groupWaypoints;
    }

    // Following static methods are ways to get instances of ServerGroup from files

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

    public static boolean createGroup(JsonObject jsonObject, UUID playerUUID)
    {
        ServerGroup gp = new ServerGroup(jsonObject, playerUUID);
        return gp.create();
    }

    @Nullable
    public static ServerGroup getGroup(UUID player, String objectUUID, boolean isGlobal)
    {
        return getGroupFromFile(JMWSServerIO.PathUtils.getObjectFilename(player, objectUUID, ObjectType.GROUP, isGlobal), player);
    }

    @Nullable
    private static ServerGroup getGroupFromFile(Path path, UUID player)
    {
        JsonObject groupLocalServerData = JMWSServerIO.getObjectDataFromDisk(path, false);
        if (groupLocalServerData != null)
        {
            return new ServerGroup(groupLocalServerData, player);
        }
        return null;
    }

    @Nullable
    public static ServerGroup getGroupFromUniqueIdentifier(String groupIdentifier, UUID user)
    {
        return getGroupFromFile(JMWSServerIO.getObjectPathFromUniqueIdentifier(groupIdentifier, ObjectType.GROUP), user);
    }
}
