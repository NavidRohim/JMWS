package me.brynview.navidrohim.jmws.client;
import me.brynview.navidrohim.jmws.Constants;

import me.brynview.navidrohim.jmws.client.config.JMWSConfig;
import me.brynview.navidrohim.jmws.payloads.JMWSActionPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class JMWSClient implements ClientModInitializer {

    public static final JMWSConfig CONFIG = JMWSConfig.createAndLoad();

    @Override
    public void onInitializeClient()
    {
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

}
