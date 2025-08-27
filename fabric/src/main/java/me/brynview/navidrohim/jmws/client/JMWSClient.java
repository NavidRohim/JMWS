package me.brynview.navidrohim.jmws.client;

import me.brynview.navidrohim.jmws.client.events.CommonEvents;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.callback.ClientCommandCallback;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.Minecraft;


public class JMWSClient implements ClientModInitializer {


    @Override
    public void onInitializeClient()
    {
        // fabric tick events
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandCallback::Callback);

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            CommonEvents.handleJoin();
        }));
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            CommonEvents.clearCache();
        }));
    }

    private void handleTick(Minecraft _minecraftClient)
    {
        if (CommonClass.syncCounter != null)
        {
            CommonClass.syncCounter.iterateCounter();
        }
    }


}
