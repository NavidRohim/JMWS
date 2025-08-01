package me.brynview.navidrohim.jmws.common;

import java.util.List;

public interface JMWSConstants {
    List<String> forbiddenGroups = List.of("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default");
    String JourneyMapVersionString = "1.20.1-5.10.3";
}
