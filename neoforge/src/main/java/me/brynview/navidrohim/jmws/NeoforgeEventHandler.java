package me.brynview.navidrohim.jmws;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.brynview.navidrohim.jmws.client.commands.ClientCommands;
import me.brynview.navidrohim.jmws.client.commands.CommonClientPlatformCommands;
import me.brynview.navidrohim.jmws.client.commands.ShareSuggestions;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(Dist.CLIENT)
public class NeoforgeEventHandler
{
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post clientTickEvent)
    {
        if (CommonClass.syncCounter != null)
        {
            CommonClass.syncCounter.iterateCounter();
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
            CommonEvents.clearCache();
        }
    }

    @SubscribeEvent
    public static void RegisterClientCommandsEvent(RegisterClientCommandsEvent event)
    {
        CommonClientPlatformCommands.registerClientDispatcher(event.getDispatcher());
    }

}