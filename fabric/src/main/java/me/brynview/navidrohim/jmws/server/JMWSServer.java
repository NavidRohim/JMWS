package me.brynview.navidrohim.jmws.server;

import me.brynview.navidrohim.jmws.server.config.JMWSServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;

import java.io.File;



public class JMWSServer implements DedicatedServerModInitializer {
    public static JMWSServerConfig SERVER_CONFIG;

    @Override
    public void onInitializeServer() {
        SERVER_CONFIG = JMWSServerConfig.createAndLoad();
    }
}
