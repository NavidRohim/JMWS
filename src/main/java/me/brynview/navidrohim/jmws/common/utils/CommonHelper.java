package me.brynview.navidrohim.jmws.common.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class CommonHelper {
    public static String makeWaypointHash(UUID playerUUID, String waypointGUID, String objectName)
    {
        return DigestUtils.sha256Hex(playerUUID.toString() + waypointGUID + objectName);
    }

}
