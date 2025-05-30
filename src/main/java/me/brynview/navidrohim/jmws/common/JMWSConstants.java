package me.brynview.navidrohim.jmws.common;

import java.util.List;

public interface JMWSConstants {
    List<String> forbiddenGroups = List.of("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default");
    public static final String JourneyMapVersionString = "1.21.1-6.0.0-beta.47";
}
