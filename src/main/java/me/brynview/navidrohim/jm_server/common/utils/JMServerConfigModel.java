package me.brynview.navidrohim.jm_server.common.utils;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import me.brynview.navidrohim.jm_server.JMServer;

@Modmenu(modId = JMServer.MODID)
@Config(name = "jmserver-config", wrapperName = "JMServerConfig", defaultHook = true)
public class JMServerConfigModel {

    public boolean showAlerts = true;
    public boolean enabled = true;
    public int updateWaypointFrequency = 800;

}