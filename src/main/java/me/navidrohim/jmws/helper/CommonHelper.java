package me.navidrohim.jmws.helper;

import journeymap.client.api.display.Waypoint;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.codec.digest.DigestUtils;

import javax.vecmath.Vector3d;
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
        Vector3d waypointLocationVector = new Vector3d(waypoint.getPosition().getX(), waypoint.getPosition().getY(), waypoint.getPosition().getZ());
        return _getWaypointFromRaw(waypointLocationVector, waypoint.getName(), uuID);
    }

    public static String _getWaypointFromRaw(Vector3d coordVector, String waypointName, UUID playerUUID) {
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
