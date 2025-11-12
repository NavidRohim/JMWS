package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.objects.SavedGroup;
import me.brynview.navidrohim.jmws.common.objects.SavedObject;
import me.brynview.navidrohim.jmws.common.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class JMWSServerIO {

    public static class Utils
    {
        public static Path getNewObjectFilename(@Nullable UUID playerOwner, String objectID, FetchType objectType) {
            return Path.of(getPathLocationPrefix(objectType) + objectID + "#" + playerOwner + ".json");
        }

        public static SavedObject getObject(String objectIdentifier, UUID ownerUUID, FetchType objectType)
        {
            return ownerUUID == null ? JMWSServerIO.getWaypointFromFile(objectIdentifier, null)
                    : JMWSServerIO.getWaypointFromFile(objectIdentifier, ownerUUID);
        }
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
        SavedGroup gp = new SavedGroup(jsonObject, playerUUID);
        return gp.create();
    }

    public static boolean createWaypoint(JsonObject jsonObject, UUID playerUUID)
    {
        SavedWaypoint wp = new SavedWaypoint(jsonObject, playerUUID);
        return wp.create();
    }

    public static boolean deleteObject(String objectIdentifier, UUID player, FetchType deletionType)
    {
        return CommonHelper.deleteFile(Utils.getNewObjectFilename(player, objectIdentifier, deletionType));
    }

    public static boolean deleteAllUserObjects(UUID playerUUID, FetchType fetchType) {
        List<Boolean> deletionStatusList = new ArrayList<>();

        for (Path waypointPath : getObjectsForUser(playerUUID, fetchType)) {
            deletionStatusList.add(CommonHelper.deleteFile(waypointPath));
        }

        return deletionStatusList.isEmpty() || deletionStatusList.stream().allMatch(deletionStatusList.getFirst()::equals);

    }

    public static Stream<Path> getAllObjects(FetchType fetchType) throws IOException
    {
        String pathSearch = getPathLocationPrefix(fetchType);
        return Files.list(Path.of(pathSearch));
    }

    public static List<Path> getObjectsForUser(UUID uuid, FetchType fetchType) {

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

    public static List<Path> getLocalWaypointsFromGroup(UUID playerUUID, String groupID) { // note; should switch to database for this shit
        List<Path> userWaypointFilepaths = getObjectsForUser(playerUUID, FetchType.WAYPOINT);
        List<Path> groupWaypoints = new ArrayList<>();

        for (Path waypointPath : userWaypointFilepaths) {
            SavedWaypoint savedWaypoint = getWaypointFromFile(waypointPath, playerUUID);
            if (savedWaypoint.getWaypointGroupId().equals(groupID)) {
                groupWaypoints.add(waypointPath);
            } else if (savedWaypoint == null) {
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
    public static <T extends SavedObject> T getObjectFromDisk(String objectIdentifier, UUID ownerUUID, Class<T> objectClass, FetchType objectType) {
        try {
            @Nullable JsonObject data = getObjectDataFromDisk(Utils.getNewObjectFilename(ownerUUID, objectIdentifier, objectType), false);
            if (data != null)
            {
                Constructor<T> constructor = objectClass.getConstructor(JsonObject.class, UUID.class);
                return constructor.newInstance(data, ownerUUID);
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
        SavedObject savedObject = transitionType.equals(FetchType.WAYPOINT) ? JMWSServerIO.getWaypointFromFile(path, player) : JMWSServerIO.getGroupFromFile(path, player);

        if (savedObject != null)
        {
            String location = getPathLocationPrefix(transitionType);

            File oldNameFile = new File(path.toString());
            File newUUIDFile = new File(location + Utils.getNewObjectFilename(player, savedObject.syncing.objectIdentifier, transitionType));

            return oldNameFile.renameTo(newUUIDFile);
        } else {
            throw new FileNotFoundException("Could not find %s %s to transition.".formatted(transitionType.toString().toLowerCase(), path));
        }
    }

    @Nullable
    private static SavedObject getGroupFromFile(Path path, UUID player)
    {
        JsonObject groupLocalServerData = getObjectDataFromDisk(path, false);
        if (groupLocalServerData != null)
        {
            return new SavedGroup(groupLocalServerData, player);
        }
        return null;
    }

    @Nullable
    public static SavedWaypoint getWaypointFromFile(String waypointIdentifier, UUID playerUUID)
    {
        JsonObject waypointLocalData = getObjectDataFromDisk(Utils.getNewObjectFilename(playerUUID, waypointIdentifier, FetchType.WAYPOINT), false);
        if (waypointLocalData != null) {
            return new SavedWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }

    @Nullable
    public static SavedWaypoint getWaypointFromFile(Path waypointPath, UUID playerUUID)
    {
        JsonObject waypointLocalData = getObjectDataFromDisk(waypointPath, false);
        if (waypointLocalData != null) {
            return new SavedWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }
}

