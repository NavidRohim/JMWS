package me.brynview.navidrohim.jm_server.common.utils;

import com.google.gson.JsonObject;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jm_server.JMServerTest;
import me.brynview.navidrohim.jm_server.common.SavedWaypoint;
import org.apache.commons.io.FileExistsException;
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

public class WaypointIOInterface {

    public static String getWaypointFilename(Waypoint waypoint, UUID uuID) {
        Vector3d waypointLocationVector = new Vector3d(waypoint.getX(), waypoint.getY(), waypoint.getZ());
        return _getWaypointFromRaw(waypointLocationVector, waypoint.getName(), uuID);
    }

    private static String _getWaypointFromRaw(Vector3d coordVector, String waypointName, UUID playerUUID) {
        return "./jmserver/" +
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

    public static boolean createWaypoint(JsonObject jsonObject, UUID playerUUID) {
        JsonObject pos = jsonObject.getAsJsonObject().getAsJsonObject("pos");
        String waypointFilePath = WaypointIOInterface._getWaypointFromRaw(new Vector3d(
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

    public static boolean deleteWaypoint(String filename) {
        File waypointFileObj = new File(filename);
        return waypointFileObj.delete();
    }

    public static List<String> getPlayerWaypointNames(UUID uuid) {
        //String content = Files.readString(path); // Java 11+
        List<String> waypointFileList = new ArrayList<>();

        try (Stream<Path> files = Files.list(Path.of("./jmserver"))) {
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

