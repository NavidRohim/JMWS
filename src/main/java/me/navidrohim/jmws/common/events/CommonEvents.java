package me.navidrohim.jmws.common.events;


import me.navidrohim.jmws.client.ClientProxy;
import me.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.common.helper.CommonHelper;
import me.navidrohim.jmws.common.helper.PlayerHelper;
import me.navidrohim.jmws.common.payloads.JMWSHandshakeReplyMessage;
import me.navidrohim.jmws.common.payloads.JMWSNetworkWrapper;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.concurrent.TimeUnit;

public class CommonEvents {

    public static void clearCache()
    {
        CommonClass.setServerModStatus(false);
        ClientProxy.serverConfig = ClientSideServerConfigObject.empty();
        PlayerHelper.clearWarningAlertCache();
    }

    public static void handleJoin(EntityPlayerMP serverPlayer, boolean isInternal, boolean sendWarningIfJMNotPresent)
    {

        if (isInternal && CommonClass.minecraftClientInstance.player == null)
        {
            if (sendWarningIfJMNotPresent && !CommonClass.clientHasJM) {
                CommonClass.scheduler.schedule(() -> {PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.jm_not_installed"), true, false, JMWSMessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
                return;
            }
            CommonClass.scheduler.schedule(() -> {
                PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.world_is_local"), true, false, JMWSMessageType.NEUTRAL);}, 2, TimeUnit.SECONDS);
        } else {
            JMWSNetworkWrapper.INSTANCE.sendTo(new JMWSHandshakeReplyMessage(), serverPlayer);

        }
    }
}
