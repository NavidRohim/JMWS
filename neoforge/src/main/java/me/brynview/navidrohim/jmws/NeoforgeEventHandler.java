package me.brynview.navidrohim.jmws;

import com.mojang.brigadier.CommandDispatcher;
import me.brynview.navidrohim.jmws.client.ClientCommands;
import me.brynview.navidrohim.jmws.client.events.CommonEvents;
import me.brynview.navidrohim.jmws.client.network.ClientHandshakeHandler;
import me.brynview.navidrohim.jmws.common.CommonClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

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
    public static void onEntityJoinWorld(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            CommonEvents.handleJoin();
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            CommonEvents.handleDisconnect();
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