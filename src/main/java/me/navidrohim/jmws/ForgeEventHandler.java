package me.navidrohim.jmws;

import me.navidrohim.jmws.client.ClientHandshakeHandler;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
    public static void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        ClientHandshakeHandler.sendHandshakeRequest(CommonClass.minecraftClientInstance);
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(PlayerEvent.PlayerLoggedOutEvent event)
    {
        CommonClass.setServerModStatus(false);
    }
    /*
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
    }*/

}