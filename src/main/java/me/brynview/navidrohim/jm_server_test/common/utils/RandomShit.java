package me.brynview.navidrohim.jm_server_test.common.utils;

import me.brynview.navidrohim.jm_server_test.JMServerTest;

import java.util.Map;
import java.util.Set;

public class RandomShit {
    public static void getKeys(Map map) {
        Object[] keys = map.keySet().toArray();
        for (int i = 0; i < map.size(); i++) {
            JMServerTest.LOGGER.info(i + ": " + keys[i]);
        }
    }
}
