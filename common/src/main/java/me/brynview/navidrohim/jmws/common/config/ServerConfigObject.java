package me.brynview.navidrohim.jmws.common.config;

import me.brynview.navidrohim.jmws.Constants;
import org.jetbrains.annotations.Nullable;

public class ServerConfigObject {

    public Boolean jmwsEnabled;
    public Boolean waypointsEnabled;
    public Boolean groupsEnabled;
    @Nullable private Double serverVersion = null;

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

    public void _setServerVersion(double serverVersion)
    {
        this.serverVersion = serverVersion;
    }

    @Nullable
    public Double getServerVersion()
    {
        return this.serverVersion;
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