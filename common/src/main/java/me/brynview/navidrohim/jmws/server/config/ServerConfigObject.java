package me.brynview.navidrohim.jmws.server.config;

public class ServerConfigObject {

    public Boolean jmwsEnabled;
    public Boolean waypointsEnabled;
    public Boolean groupsEnabled;

    public ServerConfigObject(boolean jmwsEnabled, boolean waypointsEnabled, boolean groupsEnabled) {
        this.jmwsEnabled = jmwsEnabled;
        this.waypointsEnabled = waypointsEnabled;
        this.groupsEnabled = groupsEnabled;
    }

}