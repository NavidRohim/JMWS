package me.brynview.navidrohim.jmws.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.jmws.JMServer;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.utils.JMWSIOInterface;
import me.brynview.navidrohim.jmws.common.utils.WaypointPayloadCommand;
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
        File mainDir = new File("./jmws");
        boolean _a = mainDir.mkdir(); // doing this to shutup linter

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

    private void HandleWaypointAction(JMWSActionPayload waypointActionPayload, ServerPlayNetworking.Context context) {
        WaypointPayloadCommand command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();
        ServerPlayerEntity player = context.player();
        JMWSActionPayload alertMessagePayload;

        switch (command) {

            // was "delete"
            case WaypointPayloadCommand.COMMON_DELETE_WAYPOINT -> {
                boolean result = JMWSIOInterface.deleteFile(arguments.getFirst().getAsString());
                boolean silent = arguments.get(1).getAsBoolean();

                if (!silent) {
                    if (result) {
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.deletion_success", true));
                    } else {
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.deletion_failure", true));
                    }

                    ServerPlayNetworking.send(player, alertMessagePayload);
                }
            }

            // was "create"
            case WaypointPayloadCommand.SERVER_CREATE -> {
                JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean waypointCreationSuccess = JMWSIOInterface.createWaypoint(jsonCreationData, context.player().getUuid());

                if (!silent) {
                    if (waypointCreationSuccess) {
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.creation_success", true));
                    } else {
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.creation_failure", false));
                    }

                    ServerPlayNetworking.send(player, alertMessagePayload);
                }


            }

            // was "request"
            case WaypointPayloadCommand.SYNC -> {
                try {
                    List<String> playerWaypoints = JMWSIOInterface.getPlayerWaypointNames(player.getUuid());

                    HashMap<String, String> jsonWaypointPayloadArray = new HashMap<>();
                    HashMap<String, String> jsonGroupPayloadArray = new HashMap<>();

                    for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                        String waypointFilename = playerWaypoints.get(i);
                        String jsonWaypointFileString = Files.readString(Paths.get(waypointFilename));
                        jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);
                    }

                    String jsonData = JsonStaticHelper.makeSyncRequestResponseJson(jsonWaypointPayloadArray);
                    JMWSActionPayload waypointPayloadOutbound = new JMWSActionPayload(jsonData);
                    ServerPlayNetworking.send(player, waypointPayloadOutbound);

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
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.creation_group_success", true));
                    } else {
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.creation_group_failure", false));
                    }

                    ServerPlayNetworking.send(player, alertMessagePayload);
                }
            }

            case WaypointPayloadCommand.COMMON_DELETE_GROUP -> {
                boolean result = JMWSIOInterface.deleteFile(arguments.getFirst().getAsString());
                boolean silent = arguments.get(1).getAsBoolean();

                if (!silent) {
                    if (result) {
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.deletion_group_success", true));
                    } else {
                        alertMessagePayload = new JMWSActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.deletion_group_failure", true));
                    }

                    ServerPlayNetworking.send(player, alertMessagePayload);
                }
            }

            default -> JMServer.LOGGER.warn("Unknown packet command -> " + command);
        }
    }
}
