package me.navidrohim.jmws.server.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.enums.WaypointPayloadCommand;
import me.navidrohim.jmws.helper.CommandHelper;
import me.navidrohim.jmws.helper.CommonHelper;
import me.navidrohim.jmws.payloads.JMWSActionPayload;
import me.navidrohim.jmws.server.config.ServerConfig;
import me.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.navidrohim.jmws.server.network.PlayerNetworkingHelper.sendUserMessage;

public class ServerPacketHandler {

    private static boolean serverEnabledJMWS() {
        return ServerConfig.getConfig().jmwsEnabled && (ServerConfig.getConfig().waypointsEnabled);
    }

    public static void handleIncomingActionCommand(PacketContext<JMWSActionPayload> Context, EntityPlayerMP player) {
        JMWSActionPayload waypointActionPayload = Context.message();
        WaypointPayloadCommand command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();

        switch (command) {

            // Following two cases are for deleting waypoints and groups

            case WaypointPayloadCommand.COMMON_DELETE_WAYPOINT -> {
                String fileName = arguments.get(0).getAsString().trim();
                boolean silent = arguments.get(1).getAsBoolean();
                boolean deleteAll = arguments.get(-1).getAsBoolean();
                boolean result;

                if (!deleteAll) {
                    result = CommonHelper.deleteFile(fileName);
                } else {
                    result = JMWSServerIO.deleteAllUserObjects(player.getUUID());
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
                    JsonObject jsonCreationData = new JsonParser().parse(arguments.get(0).getAsString()).getAsJsonObject();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean waypointCreationSuccess = JMWSServerIO.createWaypoint(jsonCreationData, player.getUniqueID());

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

            // was "request"
            case WaypointPayloadCommand.SYNC -> {
                try {
                    List<String> playerWaypoints = JMWSServerIO.getFileObjects(player.getUniqueID());

                    boolean sendAlert = arguments.get(-1).getAsBoolean();
                    HashMap<String, String> jsonWaypointPayloadArray = new HashMap<>();

                    for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                        String waypointFilename = playerWaypoints.get(i);
                        String jsonWaypointFileString = CommonHelper.readFromFile(waypointFilename);
                        jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);
                    }

                    String jsonData = CommandHelper.makeSyncRequestResponseJson(jsonWaypointPayloadArray, sendAlert);

                    // 2000000 was (jsonData.getBytes().length >= SERVER_CONFIG.serverConfiguration.serverPacketLimit())
                    if (jsonData.getBytes().length >= 2000000) { // packet size limit, I tried to reach this limit, but I got nowhere near.
                        sendUserMessage(player, "error.jmws.error_packet_size", false, true);
                    } else {
                        JMWSActionPayload waypointPayloadOutbound = new JMWSActionPayload(jsonData);
                        //Dispatcher.sendToClient(waypointPayloadOutbound, player);
                    }
                } catch (IOException ioe) {
                    Constants.getLogger().error(ioe.getMessage());
                }
            }

            default -> Constants.getLogger().warn("Unknown packet command -> {}", command);
        }
    }
}
