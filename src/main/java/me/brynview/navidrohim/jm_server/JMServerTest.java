package me.brynview.navidrohim.jm_server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jm_server.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jm_server.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jm_server.items.DebugItem;
import me.brynview.navidrohim.jm_server.common.payloads.RegisterUserPayload;
import me.brynview.navidrohim.jm_server.common.utils.WaypointIOInterface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class JMServerTest implements ModInitializer {

    public static final String MODID = "jm_server";
    public static final String VERSION = "0.0.7";
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
        PayloadTypeRegistry.playS2C().register(WaypointActionPayload.ID, WaypointActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, this::HandleWaypointAction);
    }

    private void HandleWaypointAction(WaypointActionPayload waypointActionPayload, ServerPlayNetworking.Context context) {
        String command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();

        switch (command) {
            case "delete" -> {
                boolean result = WaypointIOInterface.deleteWaypoint(arguments.getFirst().getAsString());
                if (result) {
                    context.player().sendMessageToClient(Text.of(Text.translatable("message.jm_server.deletion_success")), true);
                } else {
                    context.player().sendMessageToClient(Text.translatable("message.jm_server.deletion_failure"), true);
                }
            }

            case "create" -> {
                JsonObject jsonCreationData = arguments.getFirst().getAsJsonObject();
                boolean waypointCreationSuccess = WaypointIOInterface.createWaypoint(jsonCreationData, context.player().getUuid());

                if (waypointCreationSuccess) {
                    context.player().sendMessageToClient(Text.translatable("message.jm_server.creation_success"), true);
                } else {
                    context.player().sendMessageToClient(Text.translatable("message.jm_server.creation_failure"), false);
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
                    ServerPlayNetworking.send(context.player(), waypointPayloadOutbound);


                } catch (IOException ioe) {
                    JMServerTest.LOGGER.error(ioe.getMessage());
                }
            }
        }
    }
}
