package me.brynview.navidrohim.jmws.client.network;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.JMWSSounds;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static me.brynview.navidrohim.jmws.client.helper.PlayerHelper.sendUserAlert;
import static me.brynview.navidrohim.jmws.client.helper.PlayerHelper.sendUserSoundAlert;

public class ClientHandshakeHandler {

    public static void sendHandshakeRequest(ServerPlayer serverPlayer)
    {
        Dispatcher.sendToClient(new JMWSHandshakePayload(), serverPlayer);
    }
}
