package me.navidrohim.jmws.client;

import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.client.helpers.JMWSSounds;
import me.navidrohim.jmws.helper.CommonHelper;
import me.navidrohim.jmws.payloads.JMWSHandshakeMessage;
import me.navidrohim.jmws.payloads.JMWSNetworkWrapper;
import net.minecraft.client.Minecraft;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static me.navidrohim.jmws.helper.PlayerHelper.sendUserAlert;
import static me.navidrohim.jmws.helper.PlayerHelper.sendUserSoundAlert;


public class ClientHandshakeHandler {

    public static ScheduledFuture<?> timeoutTask;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void sendHandshakeRequest(Minecraft client)
    {
        if (!client.isSingleplayer()) {
            JMWSNetworkWrapper.INSTANCE.sendToServer(new JMWSHandshakeMessage());
            //Dispatcher.sendToServer(new JMWSHandshakePayload

            timeoutTask = scheduler.schedule(() -> {
                if (!CommonClass.serverHasMod) {
                    CommonClass.minecraftClientInstance.addScheduledTask(() -> {
                        sendUserAlert(CommonHelper.getTranslatableComponent("error.jmws.jmws_not_installed"), true, true, JMWSMessageType.FAILURE);
                        sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    });
                }
            }, CommonClass.config.serverHandshakeTimeout, TimeUnit.SECONDS);
        } else {
            sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.world_is_local"), true, false, JMWSMessageType.WARNING);
            sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
        }
    }
}
