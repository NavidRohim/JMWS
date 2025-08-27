package me.brynview.navidrohim.jmws.client.events;

import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.network.ClientHandshakeHandler;
import me.brynview.navidrohim.jmws.common.CommonClass;

public class CommonEvents {

    public static void clearCache()
    {
        CommonClass.setServerModStatus(false);
        CommonClass.serverConfig = null;
        PlayerHelper.clearWarningAlertCache();
    }

    public static void handleJoin()
    {
        ClientHandshakeHandler.sendHandshakeRequest(CommonClass.minecraftClientInstance);
    }
}
