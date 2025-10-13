package me.navidrohim.jmws.client.events;

import me.navidrohim.jmws.client.config.ConfigInterface;
import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.client.helpers.JMWSSounds;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.events.CommonEventHelper;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class ForgeEventHandler
{

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent clientTickEvent)
    {
        if (CommonClass.syncCounter != null && clientTickEvent.phase.equals(TickEvent.Phase.END)) // will be depricated and will become something like neo
        {
            CommonClass.syncCounter.iterateCounter();
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(PlayerEvent.PlayerLoggedOutEvent event)
    {
        CommonEventHelper.clearCache();
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
            ConfigInterface.serverEnabled = CommonClass.serverConfig.serverEnabled();
        }
    }
    @SubscribeEvent
    public static void onConfigChangedRevert(final ConfigChangedEvent.PostConfigChangedEvent event) {
        if (event.getModID().equals(Constants.MODID)) {
            ConfigInterface.serverEnabled = CommonClass.serverConfig.serverEnabled();
        }
    }
}