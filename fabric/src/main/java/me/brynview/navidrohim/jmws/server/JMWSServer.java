package me.brynview.navidrohim.jmws.server;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;

import net.fabricmc.api.DedicatedServerModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;



public class JMWSServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer()
    {
        // no longer lonely
        Constants.getLogger().info("Initialised server on JMWS");

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            CommonEvents.handleJoin(handler.player, false, false);
        }));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommonClass.minecraftServerInstance = server;
        });
    }
}
