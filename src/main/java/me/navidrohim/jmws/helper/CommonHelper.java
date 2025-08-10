package me.navidrohim.jmws.helper;

import com.google.common.collect.Iterables;
import journeymap.client.model.Waypoint;
import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.Constants;
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
        int primaryDim = Iterables.get(waypoint.getDimensions(), 0);/*
        Vector3d waypointLocationVector;
        Constants.LOGGER.info(waypoint.getX());
        Constants.LOGGER.info(waypoint.getY());
        Constants.LOGGER.info(waypoint.getZ());
        if (primaryDim == -1)
        {
            waypointLocationVector = new Vector3d(waypoint.getX() * 8, waypoint.getY(), waypoint.getZ() * 8);
        } else if (CommonClass.minecraftClientInstance.player.dimension == -1) {
            waypointLocationVector = new Vector3d((double) waypoint.getX() / 8, waypoint.getY(), (double) waypoint.getZ() / 8);
        } else {
            waypointLocationVector = new Vector3d(waypoint.getX(), waypoint.getY(), waypoint.getZ());
        }

        Constants.LOGGER.info(waypointLocationVector);*/
        //Vector3d waypointLocationVector = primaryDim == -1 ? new Vector3d(waypoint.getX() * 8, waypoint.getY(), waypoint.getZ() * 8) : new Vector3d(waypoint.getX(), waypoint.getY(), waypoint.getZ());
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
        Constants.LOGGER.info("delserver " + filename);
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
