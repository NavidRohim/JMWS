package me.brynview.navidrohim.jmws.client.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.event.common.WaypointGroupEvent;
import journeymap.api.v2.common.event.common.WaypointGroupTransferEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.JMWS;
import me.brynview.navidrohim.jmws.client.JMWSClient;
import me.brynview.navidrohim.jmws.common.JMWSConstants;
import me.brynview.navidrohim.jmws.client.objects.SavedGroup;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.client.config.JMWSConfig;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.AssetHelper;
import me.brynview.navidrohim.jmws.client.helpers.CommonHelper;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
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


@JourneyMapPlugin(apiVersion = "2.0.0")
public class IClientPluginJM implements IClientPlugin
{
    private static ScheduledFuture<?> timeoutTask;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // API reference
    private IClientAPI jmAPI = null;
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

    public Waypoint getOldWaypoint(Waypoint newWaypoint) {
        String persistentWaypointID = newWaypoint.getCustomData();
        return waypointIdentifierMap.get(persistentWaypointID);
    }

    public WaypointGroup getOldGroup(WaypointGroup newWaypointGroup)
    {
        return groupIdentifierMap.get(newWaypointGroup.getCustomData());
    }

    private void deleteAction(Waypoint waypoint, boolean silent) {

        String waypointFilename = JMWSServerIO.getWaypointFilename(waypoint, minecraftClientInstance.player.getUuid());

        waypointIdentifierMap.remove(waypoint.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteRequestJson(waypointFilename, silent, false);
        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);

        jmAPI.removeWaypoint("journeymap", waypoint);
        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void updateAction(Waypoint waypoint, Waypoint oldWaypoint)
    {
        if (oldWaypoint != null) {
            this.deleteAction(oldWaypoint, true);
            jmAPI.removeWaypoint("journeymap", oldWaypoint);
        }
        this.createAction(waypoint, true, true);

        sendUserAlert(Text.translatable("message.jmws.modified_waypoint_success"), true, false, JMWSMessageType.SUCCESS);
    }

    private void createAction(Waypoint waypoint, boolean silent, boolean isUpdate) {

        String waypointIdentifier = CommonHelper.makeWaypointHash(minecraftClientInstance.player.getUuid(), waypoint.getGuid(), waypoint.getName());
        waypointIdentifierMap.put(waypointIdentifier, waypoint);

        waypoint.setPersistent(false);
        waypoint.setCustomData(waypointIdentifier);

        String creationData = JsonStaticHelper.makeCreationRequestJson(waypoint, silent, isUpdate);
        ClientPlayNetworking.send(new JMWSActionPayload(creationData));
    }

    void WaypointCreationHandler(WaypointEvent waypointEvent) {
        if (this.getEnabledStatus() && config.uploadWaypoints() && config.serverConfiguration.serverWaypointsEnabled()) {

            Waypoint oldWaypoint = this.getOldWaypoint(waypointEvent.waypoint);

            switch (waypointEvent.getContext()) {

                case CREATE ->
                    // Sends "create" packet | new = "SERVER_CREATE"
                        this.createAction(waypointEvent.waypoint, false, false);
                case DELETED ->
                    // Sends "delete" packet | new = "COMMON_SERVER_DELETE"
                        this.deleteAction(waypointEvent.waypoint, false);
                case UPDATE ->
                    // Sends both "delete" and "create" packet in respective order and respective enums.
                        this.updateAction(waypointEvent.waypoint, oldWaypoint);
            }
        }
    }

    @Override
    public void initialize(final @NotNull IClientAPI jmAPI)
    {
        this.jmAPI = jmAPI;

        // Payloads
        ClientPlayNetworking.registerGlobalReceiver(JMWSActionPayload.ID, IClientPluginJM::HandlePacket);
        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, IClientPluginJM::HandshakeHandler);

        // JourneyMap Events
        CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", JMWS.MODID, this::WaypointCreationHandler);
        CommonEventRegistry.WAYPOINT_GROUP_EVENT.subscribe("jmapi", JMWS.MODID, this::groupEventListener);
        CommonEventRegistry.WAYPOINT_GROUP_TRANSFER_EVENT.subscribe("jmapi", JMWS.MODID, this::waypointDragHandler);
        FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(JMWS.MODID, this::addJMButtons);

        // Vanilla Events

        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {

            if (!client.isInSingleplayer()) {
                ClientPlayNetworking.send(new HandshakePayload());

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

    private void waypointDragHandler(WaypointGroupTransferEvent waypointGroupTransferEvent) {
        Waypoint subjectedChangeWp = waypointGroupTransferEvent.getWaypoint();
        waypointGroupTransferEvent.getGroupTo().addWaypoint(subjectedChangeWp);

        updateAction(subjectedChangeWp, subjectedChangeWp);

    }

    private void addJMButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {

        if (!minecraftClientInstance.isInSingleplayer()) {
            IThemeButton buttonEnabled = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.enable_button",
                    AssetHelper.onOffButtonAsset,
                    getEnabledStatus(),
                    this::enableMod);

            IThemeButton buttonSync = addonButtonDisplayEvent.getThemeButtonDisplay().addThemeToggleButton(
                    "button.jmws.update_button",
                    AssetHelper.enableButtonAsset,
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

    private void groupEventListener(WaypointGroupEvent waypointGroupEvent)
    {
        if (this.getEnabledStatus() && config.serverConfiguration.serverGroupsEnabled() && config.uploadGroups() && !JMWSConstants.forbiddenGroups.contains(waypointGroupEvent.getGroup().getGuid())) {
            ClientPlayerEntity player = minecraftClientInstance.player;
            WaypointGroup waypointGroup = waypointGroupEvent.getGroup();

            if (player == null) {
                return;
            }
            WaypointGroup oldWaypointGroup = this.getOldGroup(waypointGroup);

            switch (waypointGroupEvent.getContext()) {
                case CREATE -> this.groupCreationHandler(waypointGroup, player, false, false); // MAKE SURE you use beta 47 or higher
                case DELETED -> this.groupDeletionHandler(waypointGroup, player, false, waypointGroupEvent.deleteWaypoints());
                case UPDATE -> this.groupUpdateHandler(waypointGroup, oldWaypointGroup, player);
            }
        }
    }

    private void groupUpdateHandler(WaypointGroup waypointGroup, WaypointGroup oldWaypointGroup, ClientPlayerEntity player)
    {
        if (oldWaypointGroup != null) {
            this.groupDeletionHandler(oldWaypointGroup, player, true, false);
        }
        this.groupCreationHandler(waypointGroup, player, true, true);

        sendUserAlert(Text.translatable("message.jmws.modified_group_success"), true, false, JMWSMessageType.SUCCESS);
    }

    private void groupDeletionHandler(WaypointGroup waypointGroup, ClientPlayerEntity player, boolean silent, boolean deleteAllWaypoints)
    {
        waypointIdentifierMap.remove(waypointGroup.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteGroupRequestJson(
                player.getUuid(),
                waypointGroup.getCustomData(),
                waypointGroup.getGuid(),
                silent,
                deleteAllWaypoints,
                false);

        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);
        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void groupCreationHandler(WaypointGroup waypointGroup, ClientPlayerEntity player, boolean silent, boolean isUpdate)
    {
        String waypointIdentifier = CommonHelper.makeWaypointHash(player.getUuid(), waypointGroup.getGuid(), waypointGroup.getName());

        // this is needed because for some reason, when creating a group in the wp creation dialogue box, the CREATION event fires twice.
        if (groupIdentifierMap.containsKey(waypointIdentifier)) { return; }
        groupIdentifierMap.put(waypointIdentifier, waypointGroup);

        waypointGroup.setPersistent(false);
        waypointGroup.setCustomData(waypointIdentifier);
        String creationData = JsonStaticHelper.makeGroupCreationRequestJson(waypointGroup, silent, isUpdate);
        ClientPlayNetworking.send(new JMWSActionPayload(creationData));
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
            ClientPlayNetworking.send(new JMWSActionPayload(JsonStaticHelper.makeWaypointSyncRequestJson(sendAlert)));
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

    public static void HandshakeHandler(HandshakePayload handshakePayload, ClientPlayNetworking.Context context) {

        if (!getInstance().serverEnabledJMWS()) {
            sendUserAlert(Text.translatable("warning.jmws.server_disabled_jmws"), true, false, JMWSMessageType.WARNING);
        } else if (!getInstance().config.serverConfiguration.serverWaypointsEnabled()) {
            sendUserAlert(Text.translatable("warning.jmws.server_disabled_waypoint"), true, false, JMWSMessageType.WARNING);
        } else if (!getInstance().config.serverConfiguration.serverGroupsEnabled()) {
            sendUserAlert(Text.translatable("warning.jmws.server_disabled_group"), true, false, JMWSMessageType.WARNING);
        } else {
            sendUserAlert(Text.translatable("message.jmws.has_jmws"), true, false, JMWSMessageType.SUCCESS);
        }

        getInstance().serverHasMod = true;

        if (timeoutTask != null && !timeoutTask.isDone()) {
            timeoutTask.cancel(false);
        }
    }

    public static void deleteAllGroups() {
        // This method is a bodge fix. removeWaypointGroups (which I believe removes all groups) does not work because you cannot change the modId of a group.

        for (WaypointGroup waypointGroup : getInstance().jmAPI.getAllWaypointGroups()) {
            if (JMWSConstants.forbiddenGroups.contains(waypointGroup.getGuid())) {
                getInstance().jmAPI.removeWaypointGroup(waypointGroup, false);
            }
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

    // Helper for handleUploadGroups
    private static Set<SavedGroup> getSavedGroups(JsonObject jsonData) throws JsonSyntaxException, IllegalStateException {
        Set<SavedGroup> groups = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            groups.add(new SavedGroup(json));
        }

        return groups;

    }

    // Helper for sync, handleUploadWaypoints is basically the same but different type annotations. I should've used generics
    private boolean handleUploadGroups(JsonObject jsonGroupsRaw, ClientPlayNetworking.Context context) throws JsonSyntaxException, IllegalStateException {
        boolean hasLocalGroup = false;

        // Get existing groups (local) and get group objects saved on server
        List<? extends WaypointGroup> existingGroups = getInstance().jmAPI.getAllWaypointGroups();
        Set<SavedGroup> savedGroups = IClientPluginJM.getSavedGroups(jsonGroupsRaw.deepCopy());

        // Get an identifier of every group, used to detect if the group already exists
        Set<String> remoteGroupKeys = savedGroups.stream()
                .map(g -> g.getName() + g.getGroupIdentifier())
                .collect(Collectors.toSet());

        // Test if any existing groups (persistent) have already been added to the server, if not, add them
        for (WaypointGroup existingGroup : existingGroups) {
            String key = existingGroup.getName() + existingGroup.getGuid();
            if (!remoteGroupKeys.contains(key) && !JMWSConstants.forbiddenGroups.contains(existingGroup.getGuid()) && existingGroup.isPersistent()) {
                getInstance().groupCreationHandler(existingGroup, context.player(), true, false);
                hasLocalGroup = true;
            }
        }

        // Add server groups to the client
        for (SavedGroup savedGroup : savedGroups) {
            WaypointGroup group = WaypointFactory.fromGroupJsonString(savedGroup.getRawPacketData());
            getInstance().groupIdentifierMap.put(savedGroup.getUniversalIdentifier(), group);
            getInstance().jmAPI.addWaypointGroup(group);
        }

        // return this because need to give an alert
        return hasLocalGroup;
    }

    // Helper for sync but for waypoints
    private boolean handleUploadWaypoints(JsonObject jsonWaypoints, ClientPlayNetworking.Context context) throws JsonSyntaxException, IllegalStateException {
        boolean hasLocalWaypoint = false;

        // Get existing waypoints (local) and get waypoint objects saved on server
        List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
        Set<SavedWaypoint> savedWaypoints = IClientPluginJM.getSavedWaypoints(jsonWaypoints.deepCopy(), context.player().getUuid());

        // Get an identifier of every waypoint (BlockPos, location), used to detect if the waypoint already exists
        Set<BlockPos> remoteWaypointPositions = savedWaypoints.stream()
                .map(w -> new BlockPos(w.getWaypointX(), w.getWaypointY(), w.getWaypointZ()))
                .collect(Collectors.toSet());

        getInstance().jmAPI.removeAllWaypoints("journeymap");

        // Test if any existing waypoints (persistent, usually death waypoints) have already been added to the server, if not, add them
        for (Waypoint existing : existingWaypoints) {
            if (!remoteWaypointPositions.contains(existing.getBlockPos()) && existing.isPersistent()) {
                getInstance().createAction(existing, true, false);
                hasLocalWaypoint = true;
            }
        }

        // Add server waypoints to the client
        for (SavedWaypoint savedWaypoint : savedWaypoints) {
            Waypoint wp = WaypointFactory.fromWaypointJsonString(savedWaypoint.getRawPacketData());
            getInstance().waypointIdentifierMap.put(savedWaypoint.getUniversalIdentifier(), wp);
            getInstance().jmAPI.addWaypoint("journeymap", wp);
        }

        return hasLocalWaypoint;
    }

    public static void syncHandler(JMWSActionPayload waypointPayload, ClientPlayNetworking.Context context) {
        boolean hasLocalGroup = false;
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().getLast().getAsBoolean();

        try {

            if (config.uploadGroups() && config.serverConfiguration.serverGroupsEnabled()) {
                hasLocalGroup = getInstance().handleUploadGroups(waypointPayload.arguments().get(1).getAsJsonObject(), context);
            }

            if (config.uploadWaypoints() && config.serverConfiguration.serverWaypointsEnabled()) {
                hasLocalWaypoint = getInstance().handleUploadWaypoints(waypointPayload.arguments().getFirst().getAsJsonObject(), context);
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

    public static void removeAllGroups() {
        for (WaypointGroup wp : getInstance().jmAPI.getAllWaypointGroups()) {
            if (!JMWSConstants.forbiddenGroups.contains(wp.getGuid())) {
                getInstance().jmAPI.removeWaypointGroup(wp, false);
            }
        }
    }
    // Handler for JMWSActionPayload
    public static void HandlePacket(JMWSActionPayload waypointPayload, ClientPlayNetworking.Context context) {

        if (getInstance().getEnabledStatus()) {

            if (minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {

                // Was creation_response
                // Sends no outbound data
                case SYNC -> syncHandler(waypointPayload, context);

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
                            INSTANCE.jmAPI.removeAllWaypoints("journeymap");
                        } else {
                            INSTANCE.jmAPI.removeWaypoint("journeymap", INSTANCE.waypointIdentifierMap.get(firstArgument));
                        }

                    } else {
                        deletionMessageConfirmationKey = "message.jmws.deletion_group_all_success";
                        if (Objects.equals(firstArgument, "*")) {
                            IClientPluginJM.deleteAllGroups();
                        } else {
                            INSTANCE.jmAPI.removeWaypointGroup(getInstance().groupIdentifierMap.get(firstArgument), false);
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
