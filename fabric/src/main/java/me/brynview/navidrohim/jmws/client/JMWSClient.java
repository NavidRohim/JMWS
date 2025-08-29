package me.brynview.navidrohim.jmws.client;

import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.callback.ClientCommandCallback;

import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.Minecraft;


public class JMWSClient implements ClientModInitializer {


    @Override
    public void onInitializeClient()
    {
        // fabric tick events
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandCallback::Callback);
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            if (ServerConfig.serverConfig.serverEnabled())
            {
                CommonEvents.handleJoin(handler.player);
            }
        }));
        /*ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            CommonEvents.clearCache();
        }));*/
    }

    private void handleTick(Minecraft _minecraftClient)
    {
        if (CommonClass.syncCounter != null)
        {
            CommonClass.syncCounter.iterateCounter();
        }
    }


}
