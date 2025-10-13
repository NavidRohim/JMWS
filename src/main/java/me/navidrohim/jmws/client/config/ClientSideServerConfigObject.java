package me.navidrohim.jmws.client.config;

import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.common.config.ConfigObject;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side version of ServerConfigObject. Holds extra data which is the server version
 */
public class ClientSideServerConfigObject extends ConfigObject {

    @Nullable
    private final Double serverVersion;

    /**
     * Create server config object. Do not use this constructor and get an instance from CommonClass.serverConfig
     * @param jmwsEnabled If JMWS is enabled.
     * @param serverVersion The remote servers JMWS server version
     */
    public ClientSideServerConfigObject(boolean jmwsEnabled, @Nullable Double serverVersion) {
        super(jmwsEnabled);
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
        return new ClientSideServerConfigObject(false, Constants.SERVER_VERSION);
    }

    /**
     *
     * @return ClientServerConfigObject -- Emulated server config version with all permissions allowed (for LAN)
     */
    public static ClientSideServerConfigObject serverOwner()
    {
        return new ClientSideServerConfigObject(true, Constants.SERVER_VERSION);
    }
}
