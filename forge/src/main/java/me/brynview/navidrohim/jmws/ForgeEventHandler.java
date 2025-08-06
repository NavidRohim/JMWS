package me.brynview.navidrohim.jmws;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.brynview.navidrohim.jmws.client.ClientCommands;
import me.brynview.navidrohim.jmws.client.ClientHandshakeHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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