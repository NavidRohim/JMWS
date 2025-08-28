package me.brynview.navidrohim.jmws.server.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.WaypointPayloadCommand;
import me.brynview.navidrohim.jmws.common.helper.CommandHelper;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper.sendUserMessage;

public class ServerPacketHandler {

    private static boolean serverEnabledJMWS() {
        return ServerConfig.getConfig().jmwsEnabled && (ServerConfig.getConfig().groupsEnabled || ServerConfig.getConfig().waypointsEnabled);
    }

    public static void sendUserSync(ServerPlayer player, boolean sendAlert)
    {
        if (ServerConfig.getConfig().serverEnabled())
        {
            try {
                List<String> playerWaypoints = JMWSServerIO.getFileObjects(player.getUUID(), JMWSServerIO.FetchType.WAYPOINT);
                List<String> playerGroups = JMWSServerIO.getFileObjects(player.getUUID(), JMWSServerIO.FetchType.GROUP);

                HashMap<String, String> jsonWaypointPayloadArray = new HashMap<>();
                HashMap<String, String> jsonGroupPayloadArray = new HashMap<>();

                for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                    String waypointFilename = playerWaypoints.get(i);
                    String jsonWaypointFileString = Files.readString(Paths.get(waypointFilename));
                    jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);
                }

                for (int ix = 0 ; ix < playerGroups.size() ; ix++) {
                    String groupFilename = playerGroups.get(ix);
                    String jsonGroupFileString = Files.readString(Paths.get(groupFilename));
                    jsonGroupPayloadArray.put(String.valueOf(ix), jsonGroupFileString);
                }
                String jsonData = CommandHelper.makeSyncRequestResponseJson(jsonWaypointPayloadArray, jsonGroupPayloadArray, sendAlert);

                // 2000000 was (jsonData.getBytes().length >= SERVER_CONFIG.serverConfiguration.serverPacketLimit())
                if (jsonData.getBytes().length >= 2000000) { // packet size limit, I tried to reach this limit, but I got nowhere near.
                    sendUserMessage(player, "error.jmws.error_packet_size", false, true);
                } else {
                    JMWSActionPayload waypointPayloadOutbound = new JMWSActionPayload(jsonData);
                    Dispatcher.sendToClient(waypointPayloadOutbound, player);
                }
            } catch (IOException ioe) {
                Constants.getLogger().error(ioe.getMessage());
            }
        }
    }
    public static void handleIncomingActionCommand(PacketContext<JMWSActionPayload> Context, ServerPlayer player) {
        JMWSActionPayload waypointActionPayload = Context.message();
        WaypointPayloadCommand command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();

        switch (command) {

            // Following two cases are for deleting waypoints and groups
            case WaypointPayloadCommand.COMMON_DELETE_GROUP -> {

                String playerUUID = arguments.getFirst().getAsString();
                String groupUniversalIdentifier = arguments.get(1).getAsString();
                String groupGUID = arguments.get(2).getAsString();
                boolean silent = arguments.get(3).getAsBoolean();
                boolean deleteAllWaypointsInGroup = arguments.get(4).getAsBoolean();
                boolean removeGroupItself = arguments.get(5).getAsBoolean();
                boolean deleteAllObjects = arguments.getLast().getAsBoolean();

                boolean result;

                String fileName = JMWSServerIO.getGroupFilename(player.getUUID(), groupUniversalIdentifier);

                if (deleteAllWaypointsInGroup) {
                    result = JMWSServerIO.removeAllWaypointsFromGroup(player.getUUID(), groupGUID);
                    if (!removeGroupItself && result)
                    {
                        sendUserMessage(player, "message.jmws.deleted_waypoints_in_group", true, false);
                        return;
                    }
                }

                if (!deleteAllObjects) {
                    result = CommonHelper.deleteFile(fileName);
                } else {
                    result = JMWSServerIO.deleteAllUserObjects(player.getUUID(), JMWSServerIO.FetchType.GROUP);
                }

                if (!silent) {
                    if (result) {
                        sendUserMessage(player, "message.jmws.deletion_group_success", true, false);
                    } else {
                        sendUserMessage(player, "message.jmws.deletion_group_failure", true, true);
                    }
                }
            }

            case WaypointPayloadCommand.COMMON_DELETE_WAYPOINT -> {
                String fileName = arguments.getFirst().getAsString().stripTrailing();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean deleteAll = arguments.getLast().getAsBoolean();
                boolean result;

                if (!deleteAll) {
                    result = CommonHelper.deleteFile(fileName);
                } else {
                    result = JMWSServerIO.deleteAllUserObjects(player.getUUID(), JMWSServerIO.FetchType.WAYPOINT);
                }

                if (!silent) {
                    if (result) {
                        sendUserMessage(player, "message.jmws.deletion_success", true, false);
                    } else {
                        sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                    }
                }
            }

            // Following two cases regarding creating groups and waypoints
            case WaypointPayloadCommand.SERVER_CREATE -> {
                boolean isUpdateFromCreation = arguments.get(2).getAsBoolean();

                if (serverEnabledJMWS() && (ServerConfig.getConfig().waypointsEnabled || isUpdateFromCreation)) {
                    JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean waypointCreationSuccess = JMWSServerIO.createWaypoint(jsonCreationData, player.getUUID());

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

            case WaypointPayloadCommand.SERVER_CREATE_GROUP -> {
                boolean isUpdateFromCreation = arguments.get(2).getAsBoolean();

                if (serverEnabledJMWS() && ( ServerConfig.getConfig().groupsEnabled || isUpdateFromCreation)) {
                    JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean waypointCreationSuccess = JMWSServerIO.createGroup(jsonCreationData, player.getUUID());

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

            // was "request"
            case WaypointPayloadCommand.SYNC -> {
                boolean sendAlert = arguments.getLast().getAsBoolean();
                sendUserSync(player, sendAlert);
            }

            default -> Constants.getLogger().warn("Unknown packet command -> {}", command);
        }
    }
}
