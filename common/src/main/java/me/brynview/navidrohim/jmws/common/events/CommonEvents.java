package me.brynview.navidrohim.jmws.common.events;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.JMWSSounds;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.network.ClientHandshakeHandler;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.config.ServerConfigObject;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.TimeUnit;

import static me.brynview.navidrohim.jmws.client.helper.PlayerHelper.sendUserAlert;
import static me.brynview.navidrohim.jmws.client.helper.PlayerHelper.sendUserSoundAlert;

public class CommonEvents {

    public static void clearCache()
    {
        // will use soon
        CommonClass.setServerModStatus(false);
        CommonClass.serverConfig = ServerConfigObject.empty();
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
            ClientHandshakeHandler.sendHandshakeRequest(serverPlayer);
        }
    }
}
