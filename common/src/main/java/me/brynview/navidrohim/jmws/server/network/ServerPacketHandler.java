package me.brynview.navidrohim.jmws.server.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper.sendUserMessage;

public class ServerPacketHandler {

    private static boolean serverEnabledJMWS() {
        return ServerConfig.getConfig().jmwsEnabled && (ServerConfig.getConfig().groupsEnabled || ServerConfig.getConfig().waypointsEnabled);
    }

    public static void sendUserSync(ServerPlayer player, boolean sendAlert, boolean isDeathSync, boolean onlySyncShared)
    {
        if (ServerConfig.getConfig().serverEnabled())
        {
            UUID playerUUID = player.getUUID();
            try {
                int lastIterWp = 0;
                int lastIterGp = 0;

                List<Path> playerWaypoints = JMWSServerIO.getObjectPathsForUser(playerUUID, ObjectType.WAYPOINT);
                List<Path> playerGroups = JMWSServerIO.getObjectPathsForUser(playerUUID, ObjectType.GROUP);

                HashMap<String, String> jsonWaypointPayloadArray = new HashMap<>();
                HashMap<String, String> jsonGroupPayloadArray = new HashMap<>();

                // User-defined objects
                if (!onlySyncShared)
                {
                    for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                        Path waypointFilename = playerWaypoints.get(i);
                        String jsonWaypointFileString = Files.readString(waypointFilename);
                        jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);

                        if (JMWSServerIO.transition(playerWaypoints.get(i), ObjectType.WAYPOINT, playerUUID))
                        {
                            Constants.getLogger().error("Could not translate %s %s to new system path.".formatted(ObjectType.WAYPOINT, playerWaypoints.get(i)));
                        }
                        lastIterWp = i;
                    }

                    for (int ix = 0 ; ix < playerGroups.size() ; ix++) {
                        Path groupFilename = playerGroups.get(ix);
                        String jsonGroupFileString = Files.readString(groupFilename);
                        jsonGroupPayloadArray.put(String.valueOf(ix), jsonGroupFileString);

                        if (JMWSServerIO.transition(playerGroups.get(ix), ObjectType.GROUP, playerUUID))
                        {
                            Constants.getLogger().error("Could not translate %s %s to new system path.".formatted(ObjectType.GROUP, playerWaypoints.get(ix)));
                        }
                        lastIterGp = ix;
                    }
                }

                for (Path globalWpPath : ServerWaypoint.getGlobalWaypoints())
                {
                    lastIterWp++;
                    jsonWaypointPayloadArray.put(String.valueOf(lastIterWp), Files.readString(globalWpPath));
                }

                for (Path globalGpPath : ServerGroup.getGlobalGroups())
                {
                    lastIterGp++;
                    jsonGroupPayloadArray.put(String.valueOf(lastIterGp), Files.readString(globalGpPath));
                }

                // Shared objects
                if (ServerConfig.serverConfig.sharingEnabled)
                {
                    try (UserSharingFile userSharingFile = new UserSharingFile(playerUUID))
                    {
                        for (String shared : userSharingFile.getSharedList(ObjectType.WAYPOINT))
                        {
                            lastIterWp++;
                            String waypointData = JMWSServerIO.getObjectFromUniqueIdentifier(shared, null, ObjectType.WAYPOINT);
                            if (waypointData != null)
                            {
                                jsonWaypointPayloadArray.put(String.valueOf(lastIterWp), waypointData);
                            }
                            else {
                                userSharingFile.removeFromShared(shared, ObjectType.WAYPOINT);
                            }
                        }
                        for (String sharedGpString : userSharingFile.getSharedList(ObjectType.GROUP))
                        {
                            lastIterGp++;
                            String groupData = JMWSServerIO.getObjectFromUniqueIdentifier(sharedGpString, null, ObjectType.GROUP);
                            if (groupData != null)
                            {
                                jsonGroupPayloadArray.put(String.valueOf(lastIterGp), groupData);
                            } else {
                                userSharingFile.removeFromShared(sharedGpString, ObjectType.GROUP);
                            }
                        }
                    }
                }

                String jsonData = CommandFactory.makeSyncRequestResponseJson(jsonWaypointPayloadArray, jsonGroupPayloadArray, sendAlert, isDeathSync);
                if (jsonData.getBytes().length >= 2000000) { // packet size limit, I tried to reach this limit, but I got nowhere near.
                    sendUserMessage(player, "error.jmws.error_packet_size", false, true);
                } else {
                    JMWSActionPayload waypointPayloadOutbound = new JMWSActionPayload(jsonData);
                    Dispatcher.sendToClient(waypointPayloadOutbound, player);
                }
            } catch (IOException ioe) {
                Constants.getLogger().error("Error on server when trying to process sync from %s ERROR: %s".formatted(player.getUUID(), ioe.toString()));
            }
        }
    }

    public static void handleIncomingActionCommand(PacketContext<JMWSActionPayload> Context, ServerPlayer player) {
        JMWSActionPayload waypointActionPayload = Context.message();
        CommandFactory.Commands command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();
        UUID playerUUID = player.getUUID();

        switch (command) {

            // Following two cases are for deleting waypoints and groups
            case CommandFactory.Commands.COMMON_DELETE_GROUP -> {

                String groupUniversalIdentifier = arguments.getFirst().getAsString();
                String groupGUID = arguments.get(1).getAsString();

                boolean silent = arguments.get(2).getAsBoolean();
                boolean deleteAllWaypointsInGroup = arguments.get(3).getAsBoolean();
                boolean removeGroupItself = arguments.get(4).getAsBoolean();
                boolean deleteAllObjects = arguments.getLast().getAsBoolean();

                boolean result;
                @Nullable ServerGroup group = JMWSServerIO.getObjectFromDisk(groupUniversalIdentifier, playerUUID, ObjectType.GROUP);
                if (group != null)
                {
                    if (deleteAllWaypointsInGroup)
                    {
                        result = group.deleteWaypoints();
                        if (!removeGroupItself && result)
                        {
                            sendUserMessage(player, "message.jmws.deleted_waypoints_in_group", true, false);
                            return;
                        }
                    }
                    if (group.syncing.isOwner(playerUUID))
                    {
                        group.stopSharing();
                        if (!deleteAllObjects) {
                            result = group.delete(false);
                        } else {
                            result = group.deleteAll();
                        }

                        if (!silent) {
                            if (result) {
                                sendUserMessage(player, "message.jmws.deletion_group_success", true, false);
                            } else {
                                sendUserMessage(player, "message.jmws.deletion_group_failure", true, true);
                            }
                        }
                    }
                } else if (deleteAllWaypointsInGroup)
                {
                    ServerGroup.deleteWaypoints(playerUUID, groupGUID);
                }
            }

            case CommandFactory.Commands.COMMON_DELETE_WAYPOINT -> {

                String waypointIdentifier = arguments.getFirst().getAsString().stripTrailing();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean deleteAll = arguments.getLast().getAsBoolean();
                boolean result;

                ServerWaypoint waypoint = JMWSServerIO.getWaypointFromUniqueIdentifier(waypointIdentifier, playerUUID);

                if (waypoint != null)
                {
                    if (waypoint.syncing.isOwner(playerUUID))
                    {
                        waypoint.stopSharing();

                        if (!deleteAll) {
                            result = waypoint.delete(false);
                        } else {
                            result = waypoint.deleteAll();
                        }

                        if (!silent) {
                            if (result) {
                                sendUserMessage(player, "message.jmws.deletion_success", true, false);
                            } else {
                                sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                            }
                        }
                    } else {
                        waypoint.stopSharing(playerUUID);
                        sendUserMessage(player, "sharing.jmws.no_longer_sharing", true, false);
                    }
                } else {
                    sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                }
            }

            // Following two cases regarding creating groups and waypoints
            case CommandFactory.Commands.SERVER_CREATE -> {
                if (serverEnabledJMWS() && (ServerConfig.getConfig().waypointsEnabled)) {
                    JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean waypointCreationSuccess = JMWSServerIO.createWaypoint(jsonCreationData, playerUUID);

                    if (!silent) {
                        if (waypointCreationSuccess) {
                            sendUserMessage(player, "message.jmws.creation_success", true, false);
                        } else {

                            sendUserMessage(player, "message.jmws.creation_failure", false, true);
                        }
                    }
                } else {
                    sendUserMessage(player, "message.jmws.server_disabled_waypoints", true, true);
                }
            }

            case CommandFactory.Commands.SERVER_CREATE_GROUP -> {
                boolean isUpdateFromCreation = arguments.get(2).getAsBoolean();

                if (serverEnabledJMWS() && ( ServerConfig.getConfig().groupsEnabled || isUpdateFromCreation)) {
                    JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean waypointCreationSuccess = JMWSServerIO.createGroup(jsonCreationData, playerUUID);

                    if (!silent) {
                        if (waypointCreationSuccess) {
                            sendUserMessage(player, "message.jmws.creation_group_success", true, false);
                        } else {
                            sendUserMessage(player, "message.jmws.creation_group_failure", false, true);

                        }
                    }
                } else {
                    sendUserMessage(player, "message.jmws.server_disabled_groups", true, true);
                }
            }

            case UPDATE -> // Bug here, after updating, the user share list is cleared
            {
                String objectIdentifier = arguments.getFirst().getAsString();
                ObjectType modifyingType = ObjectType.valueOf(arguments.get(1).getAsString());
                Path objectPath = JMWSServerIO.Utils.getNewObjectFilename(playerUUID, objectIdentifier, modifyingType);
                String objectData = arguments.getLast().getAsString();

                ServerObject obj = JMWSServerIO.getObjectFromDisk(objectIdentifier, playerUUID, modifyingType);
                if (obj != null)
                {
                    if (obj.syncing.isOwner(playerUUID))
                    {
                        obj.update(objectData, false);
                        obj.syncing.syncToUsers();

                        if (modifyingType == ObjectType.WAYPOINT)
                        {
                            PlayerNetworkingHelper.sendUserMessage(player, "message.jmws.modified_waypoint_success", true, JMWSMessageType.NEUTRAL);
                        } else {
                            PlayerNetworkingHelper.sendUserMessage(player, "message.jmws.modified_group_success", true, JMWSMessageType.NEUTRAL);
                        }
                    } else {
                        sendUserMessage(player, "sharing.jmws.local_only", true, JMWSMessageType.ONE_TIME_WARNING);
                    }
                }
            }

            // was "request"
            case CommandFactory.Commands.SYNC -> {
                if (player instanceof ServerPlayer)
                {
                    boolean sendAlert = arguments.get(2).getAsBoolean();
                    boolean isDeathSync = arguments.getLast().getAsBoolean();
                    sendUserSync(player, sendAlert, isDeathSync, false);
                }
            }

            case CommandFactory.Commands.USER_ALREADY_PROCESSING_SHARE, CommandFactory.Commands.REJECT_SHARE ->
            {
                UUID forUser = UUID.fromString(Context.message().arguments().getFirst().getAsString());
                Dispatcher.sendToClient(Context.message(), Context.sender().server.getPlayerList().getPlayer(forUser));
            }

            case CommandFactory.Commands.AFFIRM_SHARE ->
            {
                UUID ownerUUID = UUID.fromString(arguments.getFirst().getAsString());
                String objectIdentifier = arguments.get(1).getAsString();
                ObjectType objType = ObjectType.valueOf(arguments.getLast().getAsString());
                ServerObject sharedWp = JMWSServerIO.getObjectFromDisk(objectIdentifier, ownerUUID, objType);

                // Add waypoint ID to users share list.
                try (UserSharingFile usf = new UserSharingFile(playerUUID))
                {
                    usf.addToShared(objectIdentifier, objType);
                }

                // Add users UUID to waypoints share list.
                if (sharedWp != null)
                {
                    sharedWp.syncing.addUserToShare(playerUUID);
                    Dispatcher.sendToClient(waypointActionPayload, CommonClass.minecraftServerInstance.getPlayerList().getPlayer(ownerUUID));
                } else {
                    sendUserMessage(player, "sharing.jmws.object_no_longer_exists", true, true);
                }
            }

            default -> Constants.getLogger().warn("Unknown packet command -> {}", command);
        }
    }
}
