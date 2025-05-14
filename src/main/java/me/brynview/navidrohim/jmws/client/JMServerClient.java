package me.brynview.navidrohim.jmws.client;
import me.brynview.navidrohim.jmws.common.utils.JMWSConfig;
import net.fabricmc.api.ClientModInitializer;

public class JMServerClient implements ClientModInitializer {

    public static JMWSConfig CONFIG;

    @Override
    public void onInitializeClient()
    {
        CONFIG = JMWSConfig.createAndLoad();
    }
}
