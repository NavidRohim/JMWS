package me.navidrohim.jmws.plugin;


public class ConfigInterface {

    public Boolean enabled;
    public final Boolean uploadWaypoints;
    public final Boolean uploadGroups;
    public final Boolean showAlerts;
    public final Boolean playEffects;
    public final Boolean colouredText;
    public final Integer updateWaypointFrequency;
    public final Integer serverHandshakeTimeout;

    public ConfigInterface() {
        this.enabled =         true;
        this.uploadWaypoints = true;
        this.uploadGroups =    true;

        this.showAlerts  = true;
        this.playEffects = true;
        this.colouredText = true;

        this.updateWaypointFrequency = 40;
        this.serverHandshakeTimeout =  5;
    }

    public int getUpdateWaypointFrequencyAsTicks()
    {
        return updateWaypointFrequency * 20;
    }
}
