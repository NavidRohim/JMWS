package me.brynview.navidrohim.jmws.client.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.IThemeButton;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.display.WaypointGroup;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.WaypointEvent;
import journeymap.client.api.event.fabric.FullscreenDisplayEvent;
import me.brynview.navidrohim.jmws.JMWS;
import me.brynview.navidrohim.jmws.client.JMWSClient;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.client.config.JMWSConfig;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.AssetHelper;
import me.brynview.navidrohim.jmws.client.helpers.CommonHelper;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPacket;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePacket;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class IClientPluginJM implements journeymap.client.api.IClientPlugin
{
    private static ScheduledFuture<?> timeoutTask;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // API reference
    private journeymap.client.api.IClientAPI jmAPI = null;
    private static IClientPluginJM INSTANCE;

    private final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();
    private final HashMap<String, WaypointGroup> groupIdentifierMap = new HashMap<>();

    private ClientWorld oldWorld = null;
    private boolean serverHasMod = false;

    private static final JMWSConfig config = JMWSClient.CONFIG;
    private int tickCounterUpdateThreshold = config.clientConfiguration.updateWaypointFrequency();
    private int tickCounter = 0;

    public static final MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();
    public IClientPluginJM() {
        INSTANCE = this;
    }

    public static IClientPluginJM getInstance() {
        return INSTANCE;
    }

    public static void sendUserAlert(Text text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {
        String finalText = text.getString();

        if (config.colouredText()) {
            finalText = messageType.toString() + text.getString();
        }

        if ((config.showAlerts() || ignoreConfig) && minecraftClientInstance.player != null) {
            minecraftClientInstance.player.sendMessage(Text.of(finalText), overlayText);
        }
    }

    public static void sendUserSoundAlert(SoundEvent sound) {
        if (config.playEffects() && minecraftClientInstance.player != null) {
            minecraftClientInstance.player.playSound(sound, 0.09f, 1f);
        }
    }

    private boolean serverEnabledJMWS() {
        return config.serverConfiguration.serverEnabled() && (config.serverConfiguration.serverGroupsEnabled() || config.serverConfiguration.serverWaypointsEnabled());
    }
    public boolean getEnabledStatus() {
        return serverHasMod && serverEnabledJMWS() && config.enabled() && (config.uploadGroups() || config.uploadWaypoints()) && !minecraftClientInstance.isInSingleplayer();
    }

    private void deleteAction(Waypoint waypoint, boolean silent) {

        String waypointFilename = JMWSServerIO.getWaypointFilename(waypoint, minecraftClientInstance.player.getUuid());

        String jsonPacketData = JsonStaticHelper.makeDeleteRequestJson(waypointFilename, silent, false);
        JMWSActionPacket waypointActionPayload = new JMWSActionPacket(jsonPacketData);

        jmAPI.remove(waypoint);
        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void updateAction(Waypoint waypoint)
    {
        this.createAction(waypoint, true, true);
        sendUserAlert(Text.translatable("message.jmws.modified_waypoint_success"), true, false, JMWSMessageType.SUCCESS);
    }

    private void createAction(Waypoint waypoint, boolean silent, boolean isUpdate) {

        String waypointIdentifier = CommonHelper.makeWaypointHash(minecraftClientInstance.player.getUuid(), waypoint.getGuid(), waypoint.getName());
        waypointIdentifierMap.put(waypointIdentifier, waypoint);

        waypoint.setPersistent(false);

        String creationData = JsonStaticHelper.makeCreationRequestJson(waypoint, silent, isUpdate);
        ClientPlayNetworking.send(new JMWSActionPacket(creationData));
    }

    void WaypointCreationHandler(WaypointEvent waypointEvent) {
        if (this.getEnabledStatus() && config.uploadWaypoints() && config.serverConfiguration.serverWaypointsEnabled()) {


            switch (waypointEvent.getContext()) {

                case CREATE ->
                    // Sends "create" packet | new = "SERVER_CREATE"
                        this.createAction(waypointEvent.waypoint, false, false);
                case DELETED ->
                    // Sends "delete" packet | new = "COMMON_SERVER_DELETE"
                        this.deleteAction(waypointEvent.waypoint, false);
                case UPDATE ->
                    // Sends both "delete" and "create" packet in respective order and respective enums.
                        this.updateAction(waypointEvent.waypoint);
            }
        }
    }

    @Override
    public void initialize(final @NotNull IClientAPI jmAPI)
    {
        this.jmAPI = jmAPI;

        // Payloads
        ClientPlayNetworking.registerGlobalReceiver(JMWSActionPacket.TYPE, IClientPluginJM::HandlePacket);
        ClientPlayNetworking.registerGlobalReceiver(JMWSHandshakePacket.TYPE, IClientPluginJM::HandshakeHandler);

        // JourneyMap Events
        //CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", JMWS.MODID, this::WaypointCreationHandler);
        //FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(JMWS.MODID, this::addJMButtons);

        // Vanilla Events

        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {

            if (!client.isInSingleplayer()) {
                ClientPlayNetworking.send(new JMWSHandshakePacket(null));

                timeoutTask = scheduler.schedule(() -> {
                    if (!serverHasMod) {
                        minecraftClientInstance.execute(() -> {
                            sendUserAlert(Text.translatable("error.jmws.jmws_not_installed"), true, true, JMWSMessageType.FAILURE);
                            sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                        });
                    }
                }, config.clientConfiguration.serverHandshakeTimeout(), TimeUnit.SECONDS);
            } else {
                sendUserAlert(Text.translatable("warning.jmws.world_is_local"), true, false, JMWSMessageType.WARNING);
                sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
            }
        }));
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            tickCounter = 0;
            serverHasMod = false;
        }));
    }

    private void addJMButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {

        if (!minecraftClientInstance.isInSingleplayer()) {
            IThemeButton buttonEnabled = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.enable_button",
                    String.valueOf(AssetHelper.onOffButtonAsset),
                    getEnabledStatus(),
                    this::enableMod);

            IThemeButton buttonSync = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.update_button",
                    String.valueOf(AssetHelper.enableButtonAsset),
                    true,
                    this::updateFromButton);

            buttonSync.setEnabled(getEnabledStatus());
            buttonSync.setTooltip(Text.translatable("button.jmws.tooltip.update_button").getString());

            buttonEnabled.setTooltip(Text.translatable("button.jmws.tooltip.enable_button").getString());
        }
    }

    private void enableMod(IThemeButton iThemeButton) {
        iThemeButton.setLabels(
                Text.translatable("addServer.resourcePack.enabled").getString(),
                Text.translatable("addServer.resourcePack.disabled").getString()
        );
        if (getEnabledStatus()) {
            config.enabled(false);
            iThemeButton.setToggled(false);
        } else {
            config.enabled(true);
            iThemeButton.setToggled(true);
        }
    }

    private void updateFromButton(IThemeButton iThemeButton) {
        updateWaypoints(true);
    }

    public static int getTickCounterUpdateThreshold() {
        return getInstance().tickCounterUpdateThreshold;
    }

    public static int getCurrentUpdateTick() {
        return getInstance().tickCounter;
    }
    public static void updateWaypoints(boolean sendAlert) { // Might use delay some day

        // Sends "request" packet | New = "SYNC"
        if (getInstance().getEnabledStatus()) {
            ClientPlayNetworking.send(new JMWSActionPacket(JsonStaticHelper.makeWaypointSyncRequestJson(sendAlert)));
        }

    }

    private void handleTick(MinecraftClient _minecraftClient) {

        // Sends "sync" packet | New = SYNC
        ClientWorld world = minecraftClientInstance.world;

        if (world != null && this.getEnabledStatus()) {
            if (world != oldWorld) {
                if (oldWorld == null) {
                    tickCounterUpdateThreshold = 20 * (config.clientConfiguration.serverHandshakeTimeout() + 1); // Add 1 second buffer to not interrupt message
                } else {
                    tickCounterUpdateThreshold = 40; // 2-second delay when switching dimension
                }
                tickCounter = 0;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) {

                    updateWaypoints(true);
                    tickCounter = 0;
                    tickCounterUpdateThreshold = config.clientConfiguration.updateWaypointFrequency();
                }
            }
            oldWorld = minecraftClientInstance.world;
        } else {

            tickCounter = 0;
            oldWorld = null;
        }
    }

    @Override
    public String getModId()
    {
        return JMWS.MODID;
    }

    @Override
    public void onEvent(ClientEvent clientEvent) {
        JMWS.info(clientEvent.type);
    }

    public static void HandshakeHandler(JMWSHandshakePacket handshakePayload, ClientPlayerEntity player, PacketSender packetSender){
        /*
        if (!getInstance().serverEnabledJMWS()) {
            sendUserAlert(Text.translatable("warning.jmws.server_disabled_jmws"), true, false, JMWSMessageType.WARNING);
        } else if (!getInstance().config.serverConfiguration.serverWaypointsEnabled()) {
            sendUserAlert(Text.translatable("warning.jmws.server_disabled_waypoint"), true, false, JMWSMessageType.WARNING);
        } else if (!getInstance().config.serverConfiguration.serverGroupsEnabled()) {
            sendUserAlert(Text.translatable("warning.jmws.server_disabled_group"), true, false, JMWSMessageType.WARNING);
        } else {
            sendUserAlert(Text.translatable("message.jmws.has_jmws"), true, false, JMWSMessageType.SUCCESS);
        }*/

        sendUserAlert(Text.of("JMWS Does not work in 1.20. Read GitHub for further reference."), true, true, JMWSMessageType.FAILURE);
        getInstance().serverHasMod = true;

        if (timeoutTask != null && !timeoutTask.isDone()) {
            timeoutTask.cancel(false);
        }
    }

    // Helper for handleUploadWaypoints
    private static Set<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) throws JsonSyntaxException, IllegalStateException {
        Set<SavedWaypoint> waypoints = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            waypoints.add(new SavedWaypoint(json, playerUUID));
        }

        return waypoints;

    }


    // Helper for sync, handleUploadWaypoints is basically the same but different type annotations. I should've used generic

    // Helper for sync but for waypoints
    private boolean handleUploadWaypoints(JsonObject jsonWaypoints, PlayerEntity player) throws JsonSyntaxException, IllegalStateException {
        boolean hasLocalWaypoint = false;

        // Get existing waypoints (local) and get waypoint objects saved on server
        List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
        Set<SavedWaypoint> savedWaypoints = IClientPluginJM.getSavedWaypoints(jsonWaypoints.deepCopy(), player.getUuid());

        // Get an identifier of every waypoint (BlockPos, location), used to detect if the waypoint already exists
        Set<BlockPos> remoteWaypointPositions = savedWaypoints.stream()
                .map(w -> new BlockPos(w.getWaypointX(), w.getWaypointY(), w.getWaypointZ()))
                .collect(Collectors.toSet());

        getInstance().jmAPI.removeAll("journeymap");

        // Test if any existing waypoints (persistent, usually death waypoints) have already been added to the server, if not, add them
        for (Waypoint existing : existingWaypoints) {
            if (!remoteWaypointPositions.contains(existing.getPosition()) && existing.isPersistent()) {
                getInstance().createAction(existing, true, false);
                hasLocalWaypoint = true;
            }
        }

        // Add server waypoints to the client
        for (SavedWaypoint savedWaypoint : savedWaypoints) {
            // Waypoint wp = Waypoint.fromWaypointJsonString(savedWaypoint.getRawPacketData()); this will be annoying
            //getInstance().jmAPI.addWaypoint("journeymap", wp); // FIX
        }

        return hasLocalWaypoint;
    }

    public static void syncHandler(JMWSActionPacket waypointPayload, PlayerEntity player) {
        boolean hasLocalGroup = false;
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().getLast().getAsBoolean();

        try {

            if (config.uploadWaypoints() && config.serverConfiguration.serverWaypointsEnabled()) {
                hasLocalWaypoint = getInstance().handleUploadWaypoints(waypointPayload.arguments().getFirst().getAsJsonObject(), player);
            }

            if (hasLocalGroup || hasLocalWaypoint) {
                updateWaypoints(false);
                if (hasLocalGroup && hasLocalWaypoint) {
                    sendUserAlert(Text.translatable("message.jmws.local_both_upload"), true, false, JMWSMessageType.SUCCESS);
                } else if (hasLocalGroup) {
                    sendUserAlert(Text.translatable("message.jmws.local_group_upload"), true, false, JMWSMessageType.SUCCESS);
                } else {
                    sendUserAlert(Text.translatable("message.jmws.local_waypoint_upload"), true, false, JMWSMessageType.SUCCESS);
                }
            } else if (sendAlert) {
                String updateMessageKey = "message.jmws.synced_success";

                if (config.uploadWaypoints() && config.uploadGroups()) {
                    updateMessageKey = "message.jmws.synced_both_success";
                } else if (config.uploadGroups()) {
                    updateMessageKey = "message.jmws.synced_group_success";
                }
                sendUserAlert(Text.translatable(updateMessageKey), true, false, JMWSMessageType.NEUTRAL);
            }
            sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);

        } catch (IllegalStateException | JsonSyntaxException exception) {
            sendUserAlert(Text.translatable("error.jmws.error_corrupted_waypoint"), true, false, JMWSMessageType.FAILURE);
            sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
        }
    }

    // Handler for JMWSActionPayload
    public static void HandlePacket(JMWSActionPacket waypointPayload, ClientPlayerEntity player, PacketSender packetSender) {

        if (getInstance().getEnabledStatus()) {

            if (minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {

                // Was creation_response
                // Sends no outbound data
                case SYNC -> syncHandler(waypointPayload, player);

                // was "update"
                // Sends "request" packet | New = "SYNC"
                case REQUEST_CLIENT_SYNC -> IClientPluginJM.updateWaypoints(true);

                // was display_interval
                // No outbound data
                case COMMON_DISPLAY_INTERVAL -> sendUserAlert(Text.translatable("message.jmws.sync_frequency", INSTANCE.tickCounterUpdateThreshold / 20), true, false, JMWSMessageType.NEUTRAL);

                // was "alert"
                // No outbound data
                case CLIENT_ALERT -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    boolean isError = waypointPayload.arguments().getLast().getAsBoolean();
                    JMWSMessageType messageType = JMWSMessageType.NEUTRAL;

                    if (isError) {
                        messageType = JMWSMessageType.FAILURE;
                        sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    }

                    sendUserAlert(Text.translatable(firstArgument), waypointPayload.arguments().get(1).getAsBoolean(), false, messageType);
                }

                // was "deleteWaypoint"
                // No outbound data
                case COMMON_DELETE_WAYPOINT -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    JMWSServerIO.FetchType deletionType = JMWSServerIO.FetchType.valueOf(waypointPayload.arguments().get(1).getAsString());
                    String deletionMessageConfirmationKey = "message.jmws.deletion_all_success";

                    if (deletionType == JMWSServerIO.FetchType.WAYPOINT) {
                        if (Objects.equals(firstArgument, "*")) {
                            INSTANCE.jmAPI.removeAll("journeymap");
                        } else {
                            INSTANCE.jmAPI.remove(INSTANCE.waypointIdentifierMap.get(firstArgument));
                        }

                    }
                    sendUserAlert(Text.translatable(deletionMessageConfirmationKey), true, false, JMWSMessageType.NEUTRAL);
                }

                // was "display_next_update"
                // No outbound data
                case COMMON_DISPLAY_NEXT_UPDATE -> sendUserAlert(Text.translatable("message.jmws.next_sync", (INSTANCE.tickCounterUpdateThreshold - INSTANCE.tickCounter) / 20), true, false, JMWSMessageType.NEUTRAL);

                default -> JMWS.LOGGER.warn("Unknown packet command -> " + waypointPayload.command());
            }
        }
    }
}
