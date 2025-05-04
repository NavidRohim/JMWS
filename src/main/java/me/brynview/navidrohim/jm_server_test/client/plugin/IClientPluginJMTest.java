package me.brynview.navidrohim.jm_server_test.client.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.common.payloads.RegisterUserPayload;
import me.brynview.navidrohim.jm_server_test.common.SavedWaypoint;
import me.brynview.navidrohim.jm_server_test.common.payloads.UserWaypointPayload;
import me.brynview.navidrohim.jm_server_test.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jm_server_test.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jm_server_test.common.utils.WaypointIOInterface;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


@JourneyMapPlugin(apiVersion = "2.0.0")
public class IClientPluginJMTest implements IClientPlugin
{
    // API reference
    private IClientAPI jmAPI = null;
    private List<Waypoint> waypointIndex = new ArrayList<>();

    private static IClientPluginJMTest INSTANCE;

    public IClientPluginJMTest() {
        INSTANCE = this;
    }

    public static IClientPluginJMTest getInstance() {
        return INSTANCE;
    }

    private void deleteAction(String waypointFilename) {
        String jsonPacketData = JsonStaticHelper.makeDeleteJson(waypointFilename, false);
        WaypointActionPayload waypointActionPayload = new WaypointActionPayload(jsonPacketData);

        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void updateAction()
    {
        JMServerTest.LOGGER.info(jmAPI.getAllWaypoints());
    }

    private void createAction(String waypointName, Vector3d positionVector, String dimension, ClientPlayerEntity player) {
        Map<String, String> WaypointData = getStringStringMap(waypointName, positionVector, dimension, player);
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        String json = null;
        try {
            json = jsonObjectMapper.writeValueAsString(WaypointData);
        } catch (JsonProcessingException e) {
            JMServerTest.LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }

        RegisterUserPayload payload = new RegisterUserPayload(json);
        ClientPlayNetworking.send(payload);
    }

    void WaypointCreationHandler(WaypointEvent waypointEvent) {

        // todo: add handing for destruction of waypoints
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) {
            return;
        }

        String waypointFilename = WaypointIOInterface.getWaypointFilename(waypointEvent, player.getUuid());

        switch (waypointEvent.getContext()) {

            case CREATE ->
            {
                waypointIndex.add(waypointEvent.waypoint);
                this.createAction(waypointEvent.waypoint.getName(), new Vector3d(
                        waypointEvent.waypoint.getX(),
                        waypointEvent.waypoint.getY(),
                        waypointEvent.waypoint.getZ()
                ), waypointEvent.waypoint.getPrimaryDimension(), player);
            }
            case DELETED ->
            {
                waypointIndex.remove(waypointEvent.waypoint);
                this.deleteAction(waypointFilename);
            }
            case UPDATE ->
            {

                //this.updateAction();
            }
        }

    }

    private static @NotNull Map<String, String> getStringStringMap(String waypointName, Vector3d positionVector, String dimension, ClientPlayerEntity player) {
        Map<String, String> WaypointData = new HashMap<>();

        WaypointData.put("name", waypointName);
        WaypointData.put("uuid", player.getUuid().toString());
        WaypointData.put("x", String.valueOf(positionVector.x));
        WaypointData.put("y", String.valueOf(positionVector.y));
        WaypointData.put("z", String.valueOf(positionVector.z));
        WaypointData.put("d", dimension);

        return WaypointData;
    }





    @Override
    public void initialize(final IClientAPI jmAPI)
    {
        this.jmAPI = jmAPI;
        CommonEventRegistry.WAYPOINT_EVENT.subscribe(JMServerTest.MODID, this::WaypointCreationHandler);
        ClientPlayNetworking.registerGlobalReceiver(UserWaypointPayload.ID, this::HandlePacket);

        JMServerTest.LOGGER.info("Initialized " + getClass().getName());
    }

    /**
     * Used by JourneyMap to associate a modId with this plugin.
     */
    @Override
    public String getModId()
    {
        return JMServerTest.MODID;
    }

    public void HandlePacket(UserWaypointPayload waypointPayload, ClientPlayNetworking.Context context) {

        List<? extends Waypoint> waypoints = INSTANCE.jmAPI.getAllWaypoints();
        AtomicBoolean justJoined = new AtomicBoolean(true);

        List<SavedWaypoint> savedWaypoints = waypointPayload.getSavedWaypoints();
        List<Waypoint> waypointArray = new ArrayList<>();

        ClientTickEvents.END_CLIENT_TICK.register((minecraftClient) -> {
            if (context.client().world != null && justJoined.get()) {
                for (Waypoint wp : waypoints) {
                    INSTANCE.jmAPI.removeWaypoint(JMServerTest.MODID, wp);
                }
                for (Waypoint wp : waypointArray) {
                    INSTANCE.jmAPI.addWaypoint(JMServerTest.MODID, wp);
                }

                justJoined.set(false);
            }
        });

        for (SavedWaypoint savedWaypoint : savedWaypoints) {
            JMServerTest.LOGGER.info(savedWaypoint.getWaypointName());
            Waypoint waypointObj = WaypointFactory.createClientWaypoint(
                    JMServerTest.MODID,
                    BlockPos.ofFloored(savedWaypoint.getWaypointX(),
                            savedWaypoint.getWaypointY(),
                            savedWaypoint.getWaypointZ()),
                    savedWaypoint.getDimensionString(),
                    true);

            waypointObj.setName(savedWaypoint.getWaypointName());
            waypointArray.add(waypointObj);

        }
    }
}
