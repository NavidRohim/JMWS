package me.brynview.navidrohim.jmws.common.events;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.JMWSSounds;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.network.ClientHandshakeHandler;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.config.ServerConfigObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static me.brynview.navidrohim.jmws.client.helper.PlayerHelper.sendUserAlert;
import static me.brynview.navidrohim.jmws.client.helper.PlayerHelper.sendUserSoundAlert;

public class CommonEvents {

    public static void clearCache()
    {
        CommonClass.setServerModStatus(false);
        CommonClass.serverConfig = ServerConfigObject.empty();
        PlayerHelper.clearWarningAlertCache();
    }

    public static void handleJoin(ServerPlayer serverPlayer)
    {
        ClientHandshakeHandler.sendHandshakeRequest(CommonClass.minecraftClientInstance, serverPlayer);
    }
}
