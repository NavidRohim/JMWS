package me.navidrohim.jmws.common.payloads;

import com.google.gson.Gson;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.CommonProxy;

public class ServerConfigSendable {
    public Boolean enabled;

    public ServerConfigSendable()
    {
        this.enabled = CommonClass.config.enabled;
    }

    public static String getServerConfigSendable()
    {
        return new Gson().toJson(new ServerConfigSendable());
    }

    public static ServerConfigSendable getServerConfigFromData(String data)
    {
        return new Gson().fromJson(data, ServerConfigSendable.class);
    }
}
