package me.brynview.navidrohim.jmws.server.commands;

import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.platform.Services;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.server.io.JMWSServerIO.getAllInitialisedGlobalObjects;

public class ServerCommands {

    public static int share(UUID sender, UUID player, String waypointID, ObjectType objectType) {
        if (ServerConfig.serverConfig.sharingEnabled)
        {
            if (sender.equals(player))
            {
                PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.cannot_share", true, false);
            } else {
                HashMap<String, Path> userObjs = JMWSServerIO.getNameHashmapLookup(sender, objectType);
                Path specifiedObj = userObjs.get(waypointID);
                if (specifiedObj != null)
                {
                    ServerObject objIns = JMWSServerIO.getObjectFromFile(specifiedObj, sender, objectType);
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

    public static int removeShare(UUID sender, String waypointID, ObjectType objectType) {
        HashMap<String, Path> userObjPaths = JMWSServerIO.getNameHashmapLookup(sender, objectType);
        Path specifiedObj = userObjPaths.get(waypointID);

        if (specifiedObj != null)
        {
            ServerObject objIns = JMWSServerIO.getObjectFromFile(specifiedObj, sender, objectType);
            objIns.stopSharing();
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.stopped_sharing", true, MessageType.NEUTRAL);
        }
        else {
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_matching_object", true, MessageType.FAILURE);
        }

        return 1;
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

    public static int removeGlobalFromBadOp(String waypointID, ObjectType objectType, UUID senderPlayer)
    {
        HashMap<String, ServerObject> userObjs = getInactiveOpUserGlobalObjects(objectType);
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

    public static HashMap<String, ServerObject> getInactiveOpUserGlobalObjects(ObjectType objectType)
    {
        List<ServerObject> objs = getAllInitialisedGlobalObjects(objectType);
        HashMap<String, ServerObject> stringServerObjectHashMap = new HashMap<>();

        objs.forEach(obj -> {
            if (!Services.PLATFORM.isOperator(obj.getOwnerUUID()))
            {
                stringServerObjectHashMap.put(obj.getObjectNonDuplicateIdentifier(), obj);
            }
        });
        return stringServerObjectHashMap;
    }
}
