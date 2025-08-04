package me.brynview.navidrohim.jmws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Constants {

    public static final String MODID = "jmws";
    public static final String MOD_NAME = "JMWS";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final List<String> forbiddenGroups = List.of("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default");
    public static final String JourneyMapVersionString = "1.21.7-6.0.0-beta.53";
}
