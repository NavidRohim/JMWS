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
    public static final String MOD_NAME = "JMWS";
    public static final String VERSION = "1.1.4-1.21.8";

    public static final List<String> forbiddenGroups = List.of("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default");
    public static final String JourneyMapVersionString = "1.21.7-6.0.0-beta.53";
}
