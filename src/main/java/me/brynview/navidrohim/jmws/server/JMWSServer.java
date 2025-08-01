package me.brynview.navidrohim.jmws.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import me.brynview.navidrohim.jmws.JMWS;
import me.brynview.navidrohim.jmws.common.io.CommonIO;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPacket;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePacket;
import me.brynview.navidrohim.jmws.server.config.JMWSServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.common.enums.WaypointPayloadCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class JMWSServer implements DedicatedServerModInitializer {

    public static JMWSServerConfig SERVER_CONFIG;

    public static void _createServerResources() {
        new File("./jmws").mkdir();
        new File("./jmws/groups").mkdir();
    }

    @Override
    public void onInitializeServer() {

        // Item
        _createServerResources();

        SERVER_CONFIG = JMWSServerConfig.createAndLoad();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (server.isDedicated()) {
                ServerPlayNetworking.registerGlobalReceiver(JMWSActionPacket.TYPE, this::HandleWaypointAction);
                ServerPlayNetworking.registerGlobalReceiver(JMWSHandshakePacket.TYPE, this::HandshakeHandler);
            }
        });
    }

    private boolean serverEnabledJMWS() {
        return SERVER_CONFIG.serverConfiguration.serverEnabled() && (SERVER_CONFIG.serverConfiguration.serverGroupsEnabled() || SERVER_CONFIG.serverConfiguration.serverWaypointsEnabled());
    }

    private void HandshakeHandler(JMWSHandshakePacket handshakePayload, ServerPlayerEntity player, PacketSender packetSender) {
        ServerPlayNetworking.send(player, handshakePayload);
    }

    private void sendUserMessage(ServerPlayerEntity player, String messageKey, Boolean overlay, boolean isError) {
        JMWSActionPacket messagePayload = new JMWSActionPacket(JsonStaticHelper.makeClientAlertRequestJson(messageKey, overlay, isError));
        ServerPlayNetworking.send(player, messagePayload);
    }

    private void HandleWaypointAction(JMWSActionPacket waypointActionPayload, ServerPlayerEntity player, PacketSender packetSender) {
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
                boolean deleteAllObjects = arguments.getLast().getAsBoolean();

                boolean result;

                String fileName = JMWSServerIO.getGroupFilename(UUID.fromString(playerUUID.toString()), groupUniversalIdentifier);

                if (deleteAllWaypointsInGroup) { JMWSServerIO.removeAllWaypointsFromGroup(player.getUuid(), groupGUID);}

                if (!deleteAllObjects) {
                    result = CommonIO.deleteFile(fileName);
                } else {
                    result = JMWSServerIO.deleteAllUserObjects(player.getUuid(), JMWSServerIO.FetchType.GROUP);
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
                    result = CommonIO.deleteFile(fileName);
                } else {
                    result = JMWSServerIO.deleteAllUserObjects(player.getUuid(), JMWSServerIO.FetchType.WAYPOINT);
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
                if (serverEnabledJMWS() && (SERVER_CONFIG.serverConfiguration.serverWaypointsEnabled() || isUpdateFromCreation)) {

                    JMWS.info(arguments.getFirst().getAsString());
                    JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean waypointCreationSuccess = JMWSServerIO.createWaypoint(jsonCreationData, player.getUuid());

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
                if (serverEnabledJMWS() && (SERVER_CONFIG.serverConfiguration.serverGroupsEnabled() || isUpdateFromCreation)) {
                    JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean waypointCreationSuccess = JMWSServerIO.createGroup(jsonCreationData, player.getUuid());

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
                try {
                    List<String> playerWaypoints = JMWSServerIO.getFileObjects(player.getUuid(), JMWSServerIO.FetchType.WAYPOINT);
                    List<String> playerGroups = JMWSServerIO.getFileObjects(player.getUuid(), JMWSServerIO.FetchType.GROUP);

                    boolean sendAlert = arguments.getLast().getAsBoolean();
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
                    String jsonData = JsonStaticHelper.makeSyncRequestResponseJson(jsonWaypointPayloadArray, jsonGroupPayloadArray, sendAlert);

                    if (jsonData.getBytes().length >= SERVER_CONFIG.serverConfiguration.serverPacketLimit()) { // packet size limit, I tried to reach this limit but I got nowhere near.
                        sendUserMessage(player, "error.jmws.error_packet_size", false, true);
                    } else {
                        JMWSActionPacket waypointPayloadOutbound = new JMWSActionPacket(jsonData);
                        ServerPlayNetworking.send(player, waypointPayloadOutbound);
                    }
                } catch (IOException ioe) {
                    JMWS.LOGGER.error(ioe.getMessage());
                }
            }

            default -> JMWS.LOGGER.warn("Unknown packet command -> " + command);
        }
    }
}
