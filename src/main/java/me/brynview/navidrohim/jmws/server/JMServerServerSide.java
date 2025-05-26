package me.brynview.navidrohim.jmws.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.jmws.JMServer;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.io.JMWSIOInterface;
import me.brynview.navidrohim.jmws.common.enums.WaypointPayloadCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;


public class JMServerServerSide implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {

        // Item
        new File("./jmws").mkdir();
        new File("./jmws/groups").mkdir();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (server.isDedicated()) {
                ServerPlayNetworking.registerGlobalReceiver(JMWSActionPayload.ID, this::HandleWaypointAction);
                ServerPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, this::HandshakeHandler);
            }
        });
    }

    private void HandshakeHandler(HandshakePayload handshakePayload, ServerPlayNetworking.Context context) {
        ServerPlayNetworking.send(context.player(), handshakePayload);
    }

    private void sendUserMessage(ServerPlayerEntity player, String messageKey, Boolean overlay, boolean isError) {
        JMWSActionPayload messagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson(messageKey, overlay, isError));
        ServerPlayNetworking.send(player, messagePayload);
    }

    private void HandleWaypointAction(JMWSActionPayload waypointActionPayload, ServerPlayNetworking.Context context) {
        WaypointPayloadCommand command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();
        ServerPlayerEntity player = context.player();

        switch (command) {

            // was "delete"
            case WaypointPayloadCommand.COMMON_DELETE_WAYPOINT -> {
                boolean result = JMWSIOInterface.deleteFile(arguments.getFirst().getAsString());
                boolean silent = arguments.get(1).getAsBoolean();

                if (!silent) {
                    if (result) {
                        sendUserMessage(player, "message.jmws.deletion_success", true, false);
                    } else {
                        sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                    }
                }
            }

            // was "create"
            case WaypointPayloadCommand.SERVER_CREATE -> {
                JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean waypointCreationSuccess = JMWSIOInterface.createWaypoint(jsonCreationData, context.player().getUuid());

                if (!silent) {
                    if (waypointCreationSuccess) {
                        sendUserMessage(player, "message.jmws.creation_success", true, false);
                    } else {
                        sendUserMessage(player, "message.jmws.creation_failure", false, true);
                    }
                }
            }

            // was "request"
            case WaypointPayloadCommand.SYNC -> {
                try {
                    List<String> playerWaypoints = JMWSIOInterface.getFileObjects(player.getUuid(), JMWSIOInterface.FetchType.WAYPOINT);
                    List<String> playerGroups = JMWSIOInterface.getFileObjects(player.getUuid(), JMWSIOInterface.FetchType.GROUP);

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

                    if (jsonData.getBytes().length >= 2000000) { // packet size limit, I tried to reach this limit but I got nowhere near.
                        sendUserMessage(player, "error.jmws.error_packet_size", false, true);
                    } else {
                        JMWSActionPayload waypointPayloadOutbound = new JMWSActionPayload(jsonData);
                        ServerPlayNetworking.send(player, waypointPayloadOutbound);
                    }
                } catch (IOException ioe) {
                    JMServer.LOGGER.error(ioe.getMessage());
                }
            }

            case WaypointPayloadCommand.SERVER_CREATE_GROUP -> {
                JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean waypointCreationSuccess = JMWSIOInterface.createGroup(jsonCreationData, context.player().getUuid());

                if (!silent) {
                    if (waypointCreationSuccess) {
                        sendUserMessage(player, "message.jmws.creation_group_success", true, false);
                    } else {
                        sendUserMessage(player, "message.jmws.creation_group_failure", false, true);

                    }
                }
            }

            case WaypointPayloadCommand.COMMON_DELETE_GROUP -> {
                boolean result = JMWSIOInterface.deleteFile(arguments.getFirst().getAsString());
                boolean silent = arguments.get(1).getAsBoolean();

                if (!silent) {
                    if (result) {
                        sendUserMessage(player, "message.jmws.deletion_group_success", true, false);
                    } else {
                        sendUserMessage(player, "message.jmws.deletion_group_failure", true, true);
                    }
                }
            }

            default -> JMServer.LOGGER.warn("Unknown packet command -> " + command);
        }
    }
}
