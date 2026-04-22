package me.brynview.navidrohim.jmws;

import com.mojang.brigadier.CommandDispatcher;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ForgeServerEventHandler {

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event)
    {
        JMWSCommon.minecraftServerInstance = event.getServer();
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
    {

        if (event.getEntity() instanceof ServerPlayer && ServerConfig.serverConfig.serverEnabled())
        {
            CommonEvents.handleJoin((ServerPlayer) event.getEntity(), false, false);
        }
    }
}
