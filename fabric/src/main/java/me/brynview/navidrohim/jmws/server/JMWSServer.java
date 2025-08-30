package me.brynview.navidrohim.jmws.server;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class JMWSServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer()
    {
        // no longer lonely
        Constants.getLogger().info("Initialised server on JMWS");

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            if (ServerConfig.serverConfig.serverEnabled())
            {
                CommonEvents.handleJoin(handler.player, false);
            }
        }));
    }
}
