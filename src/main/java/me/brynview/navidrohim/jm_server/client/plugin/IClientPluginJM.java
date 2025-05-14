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
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

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

    public static void sendUserAlert(String text, boolean overlayText, boolean ignoreConfig) {
        MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();
        if ((config.showAlerts() || ignoreConfig) && minecraftClientInstance.player != null) {
            minecraftClientInstance.player.sendMessage(Text.of(text), overlayText);
        }
    }
    public boolean getEnabledStatus() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        return (config.enabled() && !minecraftClient.isInSingleplayer());
    }

    public Waypoint getOldWaypoint(Waypoint newWaypoint) {
        String persistentWaypointID = newWaypoint.getCustomData();
        return waypointIdentifierMap.get(persistentWaypointID);
    }

    private void deleteAction(Waypoint waypoint, ClientPlayerEntity player, boolean silent) {

        String waypointFilename = WaypointIOInterface.getWaypointFilename(waypoint, player.getUuid());

        waypointIdentifierMap.remove(waypoint.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteRequestJson(waypointFilename, silent);
        WaypointActionPayload waypointActionPayload = new WaypointActionPayload(jsonPacketData);

        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void updateAction(Waypoint waypoint, Waypoint oldWaypoint, ClientPlayerEntity player)
    {
        this.deleteAction(oldWaypoint, player, true);
        this.createAction(waypoint, player, true);

        jmAPI.removeWaypoint("journeymap", oldWaypoint);
        sendUserAlert(Text.translatable("message.jm_server.modified_waypoint_success").getString(), true, false);
    }

    private void createAction(Waypoint waypoint, ClientPlayerEntity player, boolean silent) {

        String waypointIdentifier = DigestUtils.sha256Hex(player.getUuid().toString() + waypoint.getGuid() + waypoint.getName());

        waypointIdentifierMap.put(waypointIdentifier, waypoint);
        waypoint.setCustomData(waypointIdentifier);
        String creationData = JsonStaticHelper.makeCreationRequestJson(waypoint, silent);
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
                    this.createAction(waypointEvent.waypoint, player, false);
                }
                case DELETED ->
                {
                    this.deleteAction(waypointEvent.waypoint, player, false);
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
        sendUserAlert(Text.translatable("message.jm_server.modified_success").getString(), true, false);
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

                    if (!(tickCounterUpdateThreshold >= config.updateWaypointFrequency())) {
                        tickCounterUpdateThreshold = tickCounterUpdateThreshold * 2;
                    } else {
                        tickCounterUpdateThreshold = config.updateWaypointFrequency();
                    }
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

            if (minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {
                case "creation_response" -> {
                    JsonObject json = waypointPayload.arguments().getFirst().getAsJsonObject().deepCopy();

                    List<SavedWaypoint> savedWaypoints = IClientPluginJM.getSavedWaypoints(json, context.player().getUuid());
                    List<BlockPos> remoteWaypointsGuid = new ArrayList<>();

                    // Add server waypoint coordinates onto list to check
                    for (SavedWaypoint savedWaypoint : savedWaypoints) {
                        remoteWaypointsGuid.add(new BlockPos(savedWaypoint.getWaypointX(), savedWaypoint.getWaypointY(), savedWaypoint.getWaypointZ()));
                    }

                    List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
                    for (Waypoint existingWaypoint : existingWaypoints)
                    {
                        // check if waypoint already exists locally while not being in the server (meaning it was created with the mod off or not installed)
                        if (!remoteWaypointsGuid.contains(existingWaypoint.getBlockPos())) // this is only checked by using the block position, there will be a bug I can feel it
                        {
                            getInstance().createAction(existingWaypoint, context.player(), true);
                            sendUserAlert("Added local waypoint to server.", true, false);
                        }
                    }

                    // remove all to update (if waypoint has been removed)
                    INSTANCE.jmAPI.removeAllWaypoints("journeymap");

                    // Add waypoints registered on the server
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

                        // todo; in future update, add all waypoint data manually by chaining methods (very stupid, I wish I didnt have to)

                    }
                }
                case "update" -> {
                    IClientPluginJM.updateWaypoints(MinecraftClient.getInstance());
                }
                case "display_interval" -> {
                    sendUserAlert("Waypoints updated every " + INSTANCE.tickCounterUpdateThreshold / 20 + " seconds.", true, false);
                }
                case "alert" -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    sendUserAlert(Text.translatable(firstArgument).getString(), waypointPayload.arguments().get(1).getAsBoolean(), false);
                }
                case "deleteWaypoint" -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    if (Objects.equals(firstArgument, "*")) {
                        INSTANCE.jmAPI.removeAllWaypoints("journeymap");
                    } else {
                        INSTANCE.jmAPI.removeWaypoint("journeymap", INSTANCE.waypointIdentifierMap.get(firstArgument));
                    }
                    sendUserAlert(Text.translatable("message.jm_server.deletion_all_success").getString(), true, false);
                }
            }
        }
    }
}
