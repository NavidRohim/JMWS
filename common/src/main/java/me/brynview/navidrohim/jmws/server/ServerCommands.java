package me.brynview.navidrohim.jmws.server;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.share.ShareRequest;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.HashMap;

public class ServerCommands {
    public static int share(ServerPlayer sender, ServerPlayer player, String waypointID) {
        if (ServerConfig.serverConfig.sharingEnabled)
        {
            if (sender.equals(player))
            {
                PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.cannot_share", true, false);
            } else {
                HashMap<String, Path> userObjs = JMWSServerIO.getNameHashmapLookup(sender.getUUID(), FetchType.WAYPOINT);
                Path specifiedObj = userObjs.get(waypointID);
                if (specifiedObj != null)
                {
                    String waypointStringJson = JMWSServerIO.getObjectDataFromDisk(specifiedObj, false).toString();
                    Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(waypointStringJson, player.getUUID(), sender.getUUID(), ShareRequest.Direction.FOR_CLIENT)), player); // Send share request to player

                    // Send information of the share to the sender. This is needed because this command is server-side only and the client will have no knowledge of the shared obj.
                    Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(waypointStringJson, player.getUUID(), sender.getUUID(), ShareRequest.Direction.FOR_HOST)), sender);
                }
                else {
                    PlayerNetworkingHelper.sendUserMessage(player, "sharing.jmws.no_matching_object", true, JMWSMessageType.FAILURE);
                }
            }
        } else {
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_server_sharing", true, JMWSMessageType.FAILURE);
        }
        return 1;
    }
}
