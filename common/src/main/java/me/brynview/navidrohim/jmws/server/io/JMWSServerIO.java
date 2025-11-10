package me.brynview.navidrohim.jmws.server.io;

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
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class JMWSServerIO {

    public static Boolean removeAllWaypointsFromGroup(UUID playerUUID, String groupID) {
        List<Path> objectList = getLocalWaypointsFromGroup(playerUUID, groupID);

        if (objectList == null) {
            return false;
        }

        List<Boolean> successArray = new ArrayList<>();

        for (Path objPath : objectList) {
            successArray.add(CommonHelper.deleteFile(objPath.toString()));
        }
        return successArray.isEmpty() || successArray.stream().allMatch(successArray.getFirst()::equals);
    }

    public static String getPathLocationPrefix(FetchType objectType)
    {
        switch (objectType) {
            case SHARED -> {
                return "./jmws/share/";
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

    public static String getNewObjectFilename(@Nullable UUID playerOwner, String objectID, FetchType objectType) {
        return getPathLocationPrefix(objectType) + objectID + "#" + playerOwner + ".json";
    }

    public static boolean createGroup(JsonObject jsonObject, UUID playerUUID)
    {
        String universalID = jsonObject.get("customData").getAsString();
        try
        {
            String pathString = getNewObjectFilename(playerUUID, universalID, FetchType.GROUP);
            Path groupPathObj = Paths.get(pathString);
            Files.createFile(groupPathObj);

            FileWriter waypointFileWriter = new FileWriter(pathString);
            waypointFileWriter.write(jsonObject.toString());
            waypointFileWriter.close();

            return true;

        } catch (NoSuchFileException noSuchFileException) {
            CommonClass._createServerResources();
            Constants.getLogger().warn("`jmws` folder was not found so another was made. All server waypoints and groups have been wiped. (group error)" + noSuchFileException.toString());
            return createGroup(jsonObject, playerUUID);

        } catch (FileSystemException missingPerms) {
            Constants.getLogger().error("JMWS is missing write permissions to \"jmws\" folder. (group error)");
            return false;

        } catch (IOException genericIOError) {
            Constants.getLogger().error("Got exception trying to make group -> " + genericIOError);
            return false;
        }
    }

    public static boolean createWaypoint(JsonObject jsonObject, UUID playerUUID) {
        String waypointFilePath = getNewObjectFilename(playerUUID, jsonObject.get("customData").getAsString(), FetchType.WAYPOINT);

        try {

            Path waypointPathObj = Paths.get(waypointFilePath);

            Files.createFile(waypointPathObj);
            FileWriter waypointFileWriter = new FileWriter(waypointFilePath);
            waypointFileWriter.write(jsonObject.toString());
            waypointFileWriter.close();

            return true;

        } catch (NoSuchFileException noSuchFileException) {
            CommonClass._createServerResources();
            Constants.getLogger().warn("`jmws` folder was not found so another was made. All server waypoints and groups have been wiped. (waypoint error)");
            return createWaypoint(jsonObject, playerUUID);

        } catch (FileSystemException missingPerms) {
            Constants.getLogger().error("JMWS is missing write permissions to \"jmws\" folder. (waypoint error)");
            return false;

        } catch (IOException genericIOError) {
            Constants.getLogger().error("Got exception trying to make waypoint -> " + genericIOError);
            return false;
        }
    }

    public static boolean deleteObject(String objectIdentifier, UUID player, FetchType deletionType)
    {
        return CommonHelper.deleteFile(getNewObjectFilename(player, objectIdentifier, deletionType));
    }

    public static boolean deleteAllUserObjects(UUID playerUUID, FetchType fetchType) {
        List<Boolean> deletionStatusList = new ArrayList<>();

        for (Path waypointPath : getObjectsForUser(playerUUID, fetchType)) {
            deletionStatusList.add(CommonHelper.deleteFile(waypointPath.toString()));
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
            String location = transitionType.equals(FetchType.WAYPOINT) ? "./jmws/" : "./jmws/groups/";

            Constants.getLogger().info(savedObject.getUniversalIdentifier());
            File oldNameFile = new File(path.toString());
            File newUUIDFile = new File(location + getNewObjectFilename(player, savedObject.getUniversalIdentifier(), transitionType));

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
            return new SavedGroup(groupLocalServerData);
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

