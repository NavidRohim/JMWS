package me.brynview.navidrohim.jmws.server.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.objects.SavedObject;
import me.brynview.navidrohim.jmws.common.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import net.minecraft.server.level.ServerPlayer;

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

    public static void sendUserSync(ServerPlayer player, boolean sendAlert, boolean isDeathSync)
    {
        if (ServerConfig.getConfig().serverEnabled())
        {
            UUID playerUUID = player.getUUID();
            try {
                List<Path> playerWaypoints = JMWSServerIO.getObjectsForUser(playerUUID, FetchType.WAYPOINT);
                List<Path> playerGroups = JMWSServerIO.getObjectsForUser(playerUUID, FetchType.GROUP);

                HashMap<String, String> jsonWaypointPayloadArray = new HashMap<>();
                HashMap<String, String> jsonGroupPayloadArray = new HashMap<>();

                for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                    Path waypointFilename = playerWaypoints.get(i);
                    String jsonWaypointFileString = Files.readString(waypointFilename);
                    jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);

                    if (JMWSServerIO.transition(playerWaypoints.get(i), FetchType.WAYPOINT, playerUUID))
                    {
                        Constants.getLogger().error("Could not translate %s %s to new system path.".formatted(FetchType.WAYPOINT, playerWaypoints.get(i)));
                    }
                }

                for (int ix = 0 ; ix < playerGroups.size() ; ix++) {
                    Path groupFilename = playerGroups.get(ix);
                    String jsonGroupFileString = Files.readString(groupFilename);
                    jsonGroupPayloadArray.put(String.valueOf(ix), jsonGroupFileString);

                    if (JMWSServerIO.transition(playerGroups.get(ix), FetchType.GROUP, playerUUID))
                    {
                        Constants.getLogger().error("Could not translate %s %s to new system path.".formatted(FetchType.GROUP, playerWaypoints.get(ix)));
                    }
                }

                String jsonData = CommandFactory.makeSyncRequestResponseJson(jsonWaypointPayloadArray, jsonGroupPayloadArray, sendAlert, isDeathSync);

                // 2000000 was (jsonData.getBytes().length >= SERVER_CONFIG.serverConfiguration.serverPacketLimit())
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

                if (deleteAllWaypointsInGroup)
                {
                    result = JMWSServerIO.removeAllWaypointsFromGroup(playerUUID, groupGUID);
                    if (!removeGroupItself && result)
                    {
                        sendUserMessage(player, "message.jmws.deleted_waypoints_in_group", true, false);
                        return;
                    }
                }

                if (!deleteAllObjects) {
                    result = JMWSServerIO.deleteObject(groupUniversalIdentifier, playerUUID, FetchType.GROUP);
                } else {
                    result = JMWSServerIO.deleteAllUserObjects(playerUUID, FetchType.GROUP);
                }

                if (!silent) {
                    if (result) {
                        sendUserMessage(player, "message.jmws.deletion_group_success", true, false);
                    } else {
                        sendUserMessage(player, "message.jmws.deletion_group_failure", true, true);
                    }
                }
            }

            case CommandFactory.Commands.COMMON_DELETE_WAYPOINT -> {
                String waypointIdentifier = arguments.getFirst().getAsString().stripTrailing();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean deleteAll = arguments.getLast().getAsBoolean();
                boolean result;

                SavedWaypoint waypoint = JMWSServerIO.getWaypointFromFile(waypointIdentifier, playerUUID);

                if (waypoint != null)
                {
                    waypoint.removeWaypointFromUsers();

                    if (!deleteAll) {
                        result = waypoint.delete();
                    } else {
                        result = JMWSServerIO.deleteAllUserObjects(playerUUID, FetchType.WAYPOINT);
                    }

                    if (!silent) {
                        if (result) {
                            sendUserMessage(player, "message.jmws.deletion_success", true, false);
                        } else {
                            sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                        }
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

            case UPDATE ->
            {
                String objectIdentifier = arguments.getFirst().getAsString();
                FetchType modifyingType = FetchType.valueOf(arguments.get(1).getAsString());
                Path objectPath = JMWSServerIO.Utils.getNewObjectFilename(playerUUID, objectIdentifier, modifyingType);
                String objectData = arguments.getLast().getAsString();

                SavedObject obj = JMWSServerIO.getObjectFromDisk(objectIdentifier, playerUUID, modifyingType.getObjectClass(), modifyingType);
                if (obj != null)
                {
                    obj.update(objectData);
                }

            }

            // was "request"
            case CommandFactory.Commands.SYNC -> {
                if (player instanceof ServerPlayer)
                {
                    boolean sendAlert = arguments.get(2).getAsBoolean();
                    boolean isDeathSync = arguments.getLast().getAsBoolean();
                    sendUserSync(player, sendAlert, isDeathSync);
                }
            }

            case CommandFactory.Commands.USER_ALREADY_PROCESSING_SHARE, CommandFactory.Commands.REJECT_SHARE ->
            {
                echoPacket(Context);
            }

            case CommandFactory.Commands.AFFIRM_SHARE ->
            {
                UUID ownerUUID = UUID.fromString(arguments.getFirst().getAsString());
                String objectIdentifier = arguments.get(1).getAsString();
                SavedWaypoint sharedWp = JMWSServerIO.getWaypointFromFile(objectIdentifier, ownerUUID);

                // Add waypoint ID to users share list.
                try (UserSharingFile usf = new UserSharingFile(playerUUID))
                {
                    usf.addToShared(objectIdentifier);
                }

                // Add users UUID to waypoints share list.
                if (sharedWp != null)
                {
                    sharedWp.syncing.addUserToShare(playerUUID);
                } else {
                    sendUserMessage(player, "sharing.jmws.object_no_longer_exists", true, true);
                }


            }

            default -> Constants.getLogger().warn("Unknown packet command -> {}", command);
        }
    }

    private static void echoPacket(PacketContext<JMWSActionPayload> Context) {
        UUID forUser = UUID.fromString(Context.message().arguments().getFirst().getAsString());
        Dispatcher.sendToClient(Context.message(), Context.sender().server.getPlayerList().getPlayer(forUser));
    }
}
