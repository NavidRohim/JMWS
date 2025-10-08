package me.brynview.navidrohim.jmws.client.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.kinds.Const;
import commonnetwork.api.Dispatcher;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.event.*;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.api.v2.common.event.common.TeleportEvent;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.event.common.WaypointGroupEvent;
import journeymap.api.v2.common.event.common.WaypointGroupTransferEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.JMWSSounds;
import me.brynview.navidrohim.jmws.client.objects.SavedGroup;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.common.helper.CommandHelper;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.brynview.navidrohim.jmws.common.CommonClass.*;

/**
 * Main JMWS plugin which interfaces with JourneyMap.
 */
@JourneyMapPlugin(apiVersion = "2.0.0")
public class JMWSPlugin implements IClientPlugin {

    // JourneyMap API
    private IClientAPI jmAPI = null;
    private static JMWSPlugin INSTANCE;

    // Required functions

    @Override
    public void initialize(@NotNull IClientAPI jmClientApi)
    {

        this.jmAPI = jmClientApi;

        CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", this::waypointCreationHandler);
        CommonEventRegistry.WAYPOINT_GROUP_EVENT.subscribe("jmapi", Constants.MODID, this::groupEventListener);
        CommonEventRegistry.WAYPOINT_GROUP_TRANSFER_EVENT.subscribe("jmapi", Constants.MODID, this::waypointDragHandler);
        CommonEventRegistry.TELEPORT_EVENT.subscribe("jmapi", Constants.MODID, this::correctTeleportDestination); // Should be temporary

        FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(Constants.MODID, JMButtonAddon::addJMButtons);
        FullscreenEventRegistry.FULLSCREEN_RENDER_EVENT.subscribe(Constants.MODID, (renderEvent) -> {
            CommonClass.config.serverEnabled.set(CommonClass.serverConfig.serverEnabled());
            CommonClass.config.serverUploadWaypoints.set(CommonClass.serverConfig.waypointsEnabled());
            CommonClass.config.serverUploadGroups.set(CommonClass.serverConfig.groupsEnabled());
        });

        ClientEventRegistry.DEATH_WAYPOINT_EVENT.subscribe("jmapi", this::handleUserDeath);
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT.subscribe("jmapi", (optionsRegistryEvent -> config = new ConfigInterface()));
        ClientEventRegistry.MAPPING_EVENT.subscribe("jmapi", (MappingEvent event) -> {JMWSPlugin.updateWaypoints(false);});

    }

    /**
     * Only called from TELEPORT_EVENT (when user teleports) Do not call.
     * This method shouldn't be here. When teleporting to the overworld from the nether or vice versa, you will be put in the wrong location.
     * This is due to the fact that the coordinates between the nether and overworld are not 1:1, it is a well-known fact that 1 block in the nether
     * equals 8 blocks in the overworld. This must be accounted for, but it seems JourneyMap gets these calculations mixed up, which I account for here.
     * @param teleportEvent -- The event.
     */
    private void correctTeleportDestination(TeleportEvent teleportEvent) {
        // Should only been needed temporarily since I believe there is a bug with JM where teleporting to or from the nether will put you in the wrong place
        BlockPos wpBlockPos = teleportEvent.getPos();
        String fromLevel = teleportEvent.getFromLevel().location().getPath();
        String toLevel = teleportEvent.getDestinationLevel().location().getPath();

        if (toLevel.equals("the_nether") && !fromLevel.equals("the_nether")) // If we are going to the Nether
        {
            wpBlockPos = new BlockPos(wpBlockPos.getX() / 8, wpBlockPos.getY(), wpBlockPos.getZ() / 8);
        }
        else if (!toLevel.equals("the_nether") && fromLevel.equals("the_nether")) // If we are leaving the Nether
        {
            wpBlockPos = new BlockPos(wpBlockPos.getX() * 8, wpBlockPos.getY(), wpBlockPos.getZ() * 8);
        }

        teleportEvent.setPos(wpBlockPos);
    }

    /**
     * Only called from DEATH_WAYPOINT_EVENT (when user is killed for any reason) Do not call.
     * @param deathWaypointEvent -- The event. Does not have method for retrieving the death waypoint for some reason.
     */
    private void handleUserDeath(DeathWaypointEvent deathWaypointEvent) {
        if (!isInternalServer())
        {
            // We just manually sync the client to upload the death waypoint. This is because DeathWaypointEvent does not give access to the actual Waypoint instance
            // So there is no actual way of uploading the death waypoint with createAction since it requires a Waypoint instance.
            scheduler.schedule(() -> updateWaypoints(true, true), 5, TimeUnit.SECONDS);
        }
    }
    
    @Override
    public String getModId() {
        return Constants.MODID;
    }

    /**
     * Get the current plugin instance.
     * @return JMWSPlugin -- The JMWS plugin instance. There can only be one per instance of the game.
     */
    public static JMWSPlugin getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor for the plugin. Use the initialise method to do logic and other stuff.
     */
    public JMWSPlugin()
    {
        INSTANCE = this;
    }

    // General helper functions
    // Methods for manipulating / creating waypoints on the server

    /**
     * Creates a waypoint on the server.
     * @param waypoint -- Local instance of the new waypoint to create.
     * @param silent -- If the creation should happen silently (no text alert on the client)
     * @param isUpdate -- If we are just updating an already existing waypoint.
     */
    private void createAction(Waypoint waypoint, boolean silent, boolean isUpdate) {
        if (CommonClass.serverConfig.waypointsEnabled()) {
            ObjectIdentifierMap.addWaypointToMap(waypoint);
            waypoint.setPersistent(false); // Persistence must be false so it does not stay upon leaving. If it did, there would be duplicate waypoints

            String creationData = CommandHelper.makeCreationRequestJson(waypoint, silent, isUpdate);
            Dispatcher.sendToServer(new JMWSActionPayload(creationData));
        } else {
            PlayerHelper.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, JMWSMessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Syncs an updated waypoint to the server.
     * @param waypoint -- What waypoint needs updating
     * @param oldWaypoint -- Old instance of the waypoint (before the update)
     */
    private void updateAction(Waypoint waypoint, Waypoint oldWaypoint)
    {
        if (CommonClass.serverConfig.waypointsEnabled()) // Check config
        {
            if (oldWaypoint != null) { // oldWaypoint can be null if it's not found in the identifier map. Can happen if there's a corrupted waypoint
                this.deleteAction(oldWaypoint, true);
                jmAPI.removeWaypoint("journeymap", oldWaypoint);
            }
            this.createAction(waypoint, true, true);

            PlayerHelper.sendUserAlert(Component.translatable("message.jmws.modified_waypoint_success"), true, false, JMWSMessageType.SUCCESS);
        } else {
            PlayerHelper.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, JMWSMessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Deletes the specified waypoint on the server.
     * @param waypoint -- What waypoint to delete
     * @param silent -- If the deletion should be silent (no text alert on client)
     */
    private void deleteAction(Waypoint waypoint, boolean silent) {
        if (CommonClass.serverConfig.waypointsEnabled()) { // Check if action is allowed by the server.
            String waypointFilename = CommonHelper.getWaypointFilename(waypoint, CommonClass.minecraftClientInstance.player.getUUID());

            ObjectIdentifierMap.removeWaypointFromMap(waypoint);
            String jsonPacketData = CommandHelper.makeDeleteRequestJson(waypointFilename, silent, false);
            JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);

            // removedWaypoint is called here because, yes, we do listen for the deletion with the event (meaning, the waypoint should be already gone by the time the event is called)
            // But for some reason it bugs out and the waypoint stays and becomes persistent
            jmAPI.removeWaypoint("journeymap", waypoint);
            Dispatcher.sendToServer(waypointActionPayload);
        } else {
            PlayerHelper.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, JMWSMessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Only called from WAYPOINT_EVENT (when waypoint is created, updated, or deleted) Do not call.
     * @param waypointEvent The event.
     */
    void waypointCreationHandler(WaypointEvent waypointEvent) {

        if (CommonClass.getEnabledStatus() && config.waypointsEnabled() && serverConfig.waypointsEnabled()) { // Check that user is in physical server, user config allows event, and server config allows event.
            // Get old waypoint if context is UPDATE (needed because server needs reference to waypoint before it was updated so it can be deleted on the server)
            Waypoint oldWaypoint = ObjectIdentifierMap.getOldWaypoint(waypointEvent.waypoint);

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

    /**
     * Only called from WAYPOINT_GROUP_EVENT (when group is created, updated, or deleted) Do not call.
     * @param waypointGroupEvent The event.
     */
    private void groupEventListener(WaypointGroupEvent waypointGroupEvent)
    {
        if (CommonClass.getEnabledStatus() && config.groupsEnabled() && serverConfig.groupsEnabled()) // Check that user is in physical server, user config allows event, and server config allows event.
        {
            LocalPlayer player = CommonClass.minecraftClientInstance.player;
            WaypointGroup waypointGroup = waypointGroupEvent.getGroup();

            // Check if group is JourneyMap build-in group. You can delete an in-built group with the JM API but things will crash.
            // Trying to delete an in-built group with JM will delete the waypoints inside the group. The following flow statement checks for that and does it on the server.
            if (Constants.forbiddenGroups.contains(waypointGroup.getGuid()) && waypointGroupEvent.getContext().equals(WaypointGroupEvent.Context.DELETED))
            {
                this.groupDeletionHandler(waypointGroup, player, false, true, false);
            }
            else if (!Constants.forbiddenGroups.contains(waypointGroupEvent.getGroup().getGuid())) { // If group is not in-built and can be deleted
                if (player == null) {
                    return;
                }

                // Get old group if context is UPDATE (needed because server needs reference to group before it was updated so it can be deleted on the server)
                WaypointGroup oldWaypointGroup = ObjectIdentifierMap.getOldGroup(waypointGroup);

                switch (waypointGroupEvent.getContext()) {
                    case CREATE -> this.groupCreationHandler(waypointGroup, false, false);
                    case DELETED -> this.groupDeletionHandler(waypointGroup, player, false, waypointGroupEvent.deleteWaypoints(), true);
                    case UPDATE -> this.groupUpdateHandler(waypointGroup, oldWaypointGroup, player);
                }
            }
        }
    }

    /**
     * Delete group(s) on the server.
     * @param waypointGroup -- What group needs deleting
     * @param player -- What player this group belongs to
     * @param silent -- If the deletion should be silent (no text alert)
     * @param deleteAllWaypoints -- If to delete all the users groups on the server.
     * @param removeGroupItself -- If to delete just the waypoints inside the group, not the group itself. Used in edge cases like removing all waypoints in an in-built group.
     */
    private void groupDeletionHandler(WaypointGroup waypointGroup, LocalPlayer player, boolean silent, boolean deleteAllWaypoints, boolean removeGroupItself)
    {
        if (CommonClass.serverConfig.groupsEnabled()) // Make sure config allows it
        {
            ObjectIdentifierMap.removeGroupFromMap(waypointGroup); // Remove from identifier map
            String uID = waypointGroup.getCustomData() != null ? waypointGroup.getCustomData() : "null"; // This can be set to "null" but I cannot remember why.

            String jsonPacketData = CommandHelper.makeDeleteGroupRequestJson(
                    player.getUUID(),
                    uID,
                    waypointGroup.getGuid(),
                    silent,
                    deleteAllWaypoints,
                    removeGroupItself,
                    false);

            JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);
            Dispatcher.sendToServer(waypointActionPayload);
        } else {
            PlayerHelper.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, JMWSMessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Syncs an updated group to the server.
     * @param waypointGroup -- What group needs updating
     * @param oldWaypointGroup -- Old instance of the group (before the update)
     * @param player -- Which player this group belongs to
     */
    private void groupUpdateHandler(WaypointGroup waypointGroup, WaypointGroup oldWaypointGroup, LocalPlayer player)
    {
        if (CommonClass.serverConfig.groupsEnabled()) // Make sure config allows it
        {
            // Internally, we just delete the old group and create a new one
            if (oldWaypointGroup != null) {
                this.groupDeletionHandler(oldWaypointGroup, player, true, false, true);
            }
            this.groupCreationHandler(waypointGroup, true, true);

            // Send alert
            PlayerHelper.sendUserAlert(Component.translatable("message.jmws.modified_group_success"), true, false, JMWSMessageType.SUCCESS);
        } else {
            PlayerHelper.sendUserAlert(Component.translatable("message.jmws.server_disabled_groups"), true, false, JMWSMessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Only called from WAYPOINT_GROUP_TRANSFER_EVENT (when waypoint is dragged into a group) Do not call.
     * @param waypointGroupTransferEvent The event.
     */
    private void waypointDragHandler(WaypointGroupTransferEvent waypointGroupTransferEvent) {
        // Do not do on LAN, since there is no physical server.
        if (!isInternalServer())
        {
            Waypoint subjectedChangeWp = waypointGroupTransferEvent.getWaypoint();
            waypointGroupTransferEvent.getGroupTo().addWaypoint(subjectedChangeWp);

            updateAction(subjectedChangeWp, subjectedChangeWp); // Update the waypoint on server, pass the same group as old and new since it doesn't matter in this context
        }
    }

    /**
     * Removes all local groups. I remember jmAPI.removeAllWaypoints() for some reason doesn't work.
     */
    public static void deleteAllGroups() {
        // This method is a bodge fix. removeWaypointGroups (which I believe removes all groups) does not work because you cannot change the modId of a group.

        // Iterate through each group, delete it
        for (WaypointGroup wp : getInstance().jmAPI.getAllWaypointGroups()) {
            if (!Constants.forbiddenGroups.contains(wp.getGuid())) {
                getInstance().jmAPI.removeWaypointGroup(wp, false);
            }
        }
    }

    /**
     * Delete a synced object (waypoint or group). This method is only called by COMMON_DELETE_WAYPOINT in the packet handler on the client-side.
     * @param deleteAll -- If to delete all the specified deletionType.
     * @param deletionType -- What saved object to delete (waypoint or group)
     * @param toDelete -- ObjectIdentifierMap ID, this is stored on the server only in the "customData" field (example; 38ab19a2e6544389265e40ad23b49983d9620b199c111e20b2b9a5159458b519)
     */
    public void deleteSavedObjects(Boolean deleteAll, JMWSServerIO.FetchType deletionType, String toDelete)
    {
        String deletionMessageConfirmationKey = "message.jmws.deletion_all_success";

        if (deletionType == JMWSServerIO.FetchType.WAYPOINT) {
            if (deleteAll) {
                INSTANCE.jmAPI.removeAllWaypoints("journeymap");
            } else {
                INSTANCE.jmAPI.removeWaypoint("journeymap", ObjectIdentifierMap.getOldWaypoint(toDelete));
            }

        } else {
            deletionMessageConfirmationKey = "message.jmws.deletion_group_all_success";
            if (deleteAll) {
                JMWSPlugin.deleteAllGroups();
            } else {
                JMWSPlugin.getInstance().jmAPI.removeWaypointGroup(ObjectIdentifierMap.getOldGroup(toDelete), false);
            }
        }
        PlayerHelper.sendUserAlert(Component.translatable(deletionMessageConfirmationKey), true, false, JMWSMessageType.NEUTRAL);
    }

    /**
     * Syncs client to server
     * @param sendAlert -- If to send an alert when finished syncing
     * @param fromDeathEvent -- If this is being called as regards to a death event (Forces a local waypoint update)
     */
    public static void updateWaypoints(boolean sendAlert, boolean fromDeathEvent) {

        // Sends "request" packet | New = "SYNC"
        if (CommonClass.getEnabledStatus()) {
            Dispatcher.sendToServer(new JMWSActionPayload(CommandHelper.makeWaypointSyncRequestJson(sendAlert, fromDeathEvent)));
        }
    }

    /**
     * Syncs client to server
     * @param sendAlert -- If to send an alert when finished syncing
     */
    public static void updateWaypoints(boolean sendAlert) {
        updateWaypoints(sendAlert, false);
    }

    // Syncing -- Functions for syncing waypoints and groups

    /**
     * Handles a local groups creation, syncs it to the server
     * @param waypointGroup -- What waypoint group to create / update
     * @param silent -- If there should be an alert when created
     * @param isUpdate -- If the creation is just an update to an already existing group
     */
    private void groupCreationHandler(WaypointGroup waypointGroup, boolean silent, boolean isUpdate)
    {
        if (CommonClass.serverConfig.groupsEnabled()) {
            ObjectIdentifierMap.addGroupToMap(waypointGroup);
            waypointGroup.setPersistent(false);
            String creationData = CommandHelper.makeGroupCreationRequestJson(waypointGroup, silent, isUpdate);

            Dispatcher.sendToServer(new JMWSActionPayload(creationData));
        } else {
            PlayerHelper.sendUserAlert(Component.translatable("message.jmws.server_disabled_groups"), true, false, JMWSMessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Helper for syncHandler, do not use.
     * @param jsonData Json data from the server
     * @return A set of SavedWaypoints from the server.
     * @throws JsonSyntaxException If waypoint is malformed or does not parse.
     * @throws IllegalStateException Cannot remember why this can be thrown.
     */
    private static Set<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) throws JsonSyntaxException, IllegalStateException {
        Set<SavedWaypoint> waypoints = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            waypoints.add(new SavedWaypoint(json, playerUUID));
        }

        return waypoints;

    }

    /**
     * Helper for syncHandler, do not use.
     * @param jsonData Json data from the server
     * @return A set of SavedGroups from the server.
     * @throws JsonSyntaxException If group is malformed or does not parse.
     * @throws IllegalStateException Cannot remember why this can be thrown.
     */
    private static Set<SavedGroup> getSavedGroups(JsonObject jsonData) throws JsonSyntaxException, IllegalStateException {
        Set<SavedGroup> groups = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            groups.add(new SavedGroup(json));
        }

        return groups;

    }

    /**
     * Sync helper for waypoints
     * @param jsonGroupsRaw -- The groups to add to the client.
     * @return boolean -- If the user had any local groups to upload.
     * @throws JsonSyntaxException -- If there is a syntax error with the Json, usually from a corrupted group.
     */
    private boolean handleUploadGroups(JsonObject jsonGroupsRaw) throws JsonSyntaxException, IllegalStateException {
        boolean hasLocalGroup = false;

        // Get existing groups (local) and get group objects saved on server
        List<? extends WaypointGroup> existingGroups = getInstance().jmAPI.getAllWaypointGroups();
        Set<SavedGroup> savedGroups = JMWSPlugin.getSavedGroups(jsonGroupsRaw.deepCopy());

        // Get an identifier of every group, used to detect if the group already exists
        Set<String> remoteGroupKeys = savedGroups.stream()
                .map(g -> g.getName() + g.getGroupIdentifier())
                .collect(Collectors.toSet());

        // Test if any existing groups (persistent) have already been added to the server, if not, add them
        for (WaypointGroup existingGroup : existingGroups) {
            String key = existingGroup.getName() + existingGroup.getGuid();
            if (!remoteGroupKeys.contains(key) && !Constants.forbiddenGroups.contains(existingGroup.getGuid()) && existingGroup.isPersistent()) {
                getInstance().groupCreationHandler(existingGroup, true, false);
                hasLocalGroup = true;
            }
        }

        // Add server groups to the client
        for (SavedGroup savedGroup : savedGroups) {
            WaypointGroup group = WaypointFactory.fromGroupJsonString(savedGroup.getRawPacketData());
            ObjectIdentifierMap.addGroupToMap(group);
            getInstance().jmAPI.addWaypointGroup(group);
        }

        // return this because need to give an alert
        return hasLocalGroup;
    }

    /**
     * Sync helper for waypoints
     * @param jsonWaypoints -- The waypoints to add to the client.
     * @return boolean -- If the user had any local waypoints to upload.
     * @throws JsonSyntaxException -- If there is a syntax error with the Json, usually from a corrupted waypoint.
     */
    private boolean handleUploadWaypoints(JsonObject jsonWaypoints) throws JsonSyntaxException {
        boolean hasLocalWaypoint = false;

        // Get existing waypoints (local) and get waypoint objects saved on server
        List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
        Set<SavedWaypoint> savedWaypoints = JMWSPlugin.getSavedWaypoints(jsonWaypoints.deepCopy(), CommonClass.minecraftClientInstance.player.getUUID());

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
            ObjectIdentifierMap.addWaypointToMap(wp);

            Constants.getLogger().info(String.valueOf(wp.getBlockPos()));
            getInstance().jmAPI.addWaypoint("journeymap", wp);
        }

        return hasLocalWaypoint;
    }

    /**
     * Adds and syncs waypoints and groups to and from the server
     * @param waypointPayload -- The sync payload from the server.
     */
    public static void syncHandler(JMWSActionPayload waypointPayload) {
        boolean hasLocalGroup = false;
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().get(2).getAsBoolean(); // If to send an alert
        boolean isDeathSync = waypointPayload.arguments().getLast().getAsBoolean(); // If the sync was from a death waypoint creation

        try {
            // Sync remote and local groups if server and client permits
            if (config.uploadGroups.get() && CommonClass.serverConfig.groupsEnabled()) {
                hasLocalGroup = getInstance().handleUploadGroups(waypointPayload.arguments().get(1).getAsJsonObject());
            }

            // Sync remote and local waypoints if server and client permits
            if (config.uploadGroups.get() && CommonClass.serverConfig.waypointsEnabled()) {
                hasLocalWaypoint = getInstance().handleUploadWaypoints(waypointPayload.arguments().getFirst().getAsJsonObject());
            }

            // Send alerts if there were any local waypoints and or groups
            if (hasLocalGroup || hasLocalWaypoint) {
                updateWaypoints(false);
                if (isDeathSync) {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.death_waypoint_sync"), true, false, JMWSMessageType.SUCCESS);
                } else if (hasLocalGroup && hasLocalWaypoint) {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.local_both_upload"), true, false, JMWSMessageType.SUCCESS);
                } else if (hasLocalGroup) {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.local_group_upload"), true, false, JMWSMessageType.SUCCESS);
                } else {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.local_waypoint_upload"), true, false, JMWSMessageType.SUCCESS);
                }

            } else if (sendAlert) { // send alert, client permitting
                String updateMessageKey = "message.jmws.synced_success";

                // Sync message can change depending on what client permissions there are
                if (config.uploadGroups.get() && config.uploadGroups.get()) {
                    updateMessageKey = "message.jmws.synced_both_success";
                } else if (config.uploadGroups.get()) {
                    updateMessageKey = "message.jmws.synced_group_success";
                }
                PlayerHelper.sendUserAlert(Component.translatable(updateMessageKey), true, false, JMWSMessageType.NEUTRAL);
            }

            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
            CommonClass.syncCounter.resetSyncThreshold(); // Reset auto-sync timer

        } catch (IllegalStateException | JsonSyntaxException exception) {
            PlayerHelper.sendUserAlert(Component.translatable("error.jmws.error_corrupted_waypoint"), true, false, JMWSMessageType.FAILURE);
            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
        }
    }
}
