package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.server.exceptions.ObjectError;
import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
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

    public static class Utils
    {
        @Nullable
        public static Path getNewObjectFilename(@Nullable UUID playerOwner, String objectID, FetchType objectType) {
            try
            {
                return Path.of(getPathLocationPrefix(objectType) + objectID + "#" + playerOwner + ".json");
            } catch (InvalidPathException oldVersion)
            {
                Constants.getLogger().error("Client has older version than server expected. %s".formatted(playerOwner));
                return null;
            }
        }
        public static UUID getUUIDFromPath(Path path)
        {
            String pathString = path.toString();
            String uuidString = pathString.substring(pathString.indexOf("#") + 1, pathString.length() - 5);

            try
            {
                return UUID.fromString(uuidString);
            } catch (IllegalArgumentException err)
            {
                throw new ObjectError("UUID is malformed. UUID: %s From String: %s".formatted(uuidString, pathString));
            }
        }
        /*
        public static SavedObject getObject(String objectIdentifier, UUID ownerUUID, FetchType objectType)
        {
            return ownerUUID == null ? JMWSServerIO.getWaypointFromFile(objectIdentifier, null)
                    : JMWSServerIO.getWaypointFromFile(objectIdentifier, ownerUUID);
        }*/
    }

    public static Boolean removeAllWaypointsFromGroup(UUID playerUUID, String groupID) {
        List<Path> objectList = getLocalWaypointsFromGroup(playerUUID, groupID);

        if (objectList == null) {
            return false;
        }

        List<Boolean> successArray = new ArrayList<>();

        for (Path objPath : objectList) {
            successArray.add(CommonHelper.deleteFile(objPath));
        }
        return successArray.isEmpty() || successArray.stream().allMatch(successArray.getFirst()::equals);
    }

    public static String getPathLocationPrefix(FetchType objectType)
    {
        switch (objectType) {
            case SHARED -> {
                return "./jmws/users/";
            }
            case GROUP -> {
                return "./jmws/groups/";
            }
            case WAYPOINT -> {
                return "./jmws/";
            }
            default -> {
                throw new RuntimeException("Unrecognised FetchType %s".formatted(objectType));
            }
        }
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

    public static boolean deleteObject(String objectIdentifier, UUID player, FetchType deletionType)
    {
        return CommonHelper.deleteFile(Utils.getNewObjectFilename(player, objectIdentifier, deletionType));
    }

    public static boolean deleteAllUserObjects(UUID playerUUID, FetchType fetchType) {
        List<Boolean> deletionStatusList = new ArrayList<>();

        for (Path waypointPath : getObjectPathsForUser(playerUUID, fetchType)) {
            deletionStatusList.add(CommonHelper.deleteFile(waypointPath));
        }

        return deletionStatusList.isEmpty() || deletionStatusList.stream().allMatch(deletionStatusList.getFirst()::equals);

    }

    public static Stream<Path> getAllObjects(FetchType fetchType) throws IOException
    {
        String pathSearch = getPathLocationPrefix(fetchType);
        return Files.list(Path.of(pathSearch));
    }

    public static List<Path> getObjectPathsForUser(UUID uuid, FetchType fetchType) {

        List<Path> waypointFileList = new ArrayList<>();
        String pathSearch = getPathLocationPrefix(fetchType);

        try (Stream<Path> files = Files.list(Path.of(pathSearch))) {
            files.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(uuid.toString())) {
                    waypointFileList.add(path);
                }
            });
        } catch (IOException err) {
            return List.of();
            }
        return waypointFileList;
    }

    public static <T extends ServerObject> List<T> getObjectsForUser(UUID user, FetchType objectType)
    {
        List<T> list = new ArrayList<>();

        for (Path objPath : getObjectPathsForUser(user, objectType))
        {
            list.add((T) getWaypointFromFile(objPath, user));
        }


        return list;
    }

    public static HashMap<String, Path> getNameHashmapLookup(UUID user, FetchType fetchType)
    {
        HashMap<String, Path> map = new HashMap<>();
        for (ServerObject obj : getObjectsForUser(user, fetchType))
        {
            map.put(obj.getName(), obj.getObjectPath());
        }

        return map;
    }

    public static List<Path> getLocalWaypointsFromGroup(UUID playerUUID, String groupID) { // note; should switch to database for this shit
        List<Path> userWaypointFilepaths = getObjectPathsForUser(playerUUID, FetchType.WAYPOINT);
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
        } catch (IOException ioException)
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
    public static <T extends ServerObject> T getObjectFromDisk(String objectIdentifier, UUID ownerUUID, Class<T> objectClass, FetchType objectType) {
        try {
            Path objPath = getObjectPathFromUniqueIdentifier(objectIdentifier, objectType);
            @Nullable JsonObject data = getObjectDataFromDisk(objPath, false);
            if (data != null && objPath != null)
            {
                Constructor<T> constructor = objectClass.getConstructor(JsonObject.class, UUID.class);
                return constructor.newInstance(data, JMWSServerIO.Utils.getUUIDFromPath(objPath));
            } else {
                return null;
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException initExc)
        {
            throw new RuntimeException("Cannot pass %s to getObjectFromDisk TODO");
        }
    }

    @Nullable
    public static Path getObjectPathFromUniqueIdentifier(String identifier, FetchType fetchType)
    {
        try {
            for (Path objectPath : getAllObjects(fetchType).toList())
            {
                if (objectPath.toString().contains(identifier))
                {
                    return objectPath;
                }
            }
        } catch (IOException ignored)
        {
            return null;
        }
        return null;
    }

    @Nullable
    public static String getObjectFromUniqueIdentifier(String identifier, UUID playerUUID, FetchType fetchType)
    {
        Path objectPath = getObjectPathFromUniqueIdentifier(identifier, fetchType);
        if (objectPath != null)
        {
            return readRaw(objectPath, false);
        }
        return null;
    }

    public static boolean transition(Path path, FetchType transitionType, UUID player) throws FileNotFoundException {
        ServerObject serverObject = transitionType.equals(FetchType.WAYPOINT) ? JMWSServerIO.getWaypointFromFile(path, player) : JMWSServerIO.getGroupFromFile(path, player);

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
    private static ServerObject getGroupFromFile(Path path, UUID player)
    {
        JsonObject groupLocalServerData = getObjectDataFromDisk(path, false);
        if (groupLocalServerData != null)
        {
            return new ServerGroup(groupLocalServerData, player);
        }
        return null;
    }

    @Nullable
    public static ServerWaypoint getWaypointFromFile(String waypointIdentifier, UUID playerUUID)
    {
        Path waypointFilename = Utils.getNewObjectFilename(playerUUID, waypointIdentifier, FetchType.WAYPOINT);
        if (waypointFilename != null)
        {
            JsonObject waypointLocalData = getObjectDataFromDisk(waypointFilename, false);
            if (waypointLocalData != null) {
                return new ServerWaypoint(waypointLocalData, playerUUID);
            }
        }
        return null;
    }

    @Nullable
    public static ServerWaypoint getWaypointFromFile(Path waypointPath, UUID playerUUID)
    {
        JsonObject waypointLocalData = getObjectDataFromDisk(waypointPath, false);
        if (waypointLocalData != null) {
            return new ServerWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }

    @Nullable
    public static ServerWaypoint getWaypointFromUniqueIdentifier(String waypointIdentifier, UUID user)
    {
        Path objPath = getObjectPathFromUniqueIdentifier(waypointIdentifier, FetchType.WAYPOINT);
        return getWaypointFromFile(objPath, user);
    }
}

