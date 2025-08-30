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

    public boolean waypointsEnabled()
    {
        return (jmwsEnabled && waypointsEnabled);
    }

    public boolean groupsEnabled()
    {
        return (jmwsEnabled && groupsEnabled);
    }

    public boolean allEnabled()
    {
        return (jmwsEnabled && waypointsEnabled && groupsEnabled);
    }

    public static ServerConfigObject empty()
    {
        return new ServerConfigObject(false, false, false);
    }

    public static ServerConfigObject serverOwner()
    {
        return new ServerConfigObject(true, true, true);
    }
}