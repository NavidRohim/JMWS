package me.brynview.navidrohim.jmws.common.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommonUtils {
    // This is kinda just a "put whatever here that is used everywhere" class
    public static final String unknownUser = "Unknown Soldier";
    public static final String globalStringTag = "G";
    private static final String[] checkForCustomDataKeys = {
            "objectIdentifier",
            "sharedTo",
            "owner",
            "isGlobal"
    };

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

    public static boolean isLegacyDataField(@Nullable String field) {
        if (field != null && field.length() == 64)
        {
            for (int i = 0; i < field.length(); i++) {
                char c = field.charAt(i);
                if (!Character.isLetterOrDigit(c))
                    return false;
            }

            return true;
        }
        return false;
    }

    public static JsonObject parseStringToJsonObject(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    public static boolean isValidCustomDataField(@Nullable String input) {

        if (input == null) { return true; }
        return Arrays.stream(checkForCustomDataKeys).allMatch(input::contains);
    }
}
