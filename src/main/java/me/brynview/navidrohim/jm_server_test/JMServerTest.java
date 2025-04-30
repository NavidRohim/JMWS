package me.brynview.navidrohim.jm_server_test;

import me.brynview.navidrohim.jm_server_test.items.DebugItem;
import me.brynview.navidrohim.jm_server_test.client.plugin.SavedWaypoint;
import me.brynview.navidrohim.jm_server_test.client.plugin.WaypointPayload;
import me.brynview.navidrohim.jm_server_test.util.WaypointIOInterface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JMServerTest implements ModInitializer {

    public static final String MODID = "jm_server_test";
    public static final String VERSION = "0.0.1-POC";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    @Override
    public void onInitialize() {

        DebugItem.initialize();

        PayloadTypeRegistry.playC2S().register(WaypointPayload.ID, WaypointPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(WaypointPayload.ID, (payload, context) -> {
            SavedWaypoint savedWaypoint = payload.getSavedWaypoint();
            String waypointFilePath = WaypointIOInterface.getWaypointFilename(savedWaypoint);
            Path waypointPathObj = Paths.get(waypointFilePath);

            try {
                Files.createFile(waypointPathObj);
                FileWriter waypointFileWriter = new FileWriter(waypointFilePath);
                waypointFileWriter.write(savedWaypoint.getRawPacketData());
                waypointFileWriter.close();

            } catch (IOException e) {
                JMServerTest.LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
            JMServerTest.LOGGER.info("server recv waypoint creation: " + savedWaypoint.getRawJsonData().toString());

            });
    }
}
