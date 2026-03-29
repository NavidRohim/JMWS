package me.brynview.navidrohim.jmws;

import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.client.commands.CommonClientPlatformCommands;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeEventHandler
{
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post clientTickEvent)
    {
        if (ClientCommonClass.syncCounter != null) // will be depricated and will become something like neo
        {
            ClientCommonClass.syncCounter.iterateCounter();
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer && ServerConfig.serverConfig.serverEnabled())
        {
            CommonEvents.handleJoin((ServerPlayer) event.getEntity(), true, false);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer)
        {
            CommonEvents.handleDisconnect();
        }
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event)
    {
        CommonClass.minecraftServerInstance = event.getServer();
    }

    @SubscribeEvent
    public static void RegisterClientCommandsEvent(RegisterClientCommandsEvent event)
    {
        CommonClientPlatformCommands.registerClientDispatcher(event.getDispatcher());
    }

}