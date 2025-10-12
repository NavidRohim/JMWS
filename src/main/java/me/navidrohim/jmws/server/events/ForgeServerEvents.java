package me.navidrohim.jmws.server.events;

import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.common.events.CommonEvents;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ForgeServerEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
    {
        Constants.getLogger().info("handling join");

        if (event.player instanceof EntityPlayerMP)
        {
            CommonEvents.handleJoin((EntityPlayerMP) event.player, CommonClass.isInternalServer(), false);
        }
    }
}
