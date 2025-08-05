package me.brynview.navidrohim.jmws.client;
import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.CommonClass;

import me.brynview.navidrohim.jmws.client.callback.ClientCommandCallback;

import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.payloads.JMWSHandshakePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static me.brynview.navidrohim.jmws.helper.PlayerHelper.*;
import static me.brynview.navidrohim.jmws.plugin.JMWSPlugin.updateWaypoints;

public class JMWSClient implements ClientModInitializer {

    private ClientLevel oldWorld = null;
    public static int tickCounterUpdateThreshold = 800;
    public static int tickCounter = 0;

    public static boolean serverHasMod = false;

    private static ScheduledFuture<?> timeoutTask;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInitializeClient()
    {
        // fabric tick events
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandCallback::Callback);

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {

            if (!client.isSingleplayer()) {
                Dispatcher.sendToServer(new JMWSHandshakePayload());

                timeoutTask = scheduler.schedule(() -> {
                    if (!serverHasMod) {
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
        }));

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            tickCounter = 0;
            serverHasMod = false;
        }));
    }

    public static void setServerModStatus(boolean serverModStatus)
    {
        serverHasMod = serverModStatus;

        if (timeoutTask != null && !timeoutTask.isDone()) {
            timeoutTask.cancel(false);
        }
    }
    private void handleTick(Minecraft _minecraftClient) {

        // Sends "sync" packet | New = SYNC
        ClientLevel world = CommonClass.minecraftClientInstance.level;

        if (world != null && CommonClass.getEnabledStatus()) {
            if (world != oldWorld) {
                if (oldWorld == null) {
                    tickCounterUpdateThreshold = 20 * (CommonClass.config.serverHandshakeTimeout.get() + 1); // Add 1 second buffer to not interrupt message
                } else {
                    tickCounterUpdateThreshold = 40; // 2-second delay when switching dimension
                }
                tickCounter = 0;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) {

                    updateWaypoints(true);
                    tickCounter = 0;
                    tickCounterUpdateThreshold = CommonClass.config.updateWaypointFrequency.get();
                }
            }
            oldWorld = CommonClass.minecraftClientInstance.level;
        } else {

            tickCounter = 0;
            oldWorld = null;
        }
    }


}
