package me.brynview.navidrohim.jmws.common.events;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.commands.ClientCommands;
import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.common.enums.MessageType;

import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;

import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.TimeUnit;

public class CommonEvents {

    public static void handlePlayerStateChange()
    {
        if (JMWSClientCommon.currentShareScreen != null)
        {
            JMWSClientCommon.currentShareScreen.refresh();
        }
    }

    public static void handleJoin(ServerPlayer serverPlayer, boolean isInternal, boolean sendWarningIfJMNotPresent)
    {

        if (isInternal && JMWSCommon.minecraftClientInstance.player == null)
        {
            if (sendWarningIfJMNotPresent && !JMWSClientCommon.clientHasJM) {
                JMWSCommon.scheduler.schedule(() -> {
                    PlayerUtils.sendUserAlert(Component.translatable("warning.jmws.jm_not_installed"), true, false, MessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
                return;
            }

            JMWSCommon.scheduler.schedule(() -> {
                PlayerUtils.sendUserAlert(Component.translatable("warning.jmws.world_is_local"), true, false, MessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
        } else {
            JMWSCommon.scheduler.schedule(() -> PlayerNetworkingHelper.sendHandshakeAndValidate(serverPlayer), ServerConfig.serverConfig.handshakeDelay, TimeUnit.MILLISECONDS);
            //PlayerNetworkingHelper.sendHandshakeAndValidate(serverPlayer);
        }
    }

    public static void handleDisconnect()
    {
        ClientCommands.sync();
        JMWSClientCommon.setServerModStatus(false);
        JMWSClientCommon.serverConfig = ClientSideServerConfigObject.empty();
        PlayerUtils.clearWarningAlertCache();

        JMWSClientCommon.incomingShareRequests.clearAll();
        JMWSClientCommon.outgoingShareRequests.clearAll();

        JMWSClientCommon.isMapping = false;
        JMWSClientCommon.didHandshake = false;
        JMWSClientCommon.isBusy = false;
        ObjectIdentifierMap.clear();
    }
}
