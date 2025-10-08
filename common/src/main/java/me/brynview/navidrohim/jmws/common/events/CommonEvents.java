package me.brynview.navidrohim.jmws.common.events;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;

import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.CommonClass;

import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.TimeUnit;

public class CommonEvents {

    public static void clearCache()
    {
        // will use soon
        CommonClass.setServerModStatus(false);
        CommonClass.serverConfig = ClientSideServerConfigObject.empty();
        PlayerHelper.clearWarningAlertCache();
    }

    public static void handleJoin(ServerPlayer serverPlayer, boolean isInternal, boolean sendWarningIfJMNotPresent)
    {

        if (isInternal && CommonClass.minecraftClientInstance.player == null)
        {
            if (sendWarningIfJMNotPresent && !CommonClass.clientHasJM) {
                CommonClass.scheduler.schedule(() -> {PlayerHelper.sendUserAlert(Component.translatable("warning.jmws.jm_not_installed"), true, false, JMWSMessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
                return;
            }
            CommonClass.scheduler.schedule(() -> {PlayerHelper.sendUserAlert(Component.translatable("warning.jmws.world_is_local"), true, false, JMWSMessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
        } else {
            Dispatcher.sendToClient(new JMWSHandshakePayload(), serverPlayer);
        }
    }
}
