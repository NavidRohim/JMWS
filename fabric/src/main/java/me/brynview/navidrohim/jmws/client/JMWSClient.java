package me.brynview.navidrohim.jmws.client;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.commands.CommonClientPlatformCommands;
import me.brynview.navidrohim.jmws.client.screens.MissingJourneyMapScreen;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.common.CommonClass;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;


public class JMWSClient implements ClientModInitializer {


    @Override
    public void onInitializeClient()
    {
        // fabric tick events
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> CommonClientPlatformCommands.registerClientDispatcher(dispatcher));
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            CommonEvents.handleJoin(handler.player, true, true);
        }));
        ScreenEvents.AFTER_INIT.register(this::waitForStartScreenRegister);
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            ClientCommonClass.clearCache();
        }));

        // New
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommonClass.minecraftServerInstance = server;
        });
    }

    private void waitForStartScreenRegister(Minecraft minecraft, Screen screen, int i, int i1) {

        if (screen instanceof TitleScreen && !ClientCommonClass.clientHasJM)
        {
            Constants.getLogger().error("JourneyMap is missing or the wrong version is installed!");
            minecraft.setScreen(new MissingJourneyMapScreen(Component.translatable("text.config.jmws-config.title"), Component.translatable("warning.jmws.jm_not_installed")));
        }

    }

    private void handleTick(Minecraft _minecraftClient)
    {
        if (ClientCommonClass.syncCounter != null)
        {
            ClientCommonClass.syncCounter.iterateCounter();
        }
    }


}
