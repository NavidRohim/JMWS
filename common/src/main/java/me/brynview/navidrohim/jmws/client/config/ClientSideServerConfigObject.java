package me.brynview.navidrohim.jmws.client.config;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.server.config.ServerConfigObject;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side version of ServerConfigObject. Holds extra data which is the server version
 */
public class ClientSideServerConfigObject extends ServerConfigObject {

    @Nullable
    private final Double serverVersion;

    /**
     * Create server config object. Do not use this constructor and get an instance from CommonClass.serverConfig
     * @param jmwsEnabled If JMWS is enabled.
     * @param waypointsEnabled If waypoints are allowed to be synced.
     * @param groupsEnabled If groups are allowed to be synced.
     * @param serverVersion The remote servers JMWS server version
     */
    public ClientSideServerConfigObject(boolean jmwsEnabled, boolean waypointsEnabled, boolean groupsEnabled, boolean sharingEnabled, @Nullable Double serverVersion) {
        super(jmwsEnabled, waypointsEnabled, groupsEnabled, sharingEnabled);
        this.serverVersion = serverVersion;
    }

    /**
     *
     * @return Double -- The connected servers JMWS server version
     */
    @Nullable
    public Double getServerVersion()
    {
        return this.serverVersion;
    }

    /**
     *
     * @return ClientServerConfigObject -- Empty permissions object. Used as default.
     */
    public static ClientSideServerConfigObject empty()
    {
        return new ClientSideServerConfigObject(false, false, false, false, Constants.SERVER_VERSION);
    }

    /**
     *
     * @return ClientServerConfigObject -- Emulated server config version with all permissions allowed (for LAN)
     */
    public static ClientSideServerConfigObject serverOwner()
    {
        return new ClientSideServerConfigObject(true, true, true, true, Constants.SERVER_VERSION);
    }
}
