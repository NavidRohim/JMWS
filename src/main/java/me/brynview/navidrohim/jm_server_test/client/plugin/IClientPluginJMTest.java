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

    private static IClientPluginJMTest INSTANCE;

    public IClientPluginJMTest() {
        INSTANCE = this;
    }

    public static IClientPluginJMTest getInstance() {
        return INSTANCE;
    }
    void WaypointCreationHandler(WaypointEvent waypointEvent) {

        // todo: add handing for destruction of waypoints
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        if (player == null) {
            return;
        }
        Map<String, String> WaypointData = getStringStringMap(waypointEvent, player);

        switch (waypointEvent.getContext()) {

            case CREATE -> {
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
            case DELETED -> {
                String waypointFilename = WaypointIOInterface.getWaypointFilename(waypointEvent, player.getUuid());
                String jsonPacketData = JsonStaticHelper.makeDeleteJson(waypointFilename);
                WaypointActionPayload waypointActionPayload = new WaypointActionPayload(jsonPacketData);

                ClientPlayNetworking.send(waypointActionPayload);
                /*
                if (deletedWaypoint) {
                    minecraftClient.inGameHud.getChatHud().addMessage(Text.of("Removed waypoint from server."));
                } else {
                    minecraftClient.inGameHud.getChatHud().addMessage(Text.of("Waypoint was not deleted server-side. Ignoring."));
                }*/

            } case UPDATE -> {
                // todo
            }
        }

    }

    private static @NotNull Map<String, String> getStringStringMap(WaypointEvent waypointEvent, ClientPlayerEntity player) {
        Map<String, String> WaypointData = new HashMap<>();
        Waypoint waypoint = waypointEvent.waypoint;

        WaypointData.put("name", waypoint.getName());
        WaypointData.put("uuid", player.getUuid().toString());
        WaypointData.put("x", String.valueOf(waypoint.getX()));
        WaypointData.put("y", String.valueOf(waypoint.getY()));
        WaypointData.put("z", String.valueOf(waypoint.getZ()));
        WaypointData.put("d", waypoint.getPrimaryDimension());

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

        for (Waypoint wp : waypoints) {
            INSTANCE.jmAPI.removeWaypoint(JMServerTest.MODID, wp);
        }

        List<SavedWaypoint> savedWaypoints = waypointPayload.getSavedWaypoints();
        List<Waypoint> waypointArray = new ArrayList<>();

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


        ClientTickEvents.END_CLIENT_TICK.register((minecraftClient) -> {
            if (context.client().world != null && justJoined.get()) {
                justJoined.set(false);
                for (Waypoint wp : waypointArray) {
                    INSTANCE.jmAPI.addWaypoint(JMServerTest.MODID, wp);
                }
            }
        });
        }
    }
}
