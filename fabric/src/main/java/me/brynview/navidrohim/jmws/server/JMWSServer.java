package me.brynview.navidrohim.jmws.server;

import me.brynview.navidrohim.jmws.Constants;
import net.fabricmc.api.DedicatedServerModInitializer;

public class JMWSServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer()
    {
        // im so lonelyyyyy i got nobodyyyyy
        Constants.getLogger().info("Initialised server on JMWS");
    }

}
