package me.brynview.navidrohim.jmws.client;
import me.brynview.navidrohim.jmws.client.callbacks.ClientCommandCallback;
import me.brynview.navidrohim.jmws.common.config.JMWSConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class JMWSClient implements ClientModInitializer {

    public static JMWSConfig CONFIG;

    @Override
    public void onInitializeClient()
    {
        CONFIG = JMWSConfig.createAndLoad();
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandCallback::Callback);
    }
}
