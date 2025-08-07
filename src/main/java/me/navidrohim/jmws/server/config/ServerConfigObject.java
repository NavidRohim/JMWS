package me.navidrohim.jmws.server.config;

public class ServerConfigObject {

    public Boolean jmwsEnabled;
    public Boolean waypointsEnabled;

    public ServerConfigObject(boolean jmwsEnabled, boolean waypointsEnabled) {
        this.jmwsEnabled = jmwsEnabled;
        this.waypointsEnabled = waypointsEnabled;
    }

}