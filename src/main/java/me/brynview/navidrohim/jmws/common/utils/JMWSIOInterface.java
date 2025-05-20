package me.brynview.navidrohim.jmws.common.utils;

import com.google.gson.JsonObject;
import journeymap.api.v2.common.waypoint.Waypoint;
import org.joml.Vector3d;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class JMWSIOInterface {

    public enum FetchType {
        WAYPOINT,
        GROUP
    }

    public static String getWaypointFilename(Waypoint waypoint, UUID uuID) {
        Vector3d waypointLocationVector = new Vector3d(waypoint.getX(), waypoint.getY(), waypoint.getZ());
        return _getWaypointFromRaw(waypointLocationVector, waypoint.getName(), uuID);
    }

    private static String _getWaypointFromRaw(Vector3d coordVector, String waypointName, UUID playerUUID) {
        return "./jmws/" +
                coordVector.x +
                "_" +
                coordVector.y +
                "_" +
                coordVector.z +
                "_" +
                waypointName +
                "_" +
                playerUUID +
                ".json";
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
        } catch (IOException ioe) {
            return false;
        }
    }

    public static boolean createWaypoint(JsonObject jsonObject, UUID playerUUID) {
        JsonObject pos = jsonObject.getAsJsonObject().getAsJsonObject("pos");
        String waypointFilePath = JMWSIOInterface._getWaypointFromRaw(new Vector3d(
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
        } catch (IOException ioException) {
            return false;
        }
    }

    public static void deleteAllUserObjects(UUID playerUUID, FetchType fetchType) {
        for (String waypointPath : getFileObjects(playerUUID, fetchType)) {
            deleteFile(waypointPath);
        }

    }

    public static boolean deleteFile(String filename) {
        File waypointFileObj = new File(filename);
        return waypointFileObj.delete();
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
    };
}

