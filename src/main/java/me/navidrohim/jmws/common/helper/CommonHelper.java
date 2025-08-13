package me.navidrohim.jmws.common.helper;

import com.google.common.collect.Iterables;
import journeymap.client.model.Waypoint;
import net.minecraft.client.resources.I18n;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class CommonHelper {
    // This is kinda just a "put whatever here that is used everywhere" class

    public static String getTranslatableComponent(String key, Object... args)
    {
        return I18n.format(key, args);
    }

    public static String getWaypointFilename(Waypoint waypoint, UUID uuID) {
        int primaryDim = Iterables.get(waypoint.getDimensions(), 0);
        return _getWaypointFromRaw(primaryDim, waypoint.getY(), waypoint.getName(), uuID);
    }

    public static String _getWaypointFromRaw(int waypointDim, int yLevel, String waypointName, UUID playerUUID) {
        return "./jmws/" +
                waypointDim +
                yLevel +
                waypointName.replace(":", "-") +
                "_" +
                playerUUID +
                ".json";
    }

    public static boolean deleteFile(String filename) {
        File waypointFileObj = new File(filename);
        return waypointFileObj.delete();
    }

    public static String getLanguageKeyAsString(String key)
    {
        return getTranslatableComponent(key).toString();
    }

    public static String makeWaypointHash(UUID playerUUID, String waypointGUID, String objectName)
    {
        return DigestUtils.sha256Hex(playerUUID.toString() + waypointGUID + objectName);
    }

    public static String readFromFile(String file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}
