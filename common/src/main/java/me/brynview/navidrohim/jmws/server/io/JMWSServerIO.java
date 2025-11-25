package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.server.exceptions.ObjectError;
import me.brynview.navidrohim.jmws.server.objects.LegacyObject;
import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class JMWSServerIO {

    public static final String globalObjPrefix = "GLOBAL_";

    public static class Utils
    {
        public static String makeFilename(String objectID, UUID playerOwner)
        {
            return "%s#%s.json".formatted(objectID, playerOwner);
        }

        @Nullable
        public static Path getNewObjectFilename(@Nullable UUID playerOwner, String objectID, ObjectType objectType) {
            try
            {
                return Path.of(getPathLocationPrefix(objectType) + makeFilename(objectID, playerOwner));
            } catch (InvalidPathException oldVersion)
            {
                Constants.getLogger().error("Client has older version than server expected. %s".formatted(playerOwner));
                return null;
            }
        }
        public static UUID getUUIDFromPath(Path path, ObjectType transitionType)
        {
            String pathString = path.toString();
            String uuidString = pathString.substring(pathString.indexOf("#") + 1, pathString.length() - 5);

            try
            {
                return UUID.fromString(uuidString);
            } catch (IllegalArgumentException | IndexOutOfBoundsException err)
            {
                try {
                    int uuidEnd = !pathString.contains("group") ? 5 : 11;

                    UUID uuidFromLegacy = UUID.fromString(pathString.substring(pathString.lastIndexOf("_") + 1, pathString.length() - uuidEnd));
                    LegacyObject.transitionIfNeed(path, uuidFromLegacy, transitionType);

                    return uuidFromLegacy;
                } catch (IllegalArgumentException | IndexOutOfBoundsException err2)
                {
                    throw new ObjectError("UUID is malformed. UUID: %s From String: %s".formatted(uuidString, pathString));
                }
            }
        }
    }

    public static String getPathLocationPrefix(ObjectType objectType)
    {
        return objectType.getObjectPathPrefix();
    }

    public static boolean createGroup(JsonObject jsonObject, UUID playerUUID)
    {
        ServerGroup gp = new ServerGroup(jsonObject, playerUUID);
        return gp.create();
    }

    public static boolean createWaypoint(JsonObject jsonObject, UUID playerUUID)
    {
        ServerWaypoint wp = new ServerWaypoint(jsonObject, playerUUID);
        return wp.create();
    }

    public static Stream<Path> getAllObjects(ObjectType objectType)
    {
        String pathSearch = getPathLocationPrefix(objectType);
        try {
            return Files.list(Path.of(pathSearch));
        } catch (SecurityException e)
        {
            Constants.getLogger().error("FATAL: Missing permissions! cannot read from %s".formatted(pathSearch));
        } catch (IOException ignored) {}
        return Stream.of();
    }

    public static List<Path> getObjectPathsForUser(UUID uuid, ObjectType objectType, boolean global) {

        List<Path> waypointFileList = new ArrayList<>();
        String pathSearch = getPathLocationPrefix(objectType);
        String globalPrefix = global ? globalObjPrefix : "";

        try (Stream<Path> files = Files.list(Path.of(pathSearch))) {
                files.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(uuid.toString()) && path.toString().contains(globalPrefix)) {
                    waypointFileList.add(path);
                }
            });
        } catch (IOException err) {
            Constants.getLogger().error("Got error trying to get user objects: %s".formatted(err));
            return List.of();
            }
        return waypointFileList;
    }

    public static List<Path> getObjectPathsForUser(UUID uuid, ObjectType objectType) {
        return getObjectPathsForUser(uuid, objectType, false);
    }

    public static <T extends ServerObject> List<T> getObjectsForUser(UUID user, ObjectType objectType, boolean global)
    {
        List<T> list = new ArrayList<>();

        for (Path objPath : getObjectPathsForUser(user, objectType, global))
        {
            list.add((T) getObjectFromFile(objPath, user, objectType));
        }


        return list;
    }

    public static <T extends ServerObject> T getObjectFromFile(Path objPath, UUID user, ObjectType objectType)
    {
        try {
            @Nullable JsonObject data = getObjectDataFromDisk(objPath, false);
            if (data != null && objPath != null)
            {
                Constructor<? extends ServerObject> constructor = objectType.getObjectClass().getConstructor(JsonObject.class, UUID.class);
                return (T) constructor.newInstance(data, Utils.getUUIDFromPath(objPath, objectType));
            } else {
                return null;
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException initExc)
        {
            throw new RuntimeException("Cannot pass %s to getObjectFromDisk TODO");
        }
    }

    public static HashMap<String, Path> getNameHashmapLookup(UUID user, ObjectType objectType)
    {
        HashMap<String, Path> map = new HashMap<>();
        for (ServerObject obj : getObjectsForUser(user, objectType, false))
        {
            map.put(obj.getObjectNonDuplicateIdentifier(), obj.getCurrentObjectPath());
        }

        return map;
    }

    public static List<Path> getLocalWaypointsFromGroup(UUID playerUUID, String groupID) { // note; should switch to database for this shit
        List<Path> userWaypointFilepaths = getObjectPathsForUser(playerUUID, ObjectType.WAYPOINT);
        List<Path> groupWaypoints = new ArrayList<>();

        for (Path waypointPath : userWaypointFilepaths) {
            ServerWaypoint serverWaypoint = getWaypointFromFile(waypointPath, playerUUID);
            if (serverWaypoint.getWaypointGroupId().equals(groupID)) {
                groupWaypoints.add(waypointPath);
            } else if (serverWaypoint == null) {
                return null;
            }
        }
        return groupWaypoints;
    }

    @Nullable
    public static String readRaw(Path objPath, boolean silentFail)
    {
        try {
            return Files.readString(objPath);
        } catch (IOException | NullPointerException ioException)
        {
            if (!silentFail)
            {
                Constants.getLogger().error("Error retrieving saved object data -> " + ioException);
            }
        }
        return null;
    }

    @Nullable
    public static JsonObject getObjectDataFromDisk(Path objPath, boolean silentFail) {
        String data = readRaw(objPath, silentFail);
        return data != null ? JsonParser.parseString(data).getAsJsonObject() : null;
    }

    @Nullable
    public static <T extends ServerObject> T getObjectFromDisk(String objectIdentifier, UUID ownerUUID, ObjectType objectType) {
        Path objPath = Utils.getNewObjectFilename(ownerUUID, objectIdentifier, objectType);
        Constants.getLogger().info(">> > "+objPath.toString());
        if (objPath != null)
        {
            return getObjectFromFile(objPath, ownerUUID, objectType);
        }
        return null;
    }

    @Nullable
    public static Path getObjectPathFromUniqueIdentifier(String identifier, ObjectType objectType)
    {
        for (Path objectPath : getAllObjects(objectType).toList())
        {
            if (objectPath.toString().contains(identifier))
            {
                return objectPath;
            }
        }
        return null;
    }

    @Nullable
    public static String getObjectFromUniqueIdentifier(String identifier, UUID playerUUID, ObjectType objectType)
    {
        Path objectPath = getObjectPathFromUniqueIdentifier(identifier, objectType);
        if (objectPath != null)
        {
            return readRaw(objectPath, false);
        }
        return null;
    }

    public static boolean transitionPath(Path path, ObjectType transitionType, UUID player) throws FileNotFoundException {
        ServerObject serverObject = transitionType.equals(ObjectType.WAYPOINT) ? JMWSServerIO.getWaypointFromFile(path, player) : JMWSServerIO. getGroupFromFile(path, player);

        if (serverObject != null)
        {
            String location = getPathLocationPrefix(transitionType);

            File oldNameFile = new File(path.toString());
            File newUUIDFile = new File(location + Utils.getNewObjectFilename(player, serverObject.syncing.objectIdentifier, transitionType));

            return oldNameFile.renameTo(newUUIDFile);
        } else {
            throw new FileNotFoundException("Could not find %s %s to transition.".formatted(transitionType.toString().toLowerCase(), path));
        }
    }

    @Nullable
    private static ServerGroup getGroupFromFile(Path path, UUID player)
    {
        JsonObject groupLocalServerData = getObjectDataFromDisk(path, false);
        if (groupLocalServerData != null)
        {
            return new ServerGroup(groupLocalServerData, player);
        }
        return null;
    }

    @Nullable
    public static ServerWaypoint getWaypointFromFile(Path waypointPath, UUID playerUUID)
    {
        JsonObject waypointLocalData = getObjectDataFromDisk(waypointPath, false);
        if (waypointLocalData != null) {
            return new  ServerWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }

    @Nullable
    public static ServerWaypoint getWaypointFromUniqueIdentifier(String waypointIdentifier, UUID user)
    {
        Path objPath = getObjectPathFromUniqueIdentifier(waypointIdentifier, ObjectType.WAYPOINT);
        return getWaypointFromFile(objPath, user);
    }

    @Nullable
    public static ServerGroup getGroupFromUniqueIdentifier(String groupIdentifier, UUID user)
    {
        return getGroupFromFile(getObjectPathFromUniqueIdentifier(groupIdentifier, ObjectType.GROUP), user);
    }
}

