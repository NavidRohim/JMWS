package me.navidrohim.jmws.client.events;

import me.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.client.helpers.JMWSSounds;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.client.network.ClientHandshakeHandler;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;


public class ForgeEventHandler
{
    private static boolean pendingJoin = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent clientTickEvent)
    {
        if (CommonClass.syncCounter != null && clientTickEvent.phase.equals(TickEvent.Phase.END)) // will be depricated and will become something like neo
        {
            CommonClass.syncCounter.iterateCounter();
        }
    }

    @SubscribeEvent
    public static void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        pendingJoin = true; // Mark that we've just connected
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (pendingJoin && event.getEntity() == CommonClass.minecraftClientInstance.player) {
            pendingJoin = false; // Clear the flag so it only runs once
            ClientHandshakeHandler.sendHandshakeRequest(CommonClass.minecraftClientInstance);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(PlayerEvent.PlayerLoggedOutEvent event)
    {
        pendingJoin = false; // Just in case
        CommonClass.setServerModStatus(false);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
    {
        final SoundEvent[] soundEvents = {
                JMWSSounds.ACTION_SUCCEED,
                JMWSSounds.ACTION_FAILURE
        };
        event.getRegistry().registerAll(soundEvents);
    }

    @SubscribeEvent
    public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Constants.MODID)) {
            ConfigManager.sync(Constants.MODID, Config.Type.INSTANCE);
        }
    }
}