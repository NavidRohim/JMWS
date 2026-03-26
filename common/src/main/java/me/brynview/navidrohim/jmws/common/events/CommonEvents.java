package me.brynview.navidrohim.jmws.common.events;

import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.client.commands.ClientCommands;
import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.client.share.OutgoingShareRequests;
import me.brynview.navidrohim.jmws.common.enums.MessageType;

import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.CommonClass;

import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.TimeUnit;

public class CommonEvents {

    public static void handleJoin(ServerPlayer serverPlayer, boolean isInternal, boolean sendWarningIfJMNotPresent)
    {

        if (isInternal && CommonClass.minecraftClientInstance.player == null)
        {
            if (sendWarningIfJMNotPresent && !ClientCommonClass.clientHasJM) {
                CommonClass.scheduler.schedule(() -> {PlayerHelper.sendUserAlert(Component.translatable("warning.jmws.jm_not_installed"), true, false, MessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
                return;
            }
            CommonClass.scheduler.schedule(() -> {PlayerHelper.sendUserAlert(Component.translatable("warning.jmws.world_is_local"), true, false, MessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
        } else {
            CommonClass.scheduler.schedule(() -> PlayerNetworkingHelper.sendHandshakeAndValidate(serverPlayer), ServerConfig.serverConfig.handshakeDelay, TimeUnit.MILLISECONDS);
            //PlayerNetworkingHelper.sendHandshakeAndValidate(serverPlayer);
        }
    }

    public static void handleDisconnect()
    {
        ClientCommands.sync();
        ClientCommonClass.setServerModStatus(false);
        ClientCommonClass.serverConfig = ClientSideServerConfigObject.empty();
        PlayerHelper.clearWarningAlertCache();

        IncomingShareRequests.clearAll();
        OutgoingShareRequests.clearAll();

        ClientCommonClass.isMapping = false;
        ClientCommonClass.didHandshake = false;
        ClientCommonClass.isBusy = false;
    }
}
