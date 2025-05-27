package me.brynview.navidrohim.jmws.client;
import me.brynview.navidrohim.jmws.common.config.JMWSConfig;
import net.fabricmc.api.ClientModInitializer;

public class JMWSClient implements ClientModInitializer {

    public static JMWSConfig CONFIG;

    @Override
    public void onInitializeClient()
    {
        CONFIG = JMWSConfig.createAndLoad();
    }
}
