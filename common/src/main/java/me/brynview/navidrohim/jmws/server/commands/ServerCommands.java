package me.brynview.navidrohim.jmws.server.commands;

import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class ServerCommands {

    public static int share(ServerPlayer sender, ServerPlayer player, String waypointID, ObjectType objectType) {
        if (ServerConfig.serverConfig.sharingEnabled)
        {
            if (sender.equals(player) || (CommonClass.isInternalServer() && player.level().getServer().getSingleplayerProfile().getId().equals(player.getUUID())))
            {
                PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.cannot_share", true, false);
            } else {
                HashMap<String, Path> userObjs = JMWSServerIO.getNameHashmapLookup(sender.getUUID(), objectType);
                Path specifiedObj = userObjs.get(waypointID);
                if (specifiedObj != null)
                {
                    ServerObject objIns = JMWSServerIO.getObjectFromFile(specifiedObj, sender.getUUID(), objectType);
                    if (!objIns.syncing.isGlobal())
                    {
                        objIns.share(sender, player);
                    } else {
                        PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.cannot_share_global", true, MessageType.WARNING);
                    }
                }
                else {
                    PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_matching_object", true, MessageType.FAILURE);
                }
            }
        } else {
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_server_sharing", true, MessageType.FAILURE);
        }
        return 1;
    }

    public static int removeShare(ServerPlayer sender, String waypointID, ObjectType objectType) {
        HashMap<String, Path> userObjPaths = JMWSServerIO.getNameHashmapLookup(sender.getUUID(), objectType);
        Path specifiedObj = userObjPaths.get(waypointID);

        if (specifiedObj != null)
        {
            ServerObject objIns = JMWSServerIO.getObjectFromFile(specifiedObj, sender.getUUID(), objectType);
            objIns.stopSharing();
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.stopped_sharing", true, MessageType.NEUTRAL);
        }
        else {
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_matching_object", true, MessageType.FAILURE);
        }

        return 1;
    }

    public static int globalShare(String objectName, ServerPlayer player, ObjectType objectType, boolean make)
    {
        return globalShare(objectName, player.getUUID(), objectType, make);
    }

    public static int globalShare(String objectName, UUID uuid, ObjectType objectType, boolean make)
    {
        HashMap<String, Path> userObjs = JMWSServerIO.getNameHashmapLookup(uuid, objectType);
        @Nullable Path specifiedObject = userObjs.get(objectName);
        ServerObject globalObject = JMWSServerIO.getObjectFromFile(specifiedObject, uuid, objectType);

        if (specifiedObject != null && globalObject != null)
        {
            if (make)
            {
                if (!globalObject.syncing.isGlobal())
                {
                    globalObject.makeGlobal();
                    PlayerNetworkingHelper.sendUserMessage(uuid, "global.jmws.made_global", true, MessageType.NEUTRAL);
                } else {
                    PlayerNetworkingHelper.sendUserMessage(uuid, "global.jmws.already_global", true, MessageType.WARNING);
                }
            } else {
                if (globalObject.syncing.isGlobal())
                {
                    globalObject.removeGlobal();
                    PlayerNetworkingHelper.sendUserMessage(uuid, "global.jmws.remove_global", true, MessageType.NEUTRAL);
                } else {
                    PlayerNetworkingHelper.sendUserMessage(uuid, "global.jmws.not_global", true, MessageType.NEUTRAL);
                }
            }
        } else {
            PlayerNetworkingHelper.sendUserMessage(uuid, "sharing.jmws.no_matching_object", true, MessageType.FAILURE);
        }
        return 1;
    }

    public static int removeGlobalFromBadOp(String waypointID, ObjectType objectType, ServerPlayer senderPlayer)
    {
        HashMap<String, ServerObject> userObjs = ServerDispatcher.getInactiveOpUserGlobalObjects(objectType);
        @Nullable ServerObject specifiedObject = userObjs.get(waypointID);

        if (specifiedObject != null)
        {
            if (specifiedObject.syncing.isGlobal())
            {
                specifiedObject.removeGlobal();
                PlayerNetworkingHelper.sendUserMessage(senderPlayer, "global.jmws.remove_global", true, MessageType.NEUTRAL);
            } else {
                PlayerNetworkingHelper.sendUserMessage(senderPlayer, "global.jmws.not_global", true, MessageType.NEUTRAL);
            }
        } else {
            PlayerNetworkingHelper.sendUserMessage(senderPlayer, "sharing.jmws.no_matching_object", true, MessageType.FAILURE);
        }

        return 1;
    }
}
