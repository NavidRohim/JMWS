package me.brynview.navidrohim.jmws.common.config;

public class ServerConfigObject {

    public Boolean jmwsEnabled;
    public Boolean waypointsEnabled;
    public Boolean groupsEnabled;

    public ServerConfigObject(boolean jmwsEnabled, boolean waypointsEnabled, boolean groupsEnabled) {
        this.jmwsEnabled = jmwsEnabled;
        this.waypointsEnabled = waypointsEnabled;
        this.groupsEnabled = groupsEnabled;
    }

    public boolean serverEnabled()
    {
        return (jmwsEnabled && (waypointsEnabled || groupsEnabled));
    }
}