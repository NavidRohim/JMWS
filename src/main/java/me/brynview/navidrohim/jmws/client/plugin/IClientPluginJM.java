package me.brynview.navidrohim.jmws.client.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.event.common.WaypointGroupEvent;
import journeymap.api.v2.common.event.common.WaypointGroupTransferEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.JMServer;
import me.brynview.navidrohim.jmws.client.JMServerClient;
import me.brynview.navidrohim.jmws.common.SavedGroup;
import me.brynview.navidrohim.jmws.common.SavedWaypoint;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.CommonHelper;
import me.brynview.navidrohim.jmws.common.utils.JMWSConfig;
import me.brynview.navidrohim.jmws.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.utils.JMWSIOInterface;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@JourneyMapPlugin(apiVersion = "2.0.0")
public class IClientPluginJM implements IClientPlugin
{
    private static final Logger log = LoggerFactory.getLogger(IClientPluginJM.class);
    private static ScheduledFuture<?> timeoutTask;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // API reference
    private IClientAPI jmAPI = null;
    private static IClientPluginJM INSTANCE;

    private final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();
    private final HashMap<String, WaypointGroup> groupIdentifierMap = new HashMap<>();
    private final List<String> forbiddenGroups = List.of("journeymap_death", "journeymap_all", "journeymap_temp", "journeymap_default");

    private boolean oldWorld = false;
    private boolean serverHasMod = false;

    private static final JMWSConfig config = JMServerClient.CONFIG;
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
        return serverHasMod && config.enabled() && (config.uploadGroups() || config.uploadWaypoints()) && !minecraftClient.isInSingleplayer();
    }

    public Waypoint getOldWaypoint(Waypoint newWaypoint) {
        String persistentWaypointID = newWaypoint.getCustomData();
        return waypointIdentifierMap.get(persistentWaypointID);
    }

    public WaypointGroup getOldGroup(WaypointGroup newWaypointGroup)
    {
        return groupIdentifierMap.get(newWaypointGroup.getCustomData());
    }

    private void deleteAction(Waypoint waypoint, ClientPlayerEntity player, boolean silent) {

        String waypointFilename = JMWSIOInterface.getWaypointFilename(waypoint, player.getUuid());

        waypointIdentifierMap.remove(waypoint.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteRequestJson(waypointFilename, silent);
        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);

        jmAPI.removeWaypoint("journeymap", waypoint);
        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void updateAction(Waypoint waypoint, Waypoint oldWaypoint, ClientPlayerEntity player)
    {
        this.deleteAction(oldWaypoint, player, true);
        this.createAction(waypoint, player, true);

        jmAPI.removeWaypoint("journeymap", oldWaypoint);
        sendUserAlert(Text.translatable("message.jmws.modified_waypoint_success").getString(), true, false);
    }

    private void createAction(Waypoint waypoint, ClientPlayerEntity player, boolean silent) {

        String waypointIdentifier = CommonHelper.makeWaypointHash(player.getUuid(), waypoint.getGuid(), waypoint.getName());
        waypointIdentifierMap.put(waypointIdentifier, waypoint);

        waypoint.setPersistent(false);
        waypoint.setCustomData(waypointIdentifier);
        String creationData = JsonStaticHelper.makeCreationRequestJson(waypoint, silent);
        ClientPlayNetworking.send(new JMWSActionPayload(creationData));
    }

    void WaypointCreationHandler(WaypointEvent waypointEvent) {
        if (this.getEnabledStatus() && config.uploadWaypoints()) {
            MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();
            ClientPlayerEntity player = minecraftClientInstance.player;

            if (player == null) {
                return;
            }

            Waypoint oldWaypoint = this.getOldWaypoint(waypointEvent.waypoint);

            switch (waypointEvent.getContext()) {

                case CREATE ->
                    // Sends "create" packet | new = "SERVER_CREATE"
                        this.createAction(waypointEvent.waypoint, player, false);
                case DELETED ->
                    // Sends "delete" packet | new = "COMMON_SERVER_DELETE"
                        this.deleteAction(waypointEvent.waypoint, player, false);
                case UPDATE ->
                    // Sends both "delete" and "create" packet in respective order and respective enums.
                        this.updateAction(waypointEvent.waypoint, oldWaypoint, player);
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
        CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", JMServer.MODID, this::WaypointCreationHandler);
        CommonEventRegistry.WAYPOINT_GROUP_TRANSFER_EVENT.subscribe("jmapi2", JMServer.MODID, this::transferWaypointToGroup);
        CommonEventRegistry.WAYPOINT_GROUP_EVENT.subscribe("jmapi1", JMServer.MODID, this::groupEventListener);

        // Vanilla Events
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            ClientPlayNetworking.send(new HandshakePayload());

            timeoutTask = scheduler.schedule(() -> {
                if (!serverHasMod) {
                    MinecraftClient.getInstance().execute(() -> {
                        sendUserAlert("§CServer does not have JMWS installed. JMWS will be disabled.", true, true);
                    });
                }
            }, config.serverHandshakeTimeout(), TimeUnit.SECONDS);
        }));
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            tickCounter = 0;
            serverHasMod = false;
        }));
    }

    private void transferWaypointToGroup(WaypointGroupTransferEvent waypointGroupTransferEvent) {
        JMServer.LOGGER.info("wip.");
        //waypointGroupTransferEvent.cancel();
    }

    private void groupEventListener(WaypointGroupEvent waypointGroupEvent)
    {
        if (this.getEnabledStatus() && config.uploadGroups()) {
            MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();
            ClientPlayerEntity player = minecraftClientInstance.player;
            WaypointGroup waypointGroup = waypointGroupEvent.getGroup();

            if (player == null) {
                return;
            }
            WaypointGroup oldWaypointGroup = this.getOldGroup(waypointGroup);

            switch (waypointGroupEvent.getContext()) {
                case CREATE -> this.groupCreationHandler(waypointGroup, player, false); // MAKE SURE you use beta 47 or higher

                case DELETED ->
                        this.groupDeletionHandler(waypointGroup, player, false, waypointGroupEvent.deleteWaypoints());
                case UPDATE ->
                        this.groupUpdateHandler(waypointGroup, oldWaypointGroup, player);
            }
        }
    }

    private void groupUpdateHandler(WaypointGroup waypointGroup, WaypointGroup oldWaypointGroup, ClientPlayerEntity player)
    {
        this.groupDeletionHandler(oldWaypointGroup, player, true, false);
        this.groupCreationHandler(waypointGroup, player, true);

        jmAPI.removeWaypointGroup(waypointGroup, false);
        sendUserAlert(Text.translatable("message.jmws.modified_group_success").getString(), true, false);
    }

    private void groupDeletionHandler(WaypointGroup waypointGroup, ClientPlayerEntity player, boolean silent, boolean deleteAllWaypoints)
    {
        waypointIdentifierMap.remove(waypointGroup.getCustomData());
        String jsonPacketData = JsonStaticHelper.makeDeleteGroupRequestJson(JMWSIOInterface.getGroupFilename(player.getUuid(), waypointGroup.getCustomData()), silent);

        jmAPI.removeWaypointGroup(waypointGroup, deleteAllWaypoints);
        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);
        ClientPlayNetworking.send(waypointActionPayload);
    }

    private void groupCreationHandler(WaypointGroup waypointGroup, ClientPlayerEntity player, boolean silent)
    {
        String waypointIdentifier = CommonHelper.makeWaypointHash(player.getUuid(), waypointGroup.getGuid(), waypointGroup.getName());
        groupIdentifierMap.put(waypointIdentifier, waypointGroup);

        waypointGroup.setPersistent(false);
        waypointGroup.setCustomData(waypointIdentifier);
        String creationData = JsonStaticHelper.makeGroupCreationRequestJson(waypointGroup, silent);
        ClientPlayNetworking.send(new JMWSActionPayload(creationData));
    }

    public static void updateWaypoints() {

        // Sends "request" packet | New = "SYNC"

        ClientPlayNetworking.send(new JMWSActionPayload(JsonStaticHelper.makeWaypointSyncRequestJson()));
        String updateMessageKey = "message.jmws.synced_success";

        if (config.uploadWaypoints() && config.uploadGroups()) {
            updateMessageKey = "message.jmws.synced_both_success";
        } else if (config.uploadGroups()) {
            updateMessageKey = "message.jmws.synced_group_success";
        }

        sendUserAlert(Text.translatable(updateMessageKey).getString(), true, false);
    }

    private void handleTick(MinecraftClient minecraftClient) {

        // Sends "sync" packet | New = SYNC
        if (minecraftClient.world != null && this.getEnabledStatus()) {
            if (!oldWorld) {
                tickCounterUpdateThreshold = 20 * (config.serverHandshakeTimeout() + 1);
                oldWorld = true;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) {

                    updateWaypoints();
                    tickCounter = 0;
                    tickCounterUpdateThreshold = config.updateWaypointFrequency();
                }
            }
        } else {
            tickCounter = 0;
            oldWorld = false;
        }
    }

    @Override
    public String getModId()
    {
        return JMServer.MODID;
    }

    public static List<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) {
        List<SavedWaypoint> waypoints = new ArrayList<>();

        for (Map.Entry<String, JsonElement> wpEntry : jsonData.entrySet()) {
            JsonObject rawData = JsonParser.parseString(wpEntry.getValue().getAsString()).getAsJsonObject();
            waypoints.add(new SavedWaypoint(rawData, playerUUID));
        }
        return waypoints;
    }

    // todo; experiment with making parent
    public static List<SavedGroup> getSavedGroups(JsonObject jsonData) {
        List<SavedGroup> waypoints = new ArrayList<>();

        for (Map.Entry<String, JsonElement> wpEntry : jsonData.entrySet()) {
            JsonObject rawData = JsonParser.parseString(wpEntry.getValue().getAsString()).getAsJsonObject();
            waypoints.add(new SavedGroup(rawData));
        }
        return waypoints;
    }


    public static void HandshakeHandler(HandshakePayload handshakePayload, ClientPlayNetworking.Context context) {

        sendUserAlert("§2Server has JMWS!", true, false);
        getInstance().serverHasMod = true;

        if (timeoutTask != null && !timeoutTask.isDone()) {
            timeoutTask.cancel(false);
        }
    }

    public static void deleteAllGroups() {
        // This method is a bodge fix. removeWaypointGroups (which I believe removes all groups) doesnt work because you cannot change the modId of a group.

        for (WaypointGroup waypointGroup : getInstance().jmAPI.getAllWaypointGroups()) {
            if (!getInstance().forbiddenGroups.contains(waypointGroup.getGuid())) {
                getInstance().jmAPI.removeWaypointGroup(waypointGroup, false);
            }
        }
    }
    // Handler for WaypointActionPayload
    public static void HandlePacket(JMWSActionPayload waypointPayload, ClientPlayNetworking.Context context) {
        if (getInstance().getEnabledStatus()) {
            MinecraftClient minecraftClientInstance = MinecraftClient.getInstance();

            if (minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {

                // Was creation_response
                // Sends no outbound data
                case SYNC -> {

                    boolean hasLocalGroup = false;
                    boolean hasLocalWaypoint = false;

                    // I really really REALLY hate this. The code is basically executing twice over for waypoints and group. I dont know enough about Java.
                    // A lot of this code regarding groups is shit. I know it is just by instinct but I do not know how to fix it.
                    if (config.uploadGroups())
                    {
                        JsonObject jsonGroups = waypointPayload.arguments().get(1).getAsJsonObject().deepCopy();

                        List<? extends WaypointGroup> existingGroups = getInstance().jmAPI.getAllWaypointGroups();

                        List<SavedGroup> savedGroups = IClientPluginJM.getSavedGroups(jsonGroups);
                        List<String> remoteGroupsIdentifier = new ArrayList<>();

                        for (SavedGroup savedGroup : savedGroups) {
                            remoteGroupsIdentifier.add(savedGroup.getName() + savedGroup.getGroupIdentifier());
                        }
                        IClientPluginJM.deleteAllGroups(); // Delete all existing groups (bodge)

                        for (WaypointGroup existingGroup : existingGroups)
                        {
                            if (!remoteGroupsIdentifier.contains(existingGroup.getName() + existingGroup.getGuid()) && !getInstance().forbiddenGroups.contains(existingGroup.getGuid()))
                            {
                                getInstance().groupCreationHandler(existingGroup, context.player(), true);
                                hasLocalGroup = true;
                            }
                        }

                        for (SavedGroup savedGroup : savedGroups)
                        {
                            WaypointGroup waypointGroupObj = WaypointFactory.fromGroupJsonString(savedGroup.getRawPacketData());
                            getInstance().groupIdentifierMap.put(savedGroup.getUniversalIdentifier(), waypointGroupObj);
                            getInstance().jmAPI.addWaypointGroup(waypointGroupObj);
                        }
                    }
                    if (config.uploadWaypoints()) {
                        JsonObject json = waypointPayload.arguments().getFirst().getAsJsonObject().deepCopy();

                        List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
                        List<SavedWaypoint> savedWaypoints = IClientPluginJM.getSavedWaypoints(json, context.player().getUuid());
                        List<BlockPos> remoteWaypointsGuid = new ArrayList<>();


                        // Add server waypoint coordinates onto list to check
                        for (SavedWaypoint savedWaypoint : savedWaypoints) {
                            remoteWaypointsGuid.add(new BlockPos(savedWaypoint.getWaypointX(), savedWaypoint.getWaypointY(), savedWaypoint.getWaypointZ()));
                        }

                        // remove all to update (if waypoint has been removed)
                        getInstance().jmAPI.removeAllWaypoints("journeymap");

                        for (Waypoint existingWaypoint : existingWaypoints)
                        {
                            // check if waypoint already exists locally while not being in the server (meaning it was created with the mod off or not installed)
                            if (!remoteWaypointsGuid.contains(existingWaypoint.getBlockPos())) // this is only checked by using the block position, there will be a bug I can feel it
                            {
                                getInstance().createAction(existingWaypoint, context.player(), true);
                                hasLocalWaypoint = true;
                            }
                        }

                        // Add waypoints registered on the server
                        for (SavedWaypoint savedWaypoint : savedWaypoints)
                        {
                            Waypoint waypointObj = WaypointFactory.fromWaypointJsonString(savedWaypoint.getRawPacketData()); // This is a method that will only work on a pre-release version of JourneyMap that hasnt been released yet.
                            getInstance().waypointIdentifierMap.put(savedWaypoint.getUniversalIdentifier(), waypointObj);
                            getInstance().jmAPI.addWaypoint("journeymap", waypointObj);
                        }
                    }

                    // this refreshes the client again because of the local waypoints
                    if (hasLocalGroup && hasLocalWaypoint) {
                        updateWaypoints();
                        sendUserAlert(Text.translatable("message.jmws.local_both_upload").getString(), true, false);
                    }

                    else if (hasLocalWaypoint) {
                        updateWaypoints();
                        sendUserAlert(Text.translatable("message.jmws.local_waypoint_upload").getString(), true, false);
                    }

                    else if (hasLocalGroup) {
                        updateWaypoints();
                        sendUserAlert(Text.translatable("message.jmws.local_group_upload").getString(), true, false);
                    }
                }

                // was "update"
                // Sends "request" packet | New = "SYNC"
                case REQUEST_CLIENT_SYNC -> IClientPluginJM.updateWaypoints();


                // was display_interval
                // No outbound data
                case COMMON_DISPLAY_INTERVAL -> sendUserAlert("Waypoints updated every " + INSTANCE.tickCounterUpdateThreshold / 20 + " seconds.", true, false);

                // was "alert"
                // No outbound data
                case CLIENT_ALERT -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    sendUserAlert(Text.translatable(firstArgument).getString(), waypointPayload.arguments().get(1).getAsBoolean(), false);
                }

                // was "deleteWaypoint"
                // No outbound data
                case COMMON_DELETE_WAYPOINT -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    JMWSIOInterface.FetchType deletionType = JMWSIOInterface.FetchType.valueOf(waypointPayload.arguments().get(1).getAsString());
                    String deletionMessageConfirmationKey = "message.jmws.deletion_all_success";

                    if (deletionType == JMWSIOInterface.FetchType.WAYPOINT) {
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
                    sendUserAlert(Text.translatable(deletionMessageConfirmationKey).getString(), true, false);

                }

                // was "display_next_update"
                // No outbound data
                case COMMON_DISPLAY_NEXT_UPDATE -> sendUserAlert("Next waypoint update in " + (INSTANCE.tickCounterUpdateThreshold - INSTANCE.tickCounter) / 20 + " second(s)", true, false);

                default -> JMServer.LOGGER.warn("Unknown packet command -> " + waypointPayload.command());
            }
        }
    }
}
