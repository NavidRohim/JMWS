package me.brynview.navidrohim.jmws.common.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class CommonHelper {
    public static String makeWaypointHash(UUID playerUUID, Integer x, Integer y, Integer z)
    {
        return DigestUtils.sha256Hex(playerUUID.toString() + x.toString() + y.toString() + z.toString());
    }

}
