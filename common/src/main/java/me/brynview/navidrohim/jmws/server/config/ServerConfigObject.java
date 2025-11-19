package me.brynview.navidrohim.jmws.server.config;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
* An initialised subclass of this class (ClientSideServerConfigObject) is sent to every client that joins.
* The server has certain permissions of what is and is not allowed + the JMWS server version and that is sent in this class.
 * This class is also used on the server side to just read what the server can and cannot do
 */
public class ServerConfigObject {

    @Expose
    public Boolean jmwsEnabled;

    @Expose
    public Boolean waypointsEnabled;

    @Expose
    public Boolean groupsEnabled;

    @Expose
    public Boolean sharingEnabled;

    public List<Boolean> valueList;
    /**
     * Only use this constructor in its raw form on the server side. For the client side, use Gson().fromJson() with the raw packet data and specify this class.
     * @param jmwsEnabled If JMWS is enabled.
     * @param waypointsEnabled If waypoints are allowed to be synced.
     * @param groupsEnabled If groups are allowed to be synced.
     */
    public ServerConfigObject(boolean jmwsEnabled, boolean waypointsEnabled, boolean groupsEnabled, boolean sharingEnabled) {
        this.jmwsEnabled = jmwsEnabled;
        this.waypointsEnabled = waypointsEnabled;
        this.groupsEnabled = groupsEnabled;
        this.sharingEnabled = sharingEnabled;

        
        this.valueList = List.of(jmwsEnabled, waypointsEnabled, groupsEnabled, sharingEnabled);
    }

    /**
     * If the server has any syncing capabilities enabled (Checks if the mod is even enabled, then if waypoint and or group syncing is enabled)
     * @return boolean If the server allows any sort of syncing (If JMWS is enabled but both waypoint and group syncing is disabled, this will still return `false`)
     */
    public boolean serverEnabled()
    {
        return (jmwsEnabled && (waypointsEnabled || groupsEnabled));
    }

    /**
     * If the server allows waypoint syncing. The server must also have JMWS enabled for this to return `true`
     * @return boolean If waypoints are allowed to be synced.
     */
    public boolean waypointsEnabled()
    {
        return (jmwsEnabled && waypointsEnabled);
    }

    /**
     * If the server allows group syncing. The server must also have JMWS enabled for this to return `true`
     * @return boolean If groups are allowed to be synced.
     */
    public boolean groupsEnabled()
    {
        return (jmwsEnabled && groupsEnabled);
    }

    /**
     * If server has JMWS enabled, and allows waypoint and group syncing.
     * @return boolean If server has full syncing turned on.
     */
    public boolean allEnabled()
    {
        return (jmwsEnabled && waypointsEnabled && groupsEnabled && sharingEnabled);
    }
}