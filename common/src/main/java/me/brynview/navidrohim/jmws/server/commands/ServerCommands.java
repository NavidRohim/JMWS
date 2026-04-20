package me.brynview.navidrohim.jmws.server.commands;

import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
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

    public static int removeShare(ServerPlayer sender, String waypointID, ObjectType objectType) {
        HashMap<String, Path> userObjPaths = JMWSServerIO.getNameHashmapLookup(sender.getUUID(), objectType);
        Path specifiedObj = userObjPaths.get(waypointID);

        if (specifiedObj != null)
        {
            ServerObject objIns = JMWSServerIO.getObjectFromFile(specifiedObj, sender.getUUID(), objectType);
            objIns.stopSharingWithAll();
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.stopped_sharing", true, MessageType.NEUTRAL);
        }
        else {
            PlayerNetworkingHelper.sendUserMessage(sender, "sharing.jmws.no_matching_object", true, MessageType.FAILURE);
        }

        return 1;
    }
}
