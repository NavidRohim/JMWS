package me.brynview.navidrohim.jmws;

import com.mojang.brigadier.CommandDispatcher;
import me.brynview.navidrohim.jmws.client.commands.ClientCommands;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeEventHandler
{
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post clientTickEvent)
    {
        if (CommonClass.syncCounter != null) // will be depricated and will become something like neo
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
    public static void RegisterClientCommandsEvent(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("jmws")
                    .then(Commands.literal("sync").executes(commandContext -> ClientCommands.sync()))
                    .then(Commands.literal("getSyncInterval").executes(commandContext -> ClientCommands.getSyncInterval()))
                    .then(Commands.literal("nextSync").executes(updateDisplayContext -> ClientCommands.nextSync()))
                    .then(Commands.literal("clearAll")
                            .then(Commands.literal("groups").executes(groupClearAllCtx -> ClientCommands.clearAllGroups()))
                            .then(Commands.literal("waypoints").executes(waypointClearAllCtx -> ClientCommands.clearAllWaypoints())))
        );
    }

}