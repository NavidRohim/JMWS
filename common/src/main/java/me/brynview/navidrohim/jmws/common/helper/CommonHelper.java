package me.brynview.navidrohim.jmws.common.helper;

import journeymap.api.v2.common.waypoint.Waypoint;
import net.minecraft.network.chat.Component;
import org.joml.Vector3d;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommonHelper {
    // This is kinda just a "put whatever here that is used everywhere" class

    public static String getWaypointFilename(Waypoint waypoint, UUID uuID) {
        Vector3d waypointLocationVector = new Vector3d(waypoint.getBlockPos().getX(), waypoint.getBlockPos().getY(), waypoint.getBlockPos().getZ());
        return _getWaypointFromRaw(waypointLocationVector, waypoint.getName(), uuID);
    }

    public static String _getWaypointFromRaw(Vector3d coordVector, String waypointName, UUID playerUUID) {
        Set<Character> charsToRemove = new HashSet<>(Arrays.asList('<', '>', ':', '*', '"', '\\', '|', '?', '/'));

        String filename =
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

        return "./jmws/" + filename.chars() // IntStream of characters
                .mapToObj(c -> (char) c) // Convert int to Character
                .filter(c -> !charsToRemove.contains(c)) // Filter out unwanted characters
                .map(String::valueOf) // Convert Character to String
                .collect(Collectors.joining());
    }

    public static boolean deleteFile(Path filename) {
        File waypointFileObj = new File(filename.toUri());
        return waypointFileObj.delete();
    }

    public static boolean fileExists(Path filePath)
    {
        return new File(filePath.toUri()).exists();
    }
}
