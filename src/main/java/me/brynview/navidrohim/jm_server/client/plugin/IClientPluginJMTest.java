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
import me.brynview.navidrohim.jm_server.JMServerTest;
import me.brynview.navidrohim.jm_server.common.SavedWaypoint;
import me.brynview.navidrohim.jm_server.common.payloads.WaypointActionPayload;
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
public class IClientPluginJMTest implements IClientPlugin
{
    // API reference
    private IClientAPI jmAPI = null;
    private static IClientPluginJMTest INSTANCE;

    private final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();
    private int tickCounterUpdateThreshold = 1300;
    private int tickCounter = 0;
    private boolean oldWorld = false;

    public IClientPluginJMTest() {
        INSTANCE = this;
    }

    public static IClientPluginJMTest getInstance() {
        return INSTANCE;
    }

    private void deleteAction(Waypoint waypoint, ClientPlayerEntity player) {

        String waypointFilename = WaypointIOInterface.getWaypointFilename(waypoint, player.getUuid());

        waypointIdentifierMap.remove(waypoint.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteJson(waypointFilename);
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

        CommonEventRegistry.WAYPOINT_EVENT.subscribe(JMServerTest.MODID, this::WaypointCreationHandler);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
    }

    public static void updateWaypoints(MinecraftClient minecraftClient) {
        if (minecraftClient.player != null) {
            minecraftClient.player.sendMessage(Text.of("Updating waypoint status"), true);
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
                    tickCounterUpdateThreshold = 1300;
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
        return JMServerTest.MODID;
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

        JsonObject json = waypointPayload.arguments().getFirst().getAsJsonObject().deepCopy();
        List<SavedWaypoint> savedWaypoints = IClientPluginJMTest.getSavedWaypoints(json, context.player().getUuid());
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
}
