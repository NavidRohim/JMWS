package me.brynview.navidrohim.jm_server.client.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import me.brynview.navidrohim.jm_server.JMServer;
import me.brynview.navidrohim.jm_server.common.SavedWaypoint;
import me.brynview.navidrohim.jm_server.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jm_server.common.utils.JMServerConfig;
import me.brynview.navidrohim.jm_server.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jm_server.common.utils.WaypointIOInterface;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;


@JourneyMapPlugin(apiVersion = "2.0.0")
public class IClientPluginJM implements IClientPlugin
{
    // API reference
    private IClientAPI jmAPI = null;
    private static IClientPluginJM INSTANCE;

    private final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();
    private boolean oldWorld = false;

    private final int tickCounterUpdateThresholdDefault = JMServer.CONFIG.updateWaypointFrequency();
    private final boolean showAlert = JMServer.CONFIG.showAlerts();

    private int tickCounterUpdateThreshold = tickCounterUpdateThresholdDefault;
    private int tickCounter = 0;

    public IClientPluginJM() {
        INSTANCE = this;
    }

    public static IClientPluginJM getInstance() {
        return INSTANCE;
    }

    private void deleteAction(Waypoint waypoint, ClientPlayerEntity player) {

        String waypointFilename = WaypointIOInterface.getWaypointFilename(waypoint, player.getUuid());

        waypointIdentifierMap.remove(waypoint.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteRequestJson(waypointFilename);
        WaypointActionPayload waypointActionPayload = new WaypointActionPayload(jsonPacketData);

        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void updateAction(Waypoint waypoint, ClientPlayerEntity player)
    {
        String persistentWaypointID = waypoint.getCustomData();
        Waypoint oldWaypointReference = waypointIdentifierMap.get(persistentWaypointID);

        this.deleteAction(oldWaypointReference, player);
        this.createAction(waypoint, player);
    }

    private void createAction(Waypoint waypoint, ClientPlayerEntity player) {

        String waypointIdentifier = DigestUtils.sha256Hex(player.getUuid().toString() + waypoint.getGuid() + waypoint.getName());

        waypointIdentifierMap.put(waypointIdentifier, waypoint);
        waypoint.setCustomData(waypointIdentifier);
        String creationData = JsonStaticHelper.makeCreationRequestJson(waypoint);
        ClientPlayNetworking.send(new WaypointActionPayload(creationData));
    }

    void WaypointCreationHandler(WaypointEvent waypointEvent) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) {
            return;
        }

        switch (waypointEvent.getContext()) {

            case CREATE ->
            {
                this.createAction(waypointEvent.waypoint, player);
            }
            case DELETED ->
            {
                this.deleteAction(waypointEvent.waypoint, player);
            }
            case UPDATE ->
            {
                this.updateAction(waypointEvent.waypoint, player);
            }
        }

    }

    @Override
    public void initialize(final IClientAPI jmAPI)
    {
        this.jmAPI = jmAPI;

        CommonEventRegistry.WAYPOINT_EVENT.subscribe(JMServer.MODID, this::WaypointCreationHandler);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
    }

    public static void updateWaypoints(MinecraftClient minecraftClient) {
        if (minecraftClient.player != null && getInstance().showAlert) {
            minecraftClient.player.sendMessage(Text.translatable("message.jm_server.modified_success"), true);
        }
        ClientPlayNetworking.send(new WaypointActionPayload(JsonStaticHelper.makeWaypointRequestJson()));
    }

    private void handleTick(MinecraftClient minecraftClient) {
        if (minecraftClient.world != null) {
            if (!oldWorld) {
                tickCounterUpdateThreshold = 40;
                oldWorld = true;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) {
                    updateWaypoints(minecraftClient);
                    tickCounter = 0;
                    tickCounterUpdateThreshold = tickCounterUpdateThresholdDefault;
                }
            }
        } else {
            tickCounter = 0;
            oldWorld = false;
        }
    }

    /**
     * Used by JourneyMap to associate a modId with this plugin.
     */
    @Override
    public String getModId()
    {
        return JMServer.MODID;
    }

    private static List<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) {
        List<SavedWaypoint> waypoints = new ArrayList<>();

        for (Map.Entry<String, JsonElement> wpEntry : jsonData.entrySet()) {
            waypoints.add(new SavedWaypoint(wpEntry.getValue().getAsJsonObject(), playerUUID));
        }
        return waypoints;
    }

    // Handler for WaypointActionPayload
    public static void HandlePacket(WaypointActionPayload waypointPayload, ClientPlayNetworking.Context context) {
        MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();

        if (minecraftClientInstance.player == null) {
            return;
        }

        switch (waypointPayload.command()) {
            case "creation_response" -> {
                JsonObject json = waypointPayload.arguments().getFirst().getAsJsonObject().deepCopy();
                List<SavedWaypoint> savedWaypoints = IClientPluginJM.getSavedWaypoints(json, context.player().getUuid());
                INSTANCE.jmAPI.removeAllWaypoints("journeymap");

                for (SavedWaypoint savedWaypoint : savedWaypoints) {

                    Waypoint waypointObj = WaypointFactory.createClientWaypoint(
                            "journeymap",
                            BlockPos.ofFloored(savedWaypoint.getWaypointX(),
                                    savedWaypoint.getWaypointY(),
                                    savedWaypoint.getWaypointZ()),
                            savedWaypoint.getDimensionString(),
                            true);

                    waypointObj.setColor(savedWaypoint.getWaypointColour());
                    waypointObj.setName(savedWaypoint.getWaypointName());
                    waypointObj.setCustomData(savedWaypoint.getUniversalIdentifier());

                    INSTANCE.waypointIdentifierMap.put(savedWaypoint.getUniversalIdentifier(), waypointObj);
                    INSTANCE.jmAPI.addWaypoint("journeymap", waypointObj);
                }
            }
            case "update" -> {
                IClientPluginJM.updateWaypoints(MinecraftClient.getInstance());
            }
            case "display_interval" -> {
                if (getInstance().showAlert) {
                    minecraftClientInstance.player.sendMessage(Text.of("Waypoints updated every " + INSTANCE.tickCounterUpdateThreshold / 20 + " seconds."), true);
                }
            }
            case "alert" -> {
                if (getInstance().showAlert) {
                    JMServer.LOGGER.info(waypointPayload.arguments().get(1).getAsBoolean());
                    minecraftClientInstance.player.sendMessage(Text.translatable(waypointPayload.arguments().get(0).getAsString()), waypointPayload.arguments().get(1).getAsBoolean());
                }
            }
        }
    }
}
