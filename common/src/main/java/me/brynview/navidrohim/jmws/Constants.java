package me.brynview.navidrohim.jmws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Constants {

    private static class LoggerHolder {
        private static final Logger INSTANCE = LoggerFactory.getLogger(MODID);
    }

    public static Logger getLogger() {
        return LoggerHolder.INSTANCE;
    }

    public static final String MODID = "jmws";
    public static final String VERSION = "1.2.1-1.21.1"; // This is purely for display and is not needed
    public static final double SERVER_VERSION = 1.101;
    public static final boolean DEBUG = VERSION.contains("-beta.");

    public static final List<String> forgeModLoaders = List.of("Forge", "NeoForge"); // Do not edit unless there is another fork of Forge (would not be surprised)
    public static final List<String> forbiddenGroups = List.of("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default"); // (DO NOT EDIT, game will bug out)

    // For anyone forking, this MUST be updated if there is a change that is in a future version of JM that you use.
    public static final String JourneyMapVersionString = "1.21.1-6.0.0-beta.52";
    // beta 52 fixed the waypoint-drag-drop event, so this is the only version compatible (and any newer)
}
