package me.navidrohim.jmws;

import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public static final String MODID = JMWS.MODID;
    public static final String MOD_NAME = JMWS.NAME;
    public static final String VERSION = JMWS.VERSION;
    public static final Logger LOGGER = JMWS.logger;

    public static final List<String> forbiddenGroups = Collections.unmodifiableList(
            Arrays.asList("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default")
    );
    public static final String JourneyMapVersionString = "1.21.7-6.0.0-beta.53";
}
