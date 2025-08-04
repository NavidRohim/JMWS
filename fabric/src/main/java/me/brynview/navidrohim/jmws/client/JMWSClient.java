package me.brynview.navidrohim.jmws.client;
import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.CommonClass;

import me.brynview.navidrohim.jmws.client.config.JMWSConfig;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.payloads.JMWSHandshakePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

    public static final JMWSConfig CONFIG = JMWSConfig.createAndLoad();

    private ClientLevel oldWorld = null;
    public static int tickCounterUpdateThreshold = 800;//config.clientConfiguration.updateWaypointFrequency();
    public static int tickCounter = 0;

    public static boolean serverHasMod = false;

    private static ScheduledFuture<?> timeoutTask;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInitializeClient()
    {
        // fabric tick events
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {

            if (!client.isSingleplayer()) {
                Dispatcher.sendToServer(new JMWSHandshakePayload());

                // 5 was config.clientConfiguration.serverHandshakeTimeout()
                timeoutTask = scheduler.schedule(() -> {
                    if (!serverHasMod) {
                        CommonClass.minecraftClientInstance.execute(() -> {
                            sendUserAlert(Component.translatable("error.jmws.jmws_not_installed"), true, true, JMWSMessageType.FAILURE);
                            sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                        });
                    }
                }, 5, TimeUnit.SECONDS);
            } else {
                sendUserAlert(Component.translatable("warning.jmws.world_is_local"), true, false, JMWSMessageType.WARNING);
                sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
            }
        }));

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            tickCounter = 0;
            serverHasMod = false;
        }));
        /*

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandCallback::Callback);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {

            if (!client.isInSingleplayer()) {
                ClientPlayNetworking.send(new HandshakePayload());

                timeoutTask = scheduler.schedule(() -> {
                    if (!serverHasMod) {
                        minecraftClientInstance.execute(() -> {
                            sendUserAlert(Text.translatable("error.jmws.jmws_not_installed"), true, true, JMWSMessageType.FAILURE);
                            sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                        });
                    }
                }, config.clientConfiguration.serverHandshakeTimeout(), TimeUnit.SECONDS);
            } else {
                sendUserAlert(Text.translatable("warning.jmws.world_is_local"), true, false, JMWSMessageType.WARNING);
                sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
            }
        }));
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            tickCounter = 0;
            serverHasMod = false;
        }));*/

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

        // true was (world != null && this.getEnabledStatus())
        if (world != null && true) {
            if (world != oldWorld) {
                if (oldWorld == null) {
                    // 5 was config.clientConfiguration.serverHandshakeTimeout()
                    tickCounterUpdateThreshold = 20 * (5 + 1); // Add 1 second buffer to not interrupt message
                } else {
                    tickCounterUpdateThreshold = 40; // 2-second delay when switching dimension
                }
                tickCounter = 0;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) {

                    updateWaypoints(true);
                    tickCounter = 0;
                    // 800 was config.clientConfiguration.updateWaypointFrequency()
                    tickCounterUpdateThreshold = 800;
                }
            }
            oldWorld = CommonClass.minecraftClientInstance.level;
        } else {

            tickCounter = 0;
            oldWorld = null;
        }
    }


}
