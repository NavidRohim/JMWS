package me.brynview.navidrohim.jm_server_test.client.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jm_server_test.JMServerTest;
import me.brynview.navidrohim.jm_server_test.server.util.WaypointIOInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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

        switch (waypointEvent.getContext()) {
            case CREATE -> {
                Map<String, String> WaypointData = getStringStringMap(waypointEvent, player);

                ObjectMapper jsonObjectMapper = new ObjectMapper();
                String json = null;
                try {
                    json = jsonObjectMapper.writeValueAsString(WaypointData);
                } catch (JsonProcessingException e) {
                    JMServerTest.LOGGER.error(e.getMessage());
                    throw new RuntimeException(e);
                }

                WaypointPayload payload = new WaypointPayload(json);
                ClientPlayNetworking.send(payload);
            }
            case DELETED -> {
                boolean deletedWaypoint = WaypointIOInterface.deleteWaypoint(WaypointIOInterface.getWaypointFilename(waypointEvent, player.getUuid()));

                if (deletedWaypoint) {
                    minecraftClient.inGameHud.getChatHud().addMessage(Text.of("Removed waypoint from server."));
                } else {
                    minecraftClient.inGameHud.getChatHud().addMessage(Text.of("Waypoint was not deleted server-side. Ignoring."));
                }

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
}
