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
import me.brynview.navidrohim.jm_server.client.JMServerClient;
import me.brynview.navidrohim.jm_server.common.SavedWaypoint;
import me.brynview.navidrohim.jm_server.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jm_server.common.utils.JMServerConfig;
import me.brynview.navidrohim.jm_server.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jm_server.common.utils.WaypointIOInterface;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


@JourneyMapPlugin(apiVersion = "2.0.0")
public class IClientPluginJM implements IClientPlugin
{
    // API reference
    private IClientAPI jmAPI = null;
    private static IClientPluginJM INSTANCE;

    private final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();
    private boolean oldWorld = false;

    private static final JMServerConfig config = JMServerClient.CONFIG;
    private int tickCounterUpdateThreshold = config.updateWaypointFrequency();
    private int tickCounter = 0;

    public IClientPluginJM() {
        INSTANCE = this;
    }

    public static IClientPluginJM getInstance() {
        return INSTANCE;
    }

    public boolean getEnabledStatus() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        return (config.enabled() && !minecraftClient.isInSingleplayer());
    }

    public Waypoint getOldWaypoint(Waypoint newWaypoint) {
        String persistentWaypointID = newWaypoint.getCustomData();
        return waypointIdentifierMap.get(persistentWaypointID);
    }

    private void deleteAction(Waypoint waypoint, ClientPlayerEntity player) {

        String waypointFilename = WaypointIOInterface.getWaypointFilename(waypoint, player.getUuid());

        waypointIdentifierMap.remove(waypoint.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteRequestJson(waypointFilename);
        WaypointActionPayload waypointActionPayload = new WaypointActionPayload(jsonPacketData);

        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void updateAction(Waypoint waypoint, Waypoint oldWaypoint, ClientPlayerEntity player)
    {
        this.deleteAction(oldWaypoint, player);
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
        if (this.getEnabledStatus()) {
            MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();
            ClientPlayerEntity player = minecraftClientInstance.player;

            if (player == null) {
                return;
            }

            Waypoint oldWaypoint = this.getOldWaypoint(waypointEvent.waypoint);
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
                    this.updateAction(waypointEvent.waypoint, oldWaypoint, player);
                }
            }
        }
    }

    public void registerEvents() {
        ClientPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, IClientPluginJM::HandlePacket);
        CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", JMServer.MODID, this::WaypointCreationHandler);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
    }

    public void unregisterEvents() {
        ClientPlayNetworking.unregisterGlobalReceiver(WaypointActionPayload.ID.id());
        CommonEventRegistry.WAYPOINT_EVENT.unsubscribe("jmapi", JMServer.MODID);
    }
    @Override
    public void initialize(final @NotNull IClientAPI jmAPI)
    {
        this.jmAPI = jmAPI;

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            this.registerEvents();
        }));

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            this.unregisterEvents();
        }));
    }

    public static void updateWaypoints(MinecraftClient minecraftClient) {
        if (minecraftClient.player != null && config.showAlerts()) {
            minecraftClient.player.sendMessage(Text.translatable("message.jm_server.modified_success"), true);
        }
        ClientPlayNetworking.send(new WaypointActionPayload(JsonStaticHelper.makeWaypointRequestJson()));
    }

    private void handleTick(MinecraftClient minecraftClient) {
        if (minecraftClient.world != null && this.getEnabledStatus()) {
            if (!oldWorld) {
                tickCounterUpdateThreshold = 40;
                oldWorld = true;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) {
                    updateWaypoints(minecraftClient);
                    tickCounter = 0;
                    tickCounterUpdateThreshold = config.updateWaypointFrequency();
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

    public static List<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) {
        List<SavedWaypoint> waypoints = new ArrayList<>();

        for (Map.Entry<String, JsonElement> wpEntry : jsonData.entrySet()) {
            waypoints.add(new SavedWaypoint(wpEntry.getValue().getAsJsonObject(), playerUUID));
        }
        return waypoints;
    }

    // Handler for WaypointActionPayload
    public static void HandlePacket(WaypointActionPayload waypointPayload, ClientPlayNetworking.Context context) {
        if (getInstance().getEnabledStatus()) {
            MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();

            if (minecraftClientInstance.player == null) {
                return;
            }

            switch (waypointPayload.command()) {
                case "creation_response" -> {
                    JsonObject json = waypointPayload.arguments().getFirst().getAsJsonObject().deepCopy();

                    // Handles if the user has made waypoints before (with the mod disabled or when it wasnt installed) and uploads them to the server.
                    List<SavedWaypoint> savedWaypoints = IClientPluginJM.getSavedWaypoints(json, context.player().getUuid());
                    List<String> remoteWaypointsGuid = new ArrayList<>();

                    for (SavedWaypoint savedWaypoint : savedWaypoints) {
                        remoteWaypointsGuid.add(savedWaypoint.getWaypointLocalID());
                    }

                    List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();

                    JMServer.LOGGER.info(remoteWaypointsGuid);
                    for (Waypoint existingWaypoint : existingWaypoints)
                    {
                        JMServer.LOGGER.info(existingWaypoint.getGuid());
                        if (!remoteWaypointsGuid.contains(existingWaypoint.getGuid()))
                        {
                            getInstance().createAction(existingWaypoint, context.player()); // todo; add "silent" parameter (doesnt alert user of creation)
                            JMServer.LOGGER.info("Added -> " + existingWaypoint.getName() + " while mod was disabled / uninstalled.");
                        }
                    }

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
                    if (config.showAlerts()) {
                        minecraftClientInstance.player.sendMessage(Text.of("Waypoints updated every " + INSTANCE.tickCounterUpdateThreshold / 20 + " seconds."), true);
                    }
                }
                case "alert" -> {
                    if (config.showAlerts()) {
                        minecraftClientInstance.player.sendMessage(Text.translatable(waypointPayload.arguments().get(0).getAsString()), waypointPayload.arguments().get(1).getAsBoolean());
                    }
                }
            }
        }
    }
}
