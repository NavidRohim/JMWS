package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.io.CommonIO;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static me.brynview.navidrohim.jmws.io.CommonIO._getWaypointFromRaw;

public class JMWSServerIO {

    public enum FetchType {
        WAYPOINT,
        GROUP
    }

    @Nullable
    public static Boolean removeAllWaypointsFromGroup(UUID playerUUID, String groupID) {
        List<String> objectList = getLocalWaypointsFromGroup(playerUUID, groupID);

        if (objectList == null) {
            return null;
        }

        List<Boolean> successArray = new ArrayList<>();

        for (String objPath : objectList) {
            successArray.add(CommonIO.deleteFile(objPath));
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
            Constants.LOGGER.warn("`jmws` folder was not found so another was made. All server waypoints and groups have been wiped. (group error)");
            return createGroup(jsonObject, playerUUID);

        } catch (FileSystemException missingPerms) {
            Constants.LOGGER.error("JMWS is missing write permissions to \"jmws\" folder. (group error)");
            return false;

        } catch (IOException genericIOError) {
            Constants.LOGGER.error("Got exception trying to make group -> " + genericIOError);
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
            //JMWSServer._createServerResources();
            Constants.LOGGER.warn("`jmws` folder was not found so another was made. All server waypoints and groups have been wiped. (waypoint error)");
            return createWaypoint(jsonObject, playerUUID);

        } catch (FileSystemException missingPerms) {
            Constants.LOGGER.error("JMWS is missing write permissions to \"jmws\" folder. (waypoint error)");
            return false;

        } catch (IOException genericIOError) {
            Constants.LOGGER.error("Got exception trying to make waypoint -> " + genericIOError);
            return false;
        }
    }

    public static boolean deleteAllUserObjects(UUID playerUUID, FetchType fetchType) {
        List<Boolean> deletionStatusList = new ArrayList<>();

        for (String waypointPath : getFileObjects(playerUUID, fetchType)) {
            deletionStatusList.add(CommonIO.deleteFile(waypointPath));
        }

        return deletionStatusList.isEmpty() || deletionStatusList.stream().allMatch(deletionStatusList.getFirst()::equals);

    }

    public static List<String> getFileObjects(UUID uuid, FetchType fetchType) {

        List<String> waypointFileList = new ArrayList<>();
        String pathSearch;

        if (fetchType == FetchType.GROUP) {
            pathSearch = "./jmws/groups";
        } else {
            pathSearch = "./jmws";
        }


        try (Stream<Path> files = Files.list(Path.of(pathSearch))) {
            files.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(uuid.toString())) {
                    waypointFileList.add(path.toString());
                }
            });
        } catch (IOException err) {
            return List.of();
            }
        return waypointFileList;
    }

    public static List<String> getLocalWaypointsFromGroup(UUID playerUUID, String groupID) { // note; should switch to database for this shit
        List<String> userWaypointFilepaths = getFileObjects(playerUUID, FetchType.WAYPOINT);
        List<String> groupWaypoints = new ArrayList<>();

        for (String waypointPath : userWaypointFilepaths) {
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
    public static JsonObject getObjectDataFromDisk(String objPath) {
        try {
            return JsonParser.parseString(Files.readString(Path.of(objPath))).getAsJsonObject();
        } catch (IOException ioException) {
            Constants.LOGGER.error("Error retrieving saved object data -> " + ioException);
        }
        return null;
    }

    @Nullable
    private static SavedWaypoint getWaypointFromFile(String waypointPath, UUID playerUUID) {
        JsonObject waypointLocalData = getObjectDataFromDisk(waypointPath);
        if (waypointLocalData != null) {
            return new SavedWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }
}

