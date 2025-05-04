package me.brynview.navidrohim.jm_server_test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jm_server_test.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jm_server_test.items.DebugItem;
import me.brynview.navidrohim.jm_server_test.common.payloads.RegisterUserPayload;
import me.brynview.navidrohim.jm_server_test.server.handler.HandleWaypointCreationPacket;
import me.brynview.navidrohim.jm_server_test.common.payloads.UserWaypointPayload;
import me.brynview.navidrohim.jm_server_test.common.utils.WaypointIOInterface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JMServerTest implements ModInitializer {

    public static final String MODID = "jm_server_test";
    public static final String VERSION = "0.0.6";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    @Override
    public void onInitialize() {

        // Item
        File mainDir = new File("./jmserver");
        boolean _a = mainDir.mkdir(); // doing this to shutup linter

        DebugItem.initialize();

        // Packet registering (client)
        PayloadTypeRegistry.playC2S().register(RegisterUserPayload.ID, RegisterUserPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WaypointActionPayload.ID, WaypointActionPayload.CODEC);

        // Packet registering (server)
        PayloadTypeRegistry.playS2C().register(UserWaypointPayload.ID, UserWaypointPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RegisterUserPayload.ID, HandleWaypointCreationPacket::HandlePacket);
        ServerPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, this::HandleWaypointAction);
        ServerPlayConnectionEvents.JOIN.register(this::JoinListener);
    }

    private void HandleWaypointAction(WaypointActionPayload waypointActionPayload, ServerPlayNetworking.Context context) {
        JsonObject packetJson = waypointActionPayload.getJsonObject();
        String command = packetJson.asMap().get("command").getAsString();
        List<JsonElement> arguments = packetJson.asMap().get("arguments").getAsJsonArray().asList();

        JMServerTest.LOGGER.info("COMMAND: " + command);

        switch (command) {
            case "delete" -> {
                boolean result = WaypointIOInterface.deleteWaypoint(arguments.getFirst().getAsString());
                if (result) {
                    context.player().sendMessage(Text.of("Deleted successfully."));
                } else {
                    context.player().sendMessage(Text.of("Could not delete waypoint on server. Ignoring."));
                }
            }
            case "update" -> {
                Object filename = arguments;
                JMServerTest.LOGGER.info("WAYPOINT IDNE; " + filename);
            }
        }
    }

    public void JoinListener(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        try {
            List<String> playerWaypoints = WaypointIOInterface.getPlayerWaypointNames(serverPlayNetworkHandler.player.getUuid());
            JSONObject jsonWaypointPayloadArray = new JSONObject();

            for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                String waypointFilename = playerWaypoints.get(i);
                String jsonWaypointFileString = Files.readString(Paths.get(waypointFilename));
                jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);
            }

            UserWaypointPayload waypointPayloadOutbound = new UserWaypointPayload(jsonWaypointPayloadArray.toJSONString());
            ServerPlayNetworking.send(serverPlayNetworkHandler.player, waypointPayloadOutbound);


        } catch (IOException ioe) {
            JMServerTest.LOGGER.error(ioe.getMessage());
        }

    }
}
