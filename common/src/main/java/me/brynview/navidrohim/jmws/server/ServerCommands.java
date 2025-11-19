package me.brynview.navidrohim.jmws.server;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.share.ShareRequest;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;

public class ServerCommands {
    public static int share(ServerPlayer sender, ServerPlayer player, String waypointID, ObjectType objectType) {
        if (ServerConfig.serverConfig.sharingEnabled)
        {
            if (sender.equals(player) || (CommonClass.isInternalServer() && player.server.getSingleplayerProfile().getId().equals(player.getUUID())))
            {
                PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.cannot_share", true, false);
            } else {
                HashMap<String, Path> userObjs = JMWSServerIO.getNameHashmapLookup(sender.getUUID(), objectType);
                Path specifiedObj = userObjs.get(waypointID);
                if (specifiedObj != null)
                {
                    String waypointStringJson = JMWSServerIO.getObjectDataFromDisk(specifiedObj, false).toString();
                    Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(waypointStringJson, sender.getUUID(), player.getUUID(), ShareRequest.Direction.FOR_CLIENT, objectType)), player); // Send share request to player

                    // Send information of the share to the sender. This is needed because this command is server-side only and the client will have no knowledge of the shared obj.
                    Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeObjectShareRequestForUser(waypointStringJson, player.getUUID(), sender.getUUID(), ShareRequest.Direction.FOR_HOST, objectType)), sender);
                }
                else {
                    PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_matching_object", true, JMWSMessageType.FAILURE);
                }
            }
        } else {
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_server_sharing", true, JMWSMessageType.FAILURE);
        }
        return 1;
    }

    public static int globalShare(String objectName, ServerPlayer player, ObjectType objectType)
    {
        HashMap<String, Path> userObjs = JMWSServerIO.getNameHashmapLookup(player.getUUID(), objectType);
        @Nullable Path specifiedObject = userObjs.get(objectName);
        ServerObject globalObject = JMWSServerIO.getObjectFromFile(specifiedObject, player.getUUID(), objectType);

        if (specifiedObject != null && globalObject != null)
        {
            globalObject.makeGlobal();
            PlayerNetworkingHelper.sendUserMessage(player, "global.jmws.made_global", true, JMWSMessageType.NEUTRAL);
        } else {
            PlayerNetworkingHelper.sendUserMessage(player, "sharing.jmws.no_matching_object", true, JMWSMessageType.FAILURE);
        }
        return 1;
    }
}
