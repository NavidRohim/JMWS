package me.brynview.navidrohim.jmws;

import com.mojang.brigadier.CommandDispatcher;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.server.commands.ServerDispatcher;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(Dist.DEDICATED_SERVER)
public class NeoforgeServerEventHandler {

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event)
    {
        CommonClass.minecraftServerInstance = event.getServer();
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
    {

        if (event.getEntity() instanceof ServerPlayer && ServerConfig.serverConfig.serverEnabled())
        {
            CommonEvents.handleJoin((ServerPlayer) event.getEntity(), false, false);
        }
    }

    @SubscribeEvent
    public static void registerServerCommandsEvent(RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ServerDispatcher.addCommandsToDispatcher(dispatcher);
    }
}
