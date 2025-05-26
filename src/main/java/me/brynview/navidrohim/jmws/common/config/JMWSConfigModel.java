package me.brynview.navidrohim.jmws.common.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;
import me.brynview.navidrohim.jmws.JMServer;

@Modmenu(modId = JMServer.MODID)
@Config(name = "jmws-config", wrapperName = "JMWSConfig", defaultHook = true)
public class JMWSConfigModel {

    @SectionHeader("upload")
    public boolean enabled = true;
    public boolean uploadWaypoints = true;
    public boolean uploadGroups = true;

    @SectionHeader("personalisation")
    public boolean showAlerts = true;
    public boolean playEffects = false;
    public boolean colouredText = true;

    @SectionHeader("generalConfig")
    public int updateWaypointFrequency = 800;
    public int serverHandshakeTimeout = 5;
}