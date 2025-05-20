package me.brynview.navidrohim.jmws.common.utils;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import me.brynview.navidrohim.jmws.JMServer;

@Modmenu(modId = JMServer.MODID)
@Config(name = "jmws-config", wrapperName = "JMWSConfig", defaultHook = true)
public class JMWSConfigModel {

    public boolean showAlerts = true;
    public boolean enabled = true;
    public int updateWaypointFrequency = 800;
    public int serverHandshakeTimeout = 5;

}