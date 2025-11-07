package me.brynview.navidrohim.jmws.server;

import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.common.helper.CommandHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.server.level.ServerPlayer;

public class ServerCommands {
    public static int share(ServerPlayer player, String waypointID) {
        try {
            String waypointStringJson = JMWSServerIO.getWaypointFromUniqueIdentifier(waypointID, player.getUUID());
            Dispatcher.sendToClient(new JMWSActionPayload(CommandHelper.makeObjectShareRequestForUser(waypointStringJson, player.getUUID())), player);
        } catch (Exception e)
        {
            Constants.getLogger().info(e.toString());
        }
        return 1;
    }
}
