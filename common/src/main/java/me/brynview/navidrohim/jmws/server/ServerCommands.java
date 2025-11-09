package me.brynview.navidrohim.jmws.server;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.share.ShareRequest;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.server.level.ServerPlayer;

public class ServerCommands {
    public static int share(ServerPlayer sender, ServerPlayer player, String waypointID) {
        try {
            String waypointStringJson = JMWSServerIO.getWaypointFromUniqueIdentifier(waypointID, player.getUUID());
            Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(waypointStringJson, player.getUUID(), sender.getUUID(), ShareRequest.Direction.FOR_CLIENT)), player); // Send share request to player

            // Send information of the share to the sender. This is needed because this command is server-side only and the client will have no knowledge of the shared obj.
            Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(waypointStringJson, player.getUUID(), sender.getUUID(), ShareRequest.Direction.FOR_HOST)), sender);
        } catch (Exception e)
        {
            Constants.getLogger().info(e.toString());
        }
        return 1;
    }
}
