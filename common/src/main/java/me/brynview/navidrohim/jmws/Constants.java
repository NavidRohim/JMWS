package me.brynview.navidrohim.jmws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {

    private static class LoggerHolder {
        private static final Logger INSTANCE = LoggerFactory.getLogger(MODID);
    }

    public static Logger getLogger() {
        return LoggerHolder.INSTANCE;
    }

    public static final String MODID = "jmws";
    public static final String VERSION = "1.2.6-26.1.x"; // This is purely for display and is not needed
    public static final double SERVER_VERSION = 1.12;
    public static final boolean DEBUG = VERSION.contains("-beta.");
    public static boolean shouldMakeLocal = false;
    public static boolean serverHasCompatibleJourneyMap = false;
    public static final int JOURNEYMAP_LOCAL_SERVER_WAYPOINTS_BETA = 71;

    public static final List<String> allowedMods = List.of(MODID, "journeymap"); // DO NOT CHANGE!
    public static final List<String> forgeModLoaders = List.of("Forge", "NeoForge"); // Do not edit unless there is another fork of Forge (would not be surprised)
    public static final List<String> forbiddenGroups = List.of("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default"); // (DO NOT EDIT, game will bug out)

    // For anyone forking, this MUST be updated if there is a change that is in a future version of JM that you use.
    public static final String JourneyMapVersionString = "26.1-6.0.0-beta.61";
    // beta 52 fixed the waypoint-drag-drop event, so this is the only version compatible (and any newer)

    public static void updateShouldMakeLocalFromJourneyMapVersion(String versionString) {
        shouldMakeLocal = false;
        Matcher betaVersionMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(versionString);
        if (betaVersionMatcher.find()) {
            int betaVersion = Integer.parseInt(betaVersionMatcher.group(1));
            shouldMakeLocal = betaVersion >= JOURNEYMAP_LOCAL_SERVER_WAYPOINTS_BETA;
        }
    }

    public static void updateServerJourneyMapStatus(String versionString) {
        serverHasCompatibleJourneyMap = false;
        Constants.getLogger().info("JourneyMap is installed on the server. Version: %s".formatted(versionString));
        Matcher betaVersionMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(versionString);
        if (betaVersionMatcher.find()) {
            int betaVersion = Integer.parseInt(betaVersionMatcher.group(1));
            serverHasCompatibleJourneyMap = betaVersion >= JOURNEYMAP_LOCAL_SERVER_WAYPOINTS_BETA;
        }
    }

    public static void logServerJourneyMapMissing() {
        serverHasCompatibleJourneyMap = false;
        Constants.getLogger().info("JourneyMap is not installed on the server.");
    }
}
