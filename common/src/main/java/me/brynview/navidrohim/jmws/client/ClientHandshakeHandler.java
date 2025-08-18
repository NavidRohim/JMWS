package me.brynview.navidrohim.jmws.client;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.payloads.JMWSHandshakePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static me.brynview.navidrohim.jmws.helper.PlayerHelper.sendUserAlert;
import static me.brynview.navidrohim.jmws.helper.PlayerHelper.sendUserSoundAlert;

public class ClientHandshakeHandler {

    public static ScheduledFuture<?> timeoutTask;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void sendHandshakeRequest(Minecraft client)
    {
        if (!client.isSingleplayer()) {
            Dispatcher.sendToServer(new JMWSHandshakePayload());

            timeoutTask = scheduler.schedule(() -> {
                if (!CommonClass.serverHasMod) {
                    CommonClass.minecraftClientInstance.execute(() -> {
                        sendUserAlert(Component.translatable("error.jmws.jmws_not_installed"), true, true, JMWSMessageType.FAILURE);
                        sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    });
                }
            }, CommonClass.config.serverHandshakeTimeout.get(), TimeUnit.SECONDS);
        } else {
            sendUserAlert(Component.translatable("warning.jmws.world_is_local"), true, false, JMWSMessageType.WARNING);
            sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
        }
    }
}
