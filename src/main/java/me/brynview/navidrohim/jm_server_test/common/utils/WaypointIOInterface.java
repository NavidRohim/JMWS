package me.brynview.navidrohim.jm_server_test.common.utils;

import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.common.SavedWaypoint;

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
    public static String getWaypointFilename(SavedWaypoint savedWaypoint) {
        return "./jmserver/" +
                savedWaypoint.getRawJsonData().get("x") +
                "_" +
                savedWaypoint.getRawJsonData().get("y") +
                "_" +
                savedWaypoint.getRawJsonData().get("z") +
                "_" +
                savedWaypoint.getWaypointName() +
                "_" +
                savedWaypoint.getPlayerUUID() +
                ".json";
    }

    public static String getWaypointFilename(WaypointEvent waypointEvent, UUID uuID) {
        Waypoint wp = waypointEvent.getWaypoint();
        return "./jmserver/" +
                wp.getX() +
                "_" +
                wp.getY() +
                "_" +
                wp.getZ() +
                "_" +
                wp.getName() +
                "_" +
                uuID.toString() +
                ".json";
    }

    public static void createWaypoint(SavedWaypoint savedWaypoint) {
        String waypointFilePath = WaypointIOInterface.getWaypointFilename(savedWaypoint);
        Path waypointPathObj = Paths.get(waypointFilePath);

        try {

            Files.createFile(waypointPathObj);
            FileWriter waypointFileWriter = new FileWriter(waypointFilePath);
            waypointFileWriter.write(savedWaypoint.getRawPacketData());
            waypointFileWriter.close();

        } catch (IOException e) {
            JMServerTest.LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static boolean deleteWaypoint(SavedWaypoint savedWaypoint) {
        File waypointFileObj = new File(WaypointIOInterface.getWaypointFilename(savedWaypoint));
        return waypointFileObj.delete();
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

