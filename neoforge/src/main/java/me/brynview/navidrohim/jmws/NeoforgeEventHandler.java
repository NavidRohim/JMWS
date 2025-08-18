package me.brynview.navidrohim.jmws;

import com.mojang.brigadier.CommandDispatcher;
import me.brynview.navidrohim.jmws.client.ClientCommands;
import me.brynview.navidrohim.jmws.client.ClientHandshakeHandler;
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
            ClientHandshakeHandler.sendHandshakeRequest(CommonClass.minecraftClientInstance);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            CommonClass.setServerModStatus(false);
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