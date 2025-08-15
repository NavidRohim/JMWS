package me.navidrohim.jmws.client.mixinhelper;

import journeymap.client.model.Waypoint;

public class MixinManager {
    public static String recentlyRemovedDeathpointId = null;

    public static void clearDeathpointCache()
    {
        MixinManager.recentlyRemovedDeathpointId = null;
    }
}
