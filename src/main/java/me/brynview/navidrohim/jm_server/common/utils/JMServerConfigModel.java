package me.brynview.navidrohim.jm_server.common.utils;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "jmserver-config", wrapperName = "JMServerConfig")
public class JMServerConfigModel {
    public boolean showAlerts = true;
    public int updateWaypointFrequency = 1200;
}
