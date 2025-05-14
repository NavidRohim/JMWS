package me.brynview.navidrohim.jmws.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.JMServer;
import me.brynview.navidrohim.jmws.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jmws.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.utils.WaypointIOInterface;
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
                ServerPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, this::HandleWaypointAction);
            }
        });
    }

    private void HandleWaypointAction(WaypointActionPayload waypointActionPayload, ServerPlayNetworking.Context context) {
        String command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();
        ServerPlayerEntity player = context.player();
        WaypointActionPayload alertMessagePayload;

        switch (command) {
            case "delete" -> {
                boolean result = WaypointIOInterface.deleteWaypoint(arguments.getFirst().getAsString());
                boolean silent = arguments.get(1).getAsBoolean();

                if (!silent) {
                    if (result) {
                        alertMessagePayload = new WaypointActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.deletion_success", true));
                    } else {
                        alertMessagePayload = new WaypointActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.deletion_failure", true));
                    }

                    ServerPlayNetworking.send(player, alertMessagePayload);
                }
            }

            case "create" -> {
                JsonObject jsonCreationData = arguments.getFirst().getAsJsonObject();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean waypointCreationSuccess = WaypointIOInterface.createWaypoint(jsonCreationData, context.player().getUuid());

                if (!silent) {
                    if (waypointCreationSuccess) {
                        alertMessagePayload = new WaypointActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.creation_success", true));
                    } else {
                        alertMessagePayload = new WaypointActionPayload(JsonStaticHelper.makeClientAlertRequestJson("message.jmws.creation_failure", false));
                    }

                    ServerPlayNetworking.send(player, alertMessagePayload);
                }


            }
            case "request" -> {
                try {
                    List<String> playerWaypoints = WaypointIOInterface.getPlayerWaypointNames(context.player().getUuid());

                    HashMap<String, String> jsonWaypointPayloadArray = new HashMap<>();

                    for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                        String waypointFilename = playerWaypoints.get(i);
                        String jsonWaypointFileString = Files.readString(Paths.get(waypointFilename));
                        jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);
                    }

                    String jsonData = JsonStaticHelper.makeCreationRequestResponseJson(jsonWaypointPayloadArray);
                    WaypointActionPayload waypointPayloadOutbound = new WaypointActionPayload(jsonData);
                    ServerPlayNetworking.send(player, waypointPayloadOutbound);

                } catch (IOException ioe) {
                    JMServer.LOGGER.error(ioe.getMessage());
                }
            }
        }
    }
}
