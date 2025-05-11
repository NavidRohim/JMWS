package me.brynview.navidrohim.jm_server.client;
import me.brynview.navidrohim.jm_server.common.utils.JMServerConfig;
import net.fabricmc.api.ClientModInitializer;

public class JMServerClient implements ClientModInitializer {

    public static JMServerConfig CONFIG;

    @Override
    public void onInitializeClient()
    {
        CONFIG = JMServerConfig.createAndLoad();
    }
}
