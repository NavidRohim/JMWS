package me.brynview.navidrohim.jmws.server.network;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PlayerNetworkingHelper {
    public static void sendUserMessage(ServerPlayer player, String messageKey, Boolean overlay, boolean isError) {
        JMWSActionPayload messagePayload = new JMWSActionPayload(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, isError));
        Dispatcher.sendToClient(messagePayload, player);
    }

    public static void sendUserMessage(UUID player, String messageKey, Boolean overlay, boolean isError) {
        JMWSActionPayload messagePayload = new JMWSActionPayload(CommandFactory.makeClientAlertRequestJson(messageKey, overlay, isError));
        Dispatcher.sendToClient(messagePayload, CommonClass.minecraftServerInstance.getPlayerList().getPlayer(player));
    }
}
