package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static me.brynview.navidrohim.jmws.common.helper.CommonHelper._getWaypointFromRaw;

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

    public static String getGroupFilename(UUID playerUUID, String universalID) {
        return "./jmws/groups/" + universalID + "_" + playerUUID + "-group" + ".json";
    }
    public static boolean createGroup(JsonObject jsonObject, UUID playerUUID)
    {
        String universalID = jsonObject.get("customData").getAsString();
        try
        {
            String pathString = getGroupFilename(playerUUID, universalID);
            Path groupPathObj = Paths.get(pathString);
            Files.createFile(groupPathObj);

            FileWriter waypointFileWriter = new FileWriter(pathString);
            waypointFileWriter.write(jsonObject.toString());
            waypointFileWriter.close();

            return true;

        } catch (NoSuchFileException noSuchFileException) {
            //JMWSServer._createServerResources();
            Constants.getLogger().warn("`jmws` folder was not found so another was made. All server waypoints and groups have been wiped. (group error)");
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
        JsonObject pos = jsonObject.getAsJsonObject().getAsJsonObject("pos");
        String waypointFilePath = _getWaypointFromRaw(new Vector3d(
                pos.get("x").getAsInt(),
                pos.get("y").getAsInt(),
                pos.get("z").getAsInt()
                ),
                jsonObject.get("name").getAsString(),
                playerUUID

        );
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

    public static boolean deleteAllUserObjects(UUID playerUUID, FetchType fetchType) {
        List<Boolean> deletionStatusList = new ArrayList<>();

        for (Path waypointPath : getObjectsForUser(playerUUID, fetchType)) {
            deletionStatusList.add(CommonHelper.deleteFile(waypointPath.toString()));
        }

        return deletionStatusList.isEmpty() || deletionStatusList.stream().allMatch(deletionStatusList.getFirst()::equals);

    }

    public static Stream<Path> getAllObjects(FetchType fetchType) throws IOException
    {
        List<String> waypointFileList = new ArrayList<>();
        String pathSearch = fetchType == FetchType.WAYPOINT ? "./jmws" : "./jmws/groups";
        return Files.list(Path.of(pathSearch));
    }
    public static List<Path> getObjectsForUser(UUID uuid, FetchType fetchType) {

        List<Path> waypointFileList = new ArrayList<>();
        String pathSearch = fetchType == FetchType.WAYPOINT ? "./jmws" : "./jmws/groups";

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
    public static String readRaw(Path objPath)
    {
        try {
            return Files.readString(objPath);
        } catch (IOException ioException)
        {
            Constants.getLogger().error("Error retrieving saved object data -> " + ioException);
        }
        return null;
    }
    @Nullable
    public static JsonObject getObjectDataFromDisk(Path objPath) {
        String data = readRaw(objPath);
        return data != null ? JsonParser.parseString(data).getAsJsonObject() : null;


    }


    @Nullable
    public static SavedWaypoint getWaypointFromFile(Path waypointPath, UUID playerUUID) {
        JsonObject waypointLocalData = getObjectDataFromDisk(waypointPath);
        if (waypointLocalData != null) {
            return new SavedWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }

    @Nullable
    public static String getWaypointFromUniqueIdentifier(String identifier, UUID playerUUID)
    {
        try {
            for (Path allWaypoints : getAllObjects(FetchType.WAYPOINT).toList())
            {
                if (allWaypoints.toString().contains(identifier))
                {
                    return readRaw(allWaypoints);
                }
            }
        } catch (IOException ignored)
        {
            return null;
        }
        return null;
    }
}

