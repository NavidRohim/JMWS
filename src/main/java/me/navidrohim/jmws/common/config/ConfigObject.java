package me.navidrohim.jmws.common.config;

/**
* An initialised subclass of this class (ClientSideServerConfigObject) is sent to every client that joins.
* The server has certain permissions of what is and is not allowed + the JMWS server version and that is sent in this class.
 * This class is also used on the server side to just read what the server can and cannot do
 */
public class ConfigObject {

    public Boolean jmwsEnabled;

    /**
     * Only use this constructor in its raw form on the server side. For the client side, use Gson().fromJson() with the raw packet data and specify this class.
     * @param jmwsEnabled If JMWS is enabled.
     */
    public ConfigObject(boolean jmwsEnabled) {
        this.jmwsEnabled = jmwsEnabled;
    }

    /**
     * If the server has any syncing capabilities enabled
     * @return boolean If the server allows any sort of syncing
     */
    public boolean serverEnabled()
    {
        return jmwsEnabled;
    }
}