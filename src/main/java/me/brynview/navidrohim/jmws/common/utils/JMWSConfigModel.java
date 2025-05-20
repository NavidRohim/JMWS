package me.brynview.navidrohim.jmws.common.utils;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;
import me.brynview.navidrohim.jmws.JMServer;

@Modmenu(modId = JMServer.MODID)
@Config(name = "jmws-config", wrapperName = "JMWSConfig", defaultHook = true)
public class JMWSConfigModel {

    @SectionHeader("generalConfig")
    public boolean showAlerts = true;
    public int updateWaypointFrequency = 800;
    public int serverHandshakeTimeout = 5;

    @SectionHeader("upload")
    public boolean enabled = true;
    public boolean uploadWaypoints = true;
    public boolean uploadGroups = true;

}